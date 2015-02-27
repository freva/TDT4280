package Exercise3;

import Exercise3.containers.Item;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.HashMap;


public class Exchange extends Agent {
    private static ArrayList<HashMap<Item, Integer>> resourceDistributions;
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


    private static void checkNextAuction() {
        AID[] traders = getTraders();
        AID trader = traders[(int) (Math.random()*traders.length)];

        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
        msg.addReceiver(trader);
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
        if(resourceDistributions.size() == 1) checkNextAuction();
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


    private class AuctionAdministrator extends Behaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();

            if(msg == null) return;
            switch (msg.getPerformative()) {
                case ACLMessage.DISCONFIRM:
                case ACLMessage.AGREE:
                case ACLMessage.CFP:
                    checkNextAuction();
                    break;
            }
        }

        @Override
        public boolean done() {
            return hasWinner();
        }
    }
}
