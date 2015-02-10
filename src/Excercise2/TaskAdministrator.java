package Excercise2;

import Excercise2.parser.Parser;
import Excercise2.parser.Node;
import jade.Boot;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.Arrays;


public class TaskAdministrator extends jade.core.Agent {
    public static void main(String args[]) {
        Boot.main(new String[]{"-gui", "TA:Excercise2.TaskAdministrator;AA:Excercise2.agents.AdditionAgent;AA2:Excercise2.agents.AdditionAgent"});

    }


    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    Node tree = Parser.convertToPostfix(msg.getContent());

                    while(tree.getOperator() != null) {
                    //    System.out.println("Is not computable: " + tree);
                        Node next = depthFirstComputableSearch(tree);

                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();
                        sd.setType(next.getOperator().name());
                        template.addServices(sd);
                        try {
                            DFAgentDescription[] result = DFService.search(myAgent, template);
                            AID [] sellerAgents = new AID[result.length];
                            for (int i = 0; i < result.length; ++i)
                                sellerAgents[i] = result[i].getName();

                            myAgent.addBehaviour(new FirstPriceSealedBidAuction(sellerAgents, next));
                        } catch (FIPAException fe) {
                            fe.printStackTrace();
                        }
                    }
                    System.out.println("The answer is: " + tree.getValue());
                }
            }
        });
    }


    private Node depthFirstComputableSearch(Node root) {
        if(root.isComputable()) return root;
        else {
            for(Node child: root.getChildren()) {
                Node candidate = depthFirstComputableSearch(child);
                if(candidate != null) return candidate;
            }
        }

        return null;
    }


    class FirstPriceSealedBidAuction extends Behaviour {
        private AID[] agents;
        private Node toCompute;
        private int step = 0, replyCounter = 0, bestOffer = Integer.MAX_VALUE;
        private MessageTemplate mt;
        private AID bestAgent;

        FirstPriceSealedBidAuction(AID[] agents, Node toCompute) {
            this.agents = agents;
            this.toCompute = toCompute;
        }

        @Override
        public void action() {
            System.out.println("Action called!");
            switch (step) {
                case 0:
                    System.out.println("Starting auction");
                    ACLMessage message = new ACLMessage(ACLMessage.CFP);
                    for(AID a: agents) {
                        message.addReceiver(a);
                    }

                    try {
                        message.setContentObject(toCompute);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    message.setConversationId("biddingOffer");
                    message.setReplyWith("offer" + System.currentTimeMillis());
                    myAgent.send(message);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("biddingOffer"), MessageTemplate.MatchInReplyTo(message.getReplyWith()));
                    step = 1;
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
                        System.out.println("Received proposal");

                        if(++replyCounter >= agents.length) step = 2;
                    }
                    break;

                case 2:
                    System.out.println("Accepted a proposal with offer " +bestOffer + " from " + bestAgent);
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestAgent);
                    try {
                        order.setContentObject(toCompute);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    order.setConversationId("order" + System.currentTimeMillis());
                    order.setReplyWith("confirmation" + System.currentTimeMillis());
                    myAgent.send(order);

                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("order"), MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;

                case 3:
                    System.out.println("Waiting for reply");
                    reply = myAgent.receive(mt);
                    if(reply != null) {
                        if(reply.getPerformative() == ACLMessage.INFORM) {
                            double answer = Double.parseDouble(reply.getContent());
                            toCompute.setValue(answer);

                            System.out.println(answer);
                            System.out.println(toCompute);
                            System.exit(0);
                            myAgent.doDelete();
                        }
                    }
            }

        }

        @Override
        public boolean done() {
            return false;
        }
    }
}
