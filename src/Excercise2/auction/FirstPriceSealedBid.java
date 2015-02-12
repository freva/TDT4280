package Excercise2.auction;

import Excercise2.parser.Node;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;

public class FirstPriceSealedBid extends Behaviour {
    private AID[] agents;
    private Node toCompute;
    private int step = 0, replyCounter = 0, bestOffer = Integer.MAX_VALUE;
    private MessageTemplate mt;
    private AID bestAgent;

    public FirstPriceSealedBid(AID[] agents, Node toCompute) {
        this.agents = agents;
        this.toCompute = toCompute;
    }

    @Override
    public void action() {
        switch (step) {
            case 0:
                ACLMessage message = new ACLMessage(ACLMessage.CFP);
                for(AID a: agents) {
                    message.addReceiver(a);
                }

                try {
                    message.setContentObject(toCompute);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                message.setConversationId("auction-start-" + toCompute.getOperator().name());
                message.setReplyWith("offer" + toCompute.getOperator().name() + System.currentTimeMillis());
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId(message.getConversationId()),
                        MessageTemplate.MatchInReplyTo(message.getReplyWith()));
                step = 1;
                myAgent.send(message);
                break;

            case 1:
                ACLMessage reply = myAgent.receive(mt);
                if(reply != null) {
                    if(reply.getPerformative() == ACLMessage.PROPOSE) {
                        int proposal = Integer.parseInt(reply.getContent());
                        if(proposal < bestOffer) {
                            bestOffer = proposal;
                            bestAgent = reply.getSender();
                        }
                    }

                    if(++replyCounter >= agents.length) step = 2;
                } else block();
                break;

            case 2:
                ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                order.addReceiver(bestAgent);
                try {
                    order.setContentObject(toCompute);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                order.setConversationId("order-" + toCompute.getOperator().name());
                order.setReplyWith("confirmation" + toCompute.getOperator().name() + System.currentTimeMillis());
                myAgent.send(order);

                mt = MessageTemplate.and(MessageTemplate.MatchConversationId(order.getConversationId()), MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                step = 3;
                break;

            case 3:
                reply = myAgent.receive(mt);
                if(reply != null) {
                    if(reply.getPerformative() == ACLMessage.INFORM) {
                        double answer = Double.parseDouble(reply.getContent());
                        toCompute.setValue(answer);
                        step = 4;
                    }
                } else block();
        }
    }

    @Override
    public boolean done() {
        return ((step == 2 && bestAgent == null) || step == 4);
    }
}
