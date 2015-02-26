package Exercise3;

import Exercise3.containers.AuctionItem;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;

public class Exchange extends Agent {
    private ArrayList<AuctionItem> auctionQueue = new ArrayList<AuctionItem>();
    private boolean isRunning = false;
    private static AID exchange;


    protected void setup() {
        exchange = this.getAID();
        addBehaviour(new AuctionAdministrator());
    }


    private void checkNextAuction() throws IOException {
        if(auctionQueue.size() == 0) return;

        AuctionItem ai = auctionQueue.remove((int) (auctionQueue.size()*Math.random()));
        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
        msg.addReceiver(ai.getOwner());
        msg.setContentObject(ai);
        this.send(msg);
    }


    public static AID getExchange() {
        return exchange;
    }


    private class AuctionAdministrator extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = receive();
            ACLMessage response;

            try {
                switch (msg.getPerformative()) {
                    case ACLMessage.INFORM:
                        ArrayList<AuctionItem> auctionItems = (ArrayList<AuctionItem>) msg.getContentObject();
                        auctionQueue.addAll(auctionItems);

                        if (!isRunning) {
                            checkNextAuction();
                            isRunning = true;
                        }
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
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
