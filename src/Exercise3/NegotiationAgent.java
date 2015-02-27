package Exercise3;

import Exercise3.containers.AuctionItem;
import Exercise3.containers.AuctionState;
import Exercise3.containers.Bid;
import Exercise3.containers.Item;
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
    private HashMap<Item, Integer> resourceDeficit = new HashMap<Item, Integer>();
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


        ArrayList<AuctionItem> forSale = new ArrayList<AuctionItem>();
        for(Item item: resourceDeficit.keySet()){
            if(resourceDeficit.get(item) <= 0) continue;

            AuctionItem ai = new AuctionItem(this.getAID(), item, resourceDeficit.get(item));
            forSale.add(ai);
        }

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(Exchange.getExchange());
        try {
            msg.setContentObject(forSale);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.send(msg);

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
                        ai = (AuctionItem) msg.getContentObject();

                        if(ai == null) return;
                        ai.setAmount(resourceDeficit.get(ai.getItem()));

                        if(ai.getAmount() > 0) {
                            response = new ACLMessage(ACLMessage.CONFIRM);
                        } else {
                            response = new ACLMessage(ACLMessage.DISCONFIRM);
                        }

                        response.setContentObject(ai);
                        response.addReceiver(Exchange.getExchange());
                        myAgent.send(response);
                        break;

                    case ACLMessage.INFORM_REF:
                        ai = (AuctionItem) msg.getContentObject();
                        response = new ACLMessage(ACLMessage.INFORM_IF);

                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();
                        sd.setType("Trader");
                        template.addServices(sd);

                        DFAgentDescription result[] = DFService.search(myAgent, template);
                        for (DFAgentDescription aResult : result) {
                            if (! aResult.getName().equals(myAgent.getAID())) {
                                nrBidders++;
                                response.addReceiver(aResult.getName());
                            }
                        }

                        myAuction = new AuctionState(ai, getWantedItems());
                        response.setContentObject(myAuction);
                        myAgent.send(response);
                        break;

                    case ACLMessage.INFORM_IF:
                        as = (AuctionState) msg.getContentObject();
                        int quantityNeeded = -resourceDeficit.get(as.getAuctionItem().getItem());
                        if(quantityNeeded > 0) {
                            bid = generateBid(as);
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
                    myAuction.getAuctionItem().getMarketValue() * (0.2 + Math.pow(0.8, myAuction.getNumRounds())) <= bids.get(0).getMarketValue();

                    if(bids.size() == 1) {
                        Bid test = bids.get(0);
                        System.out.println("Round " + myAuction.getNumRounds() + ": " + test + " " + myAuction.getAuctionItem());
                        System.out.println(test.getMarketValue() + " " + myAuction.getAuctionItem().getMarketValue());
                        System.out.println((myAuction.getAuctionItem().getMarketValue() * (1-Math.pow(0.8, myAuction.getNumRounds()+1))) +
                                " " + myAuction.getAuctionItem().getMarketValue() * (0.2 + Math.pow(0.8, myAuction.getNumRounds())));
                    }

                    if(bids.size() == 0 || acceptsBid) {
                        response = new ACLMessage(ACLMessage.AGREE);
                        response.addReceiver(Exchange.getExchange());
                        myAgent.send(response);

                        if (bids.size() == 0) {
                            ArrayList<AuctionItem> resaleItem = new ArrayList<AuctionItem>();
                            resaleItem.add(myAuction.getAuctionItem());
                            response = new ACLMessage(ACLMessage.INFORM);
                            response.addReceiver(Exchange.getExchange());
                            response.setContentObject(resaleItem);
                            myAgent.send(response);
                        } else {
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
                        response = new ACLMessage(ACLMessage.INFORM_IF);

                        for (Bid newBid : bids)
                            response.addReceiver(newBid.getBidder());

                        response.setContentObject(myAuction);
                        myAgent.send(response);
                    }

                    bids = new ArrayList<Bid>();
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            } catch (FIPAException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean done() {
            for(Item item: resourceDeficit.keySet())
                if(resourceDeficit.get(item) < 0) return false;

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
        //int numToBuy = Math.min(as.getAuctionItem().getAmount(), -spareQuantity(as.getAuctionItem().getItem()));
        int bidValue = (int) (as.getAuctionItem().getMarketValue() * (1-Math.pow(0.8, as.getNumRounds()+1)));

        for(Item item: as.getWantedItems().keySet()){
            if(resourceDeficit.get(item) > 0){
                int nrOfWantedItems = as.getWantedItems().get(item);
                int toBid = (int) Math.min(Math.ceil(bidValue/item.getValue()), Math.min(nrOfWantedItems, resourceDeficit.get(item)));
                bidValue -= toBid * item.getValue();
                bid.put(item, toBid);
            }
        }

        if (bidValue <= 0) return new Bid(this.getAID(), bid, 0);
        return new Bid(this.getAID(), bid, Math.min(bidValue, coins));
    }


    private HashMap<Item, Integer> getWantedItems() {
        HashMap<Item, Integer> wantedItems = new HashMap<Item, Integer>();

        for(Item item: wantedItems.keySet()) {
            if(resourceDeficit.get(item) >= 0) continue;
            wantedItems.put(item, -resourceDeficit.get(item));
        }
        return wantedItems;
    }
}
