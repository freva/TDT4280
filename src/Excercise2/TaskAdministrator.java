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
import sun.plugin2.message.Message;

import java.io.IOException;


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
                        System.out.println("Is not computable: " + tree);
                        Node next = depthFirstComputableSearch(tree);
                        Node answer = computeNext(next);
                        if(next.getParent() != null) next.getParent().replace(next, answer);
                        else tree = answer;
                    }
                    System.out.println("The answer is: " + tree.getValue());
                }
            }
        });
    }


    private Node computeNext(Node next) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(next.getOperator().name());
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            AID [] sellerAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                sellerAgents[i] = result[i].getName();
            }

            this.addBehaviour(new FirstPriceSealedBidAuction(sellerAgents, next));
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        return new Node(next.getParent(), "9");
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
        private int step = 0;
        private MessageTemplate mt;

        FirstPriceSealedBidAuction(AID[] agents, Node toCompute) {
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

                    message.setConversationId("biddingOffer");
                    message.setReplyWith("offer" + System.currentTimeMillis());
                    myAgent.send(message);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("biddingOffer"), MessageTemplate.MatchInReplyTo(message.getReplyWith()));
                    step = 1;
                    break;

                case 1:
            }

        }

        @Override
        public boolean done() {
            return false;
        }
    }
}
