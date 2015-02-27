package Exercise3;

import Exercise3.containers.AuctionItem;
import Exercise3.containers.AuctionState;
import Exercise3.containers.Bid;
import Exercise3.containers.Item;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.*;


public class NegotiationAgent extends Agent {
    private HashMap<Item, Integer> resourceDeficit;
    private int coins = 10000;


    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Trader");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        resourceDeficit = Exchange.getResourceDistribution();
        System.out.println(getLocalName() + ": " + resourceDeficit.entrySet());

        addBehaviour(new SaleStuff());
    }


    class SaleStuff extends Behaviour {
        private ArrayList<Bid> bids = new ArrayList<Bid>();
        private int nrRejected = 0, nrBidders = 0;
        private AuctionState myAuction = null;

        @Override
        public void action() {
            ACLMessage response, msg = receive();
            Bid bid;
            AuctionItem ai;
            AuctionState as;


            try {
                if(msg == null) return;
                switch (msg.getPerformative()) {
                    case ACLMessage.QUERY_IF:
                        ai = getItemForSale();
                        if(ai == null) response = new ACLMessage(ACLMessage.DISCONFIRM);
                        else response = new ACLMessage(ACLMessage.CONFIRM);
                        response.addReceiver(Exchange.getExchange());
                        myAgent.send(response);
                        if(ai == null) return;

                        response = new ACLMessage(ACLMessage.INFORM);
                        for (AID trader : Exchange.getTraders()) {
                            if (! trader.equals(myAgent.getAID())) {
                                nrBidders++;
                                response.addReceiver(trader);
                            }
                        }

                        myAuction = new AuctionState(ai, getWantedItems());
                        response.setContentObject(myAuction);
                        myAgent.send(response);
                        break;

                    case ACLMessage.INFORM:
                        as = (AuctionState) msg.getContentObject();
                        int quantityNeeded = -resourceDeficit.get(as.getAuctionItem().getItem());
                        bid = generateBid(as);

                        if(quantityNeeded > 0 && bid != null) {
                            response = new ACLMessage(ACLMessage.PROPOSE);
                            response.setContentObject(bid);
                        } else {
                            response = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                        }

                        response.addReceiver(as.getAuctionItem().getOwner());
                        myAgent.send(response);
                        break;

                    case ACLMessage.REJECT_PROPOSAL:
                        nrRejected++;
                        break;

                    case ACLMessage.PROPOSE:
                        bid = (Bid) msg.getContentObject();
                        System.out.println(getLocalName() + ": Bid: " + bid + " | " + myAuction.getAuctionItem());
                        bids.add(bid);
                        break;

                    case ACLMessage.ACCEPT_PROPOSAL:
                        as = (AuctionState) msg.getContentObject();
                        Item item = as.getAuctionItem().getItem();
                        resourceDeficit.put(item, resourceDeficit.get(item) + as.getAuctionItem().getAmount());
                        adjustResources(as.getBestBid(), -1);
                        break;
                }


                if(myAuction != null && bids.size() + nrRejected == nrBidders) {
                    boolean acceptsBid = bids.size() == 1 &&
                    myAuction.getAuctionItem().getMarketValue() * (0.2 + Math.pow(0.8, 2*myAuction.getNumRounds())) <= bids.get(0).getMarketValue();

                    if(bids.size() == 0 || acceptsBid) {
                        response = new ACLMessage(ACLMessage.AGREE);
                        response.addReceiver(Exchange.getExchange());
                        myAgent.send(response);

                        if (acceptsBid) {
                            response = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                            response.addReceiver(bids.get(0).getBidder());
                            response.setContentObject(new AuctionState(myAuction, getWantedItems(), bids.get(0)));
                            myAgent.send(response);

                            Item item = myAuction.getAuctionItem().getItem();
                            resourceDeficit.put(item, resourceDeficit.get(item) - myAuction.getAuctionItem().getAmount());
                            adjustResources(bids.get(0), 1);
                        }
                        myAuction = null;
                        nrRejected = 0;
                        nrBidders = 0;
                    } else {
                        Collections.sort(bids);
                        Bid bestBid = bids.get(bids.size() - 1);
                        myAuction = new AuctionState(myAuction, getWantedItems(), bestBid);
                        response = new ACLMessage(ACLMessage.INFORM);

                        for (Bid newBid : bids)
                            response.addReceiver(newBid.getBidder());

                        response.setContentObject(myAuction);
                        myAgent.send(response);
                    }

                    bids = new ArrayList<Bid>();
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean done() {
            if(Exchange.hasWinner()) return true;

            for(Item item: resourceDeficit.keySet())
                if(resourceDeficit.get(item) < 0) return false;
            Exchange.hasWon();

            System.out.println(getLocalName() + ": I am done!");
            System.out.println(getLocalName() + ": Resources deficit: " + resourceDeficit.entrySet());
            return true;
        }
    }


    private void adjustResources(Bid bid, int sign){
        for(Item item : bid.getItems().keySet())
            resourceDeficit.put(item, resourceDeficit.get(item) + sign*bid.getItems().get(item));

        coins += sign * bid.getCoins();
    }


    private Bid generateBid(AuctionState as){
        HashMap<Item, Integer> bid = new HashMap<Item, Integer>();
        int numToBuy = Math.min(as.getAuctionItem().getAmount(), -resourceDeficit.get(as.getAuctionItem().getItem()));
        int bidValue = (int) (numToBuy*as.getAuctionItem().getItem().getValue() * as.getNumRounds()/2.0);
        if(getTotalWealth() + as.getAuctionItem().getMarketValue() - bidValue < 0) return null;

        for(Item item: as.getWantedItems().keySet()){
            if(resourceDeficit.get(item) > 0){
                int nrOfWantedItems = as.getWantedItems().get(item);
                int toBid = (int) Math.min(Math.ceil(bidValue/item.getValue()), Math.min(nrOfWantedItems, resourceDeficit.get(item)));
                bidValue -= toBid * item.getValue();
                bid.put(item, toBid);
            }
        }

        if (bidValue <= 0) return new Bid(this.getAID(), bid, 0);
        else if(coins > bidValue) return new Bid(this.getAID(), bid, bidValue);
        else return null;
    }


    private HashMap<Item, Integer> getWantedItems() {
        HashMap<Item, Integer> wantedItems = new HashMap<Item, Integer>();

        for(Item item: resourceDeficit.keySet()) {
            if(resourceDeficit.get(item) >= 0) continue;
            wantedItems.put(item, -resourceDeficit.get(item));
        }
        return wantedItems;
    }


    private AuctionItem getItemForSale() {
        ArrayList<AuctionItem> forSale = new ArrayList<AuctionItem>();
        for (Item item : resourceDeficit.keySet()) {
            if (resourceDeficit.get(item) <= 0) continue;

            forSale.add(new AuctionItem(this.getAID(), item, resourceDeficit.get(item)));
        }
        if(forSale.size() == 0) return null;
        return forSale.get((int) (Math.random() * forSale.size()));
    }

    private int getTotalWealth() {
        int sum = 0;
        for(Item item: resourceDeficit.keySet())
            sum += item.getValue() * resourceDeficit.get(item);

        return sum + coins;
    }
}
