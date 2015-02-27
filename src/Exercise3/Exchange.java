package Exercise3;

import Exercise3.containers.AuctionItem;
import Exercise3.containers.Item;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Exchange extends Agent {
    private static ArrayList<HashMap<Item, Integer>> resourceDistributions;
    private static ArrayList<AuctionItem> auctionQueue = new ArrayList<AuctionItem>();
    private static AID exchange;
    private static AID[] traders;
    private static Agent myAgent;
    private static boolean hasWinner = false;


    protected void setup() {
        myAgent = this;
        exchange = this.getAID();
        resourceDistributions = ResourceManager.getDistributions(Integer.parseInt((String) getArguments()[0]));
        addBehaviour(new AuctionAdministrator());
    }


    private static void checkNextAuction() throws IOException {
        if(auctionQueue.size() == 0) return;

        AuctionItem ai = auctionQueue.remove((int) (auctionQueue.size()*Math.random()));
        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
        msg.addReceiver(ai.getOwner());
        msg.setContentObject(ai);
        myAgent.send(msg);
    }


    public static AID getExchange() {
        return exchange;
    }


    public static boolean hasWinner() {
        return hasWinner;
    }


    public static void hasWon() {
        hasWinner = true;
    }


    public static HashMap<Item, Integer> getResourceDistribution() {
        return resourceDistributions.remove(0);
    }


    public static AID[] getTraders() {
        if(traders == null) {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Trader");
            template.addServices(sd);
            DFAgentDescription result[];
            try {
                result = DFService.search(myAgent, template);
                traders = new AID[result.length];

                for (int i = 0; i < result.length; i++)
                    traders[i] = result[i].getName();
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }

        return traders;
    }


    private class AuctionAdministrator extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            ACLMessage response;

            try {
                if(msg == null) return;
                switch (msg.getPerformative()) {
                    case ACLMessage.INFORM:
                        ArrayList<AuctionItem> auctionItems = (ArrayList<AuctionItem>) msg.getContentObject();
                        auctionQueue.addAll(auctionItems);
                        break;

                    case ACLMessage.CONFIRM:
                        AuctionItem ai = (AuctionItem) msg.getContentObject();

                        response = new ACLMessage(ACLMessage.INFORM_REF);
                        response.setContentObject(ai);
                        response.addReceiver(ai.getOwner());
                        myAgent.send(response);
                        break;

                    case ACLMessage.DISCONFIRM:
                        checkNextAuction();
                        break;

                    case ACLMessage.AGREE:
                        checkNextAuction();
                        break;

                    case ACLMessage.CFP:
                        checkNextAuction();
                        break;

                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
