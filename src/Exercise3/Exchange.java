package Exercise3;

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
import java.io.Serializable;
import java.util.ArrayList;

public class Exchange extends Agent {
    private ArrayList<AuctionItem> auctionQueue = new ArrayList<AuctionItem>();
    private static final String AUCTION_CONFIRM_ID = "auction-confirm";
    private static final String AUCTION_START = "auction-start";


    protected void setup() {
        addBehaviour(new AuctionAdministrator());
    }


    private void checkNextAuction() {
        AuctionItem ai = auctionQueue.remove(0);
        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
        msg.addReceiver(ai.owner);
        try {
            msg.setContentObject(ai);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.setConversationId(AUCTION_CONFIRM_ID);
        this.send(msg);
    }


    private void announceNewAuction(AuctionItem ai) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Trader");
        template.addServices(sd);
        try {
            DFAgentDescription result[] = DFService.search(this, template);
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);

            for (DFAgentDescription aResult : result) {
                if (aResult.getName() != ai.owner) {
                    msg.addReceiver(aResult.getName());
                }
            }
            msg.setContentObject(ai);
            msg.setConversationId(AUCTION_START);
            this.send(msg);


            if (! msg.getAllReceiver().hasNext())
                throw new IllegalArgumentException("Could not find any agent to trade with");
        } catch (FIPAException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class AuctionAdministrator extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = receive();
            AuctionItem ai = null;

            try {
                ai = (AuctionItem) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            switch (msg.getPerformative()) {
                case ACLMessage.INFORM:
                    auctionQueue.add(ai);
                    break;

                case ACLMessage.CONFIRM:
                    announceNewAuction(ai);
                    break;

                case ACLMessage.DISCONFIRM:
                    checkNextAuction();
                    break;

                case ACLMessage.AGREE:
                    checkNextAuction();
                    break;
            }
        }
    }

    public class AuctionItem implements Serializable {
        private AID owner;
        private Item item;
        private int amount;

        public AuctionItem(AID owner, Item item, int amount) {
            this.owner = owner;
            this.item = item;
            this.amount = amount;
        }
    }
}
