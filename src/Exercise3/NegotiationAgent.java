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
    private HashMap<Item, Integer> ownedResources = new HashMap<Item, Integer>();
    private HashMap<Item, Integer> wantedResources = new HashMap<Item, Integer>();
    private int coins = 1000;

    private static final String BID_THREAD = "bid-thread";


    protected void setup() {
        int id = Integer.parseInt((String) getArguments()[0]);
        setResources(id);
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
        for(Item item: ownedResources.keySet()){
            if(ownedResources.get(item) <= wantedResources.get(item)) continue;

            AuctionItem ai = new AuctionItem(this.getAID(), item, ownedResources.get(item) - wantedResources.get(item));
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
    }


    class SaleStuff extends Behaviour {
        @Override
        public void action() {
            ACLMessage response, msg = receive();
            AuctionItem ai;

            try {
                switch (msg.getPerformative()) {
                    case ACLMessage.QUERY_IF:
                        ai = (AuctionItem) msg.getContentObject();

                        if(ai == null) return;
                        ai.setAmount(ownedResources.get(ai.getItem()) - wantedResources.get(ai.getItem()));

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
                            if (aResult.getName() != myAgent.getAID()) {
                                response.addReceiver(aResult.getName());
                            }
                        }

                        response.setConversationId(BID_THREAD);
                        response.setReplyWith(BID_THREAD + System.currentTimeMillis());
                        response.setContentObject(new AuctionState(ai, getWantedItems(), new Bid()));
                        myAgent.send(response);
                        break;

                    case ACLMessage.INFORM_IF:
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
            return false;
        }
    }


    private HashMap<Item, Integer> getWantedItems() {
        HashMap<Item, Integer> wantedItems = new HashMap<Item, Integer>();

        for(Item item: wantedItems.keySet()) {
            if(ownedResources.get(item) >= wantedItems.get(item)) continue;
            wantedItems.put(item, ownedResources.get(item) - wantedResources.get(item));
        }
        return wantedItems;
    }


    private void setResources(int id){
        HashMap<Item, Integer> test = new HashMap<Item, Integer>();
        for(Item item: Item.values()) {
            ownedResources.put(item, (int) (200 * Math.random() + 100));
            wantedResources.put(item, (int) (Main.numAgents*100/Math.pow(2, (id + item.getId())%Main.numAgents)*Math.random())+60);
            test.put(item, ownedResources.get(item) - wantedResources.get(item));
        }

        System.out.println(getLocalName() + " wants resources: " + test.entrySet());
    }
}
