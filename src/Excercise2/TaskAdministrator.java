package Excercise2;

import Excercise2.auction.FirstPriceSealedBid;
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


public class TaskAdministrator extends jade.core.Agent {
    public static void main(String args[]) {
            String agents = "TA:Excercise2.TaskAdministrator;" +
                            "AA:Excercise2.agents.Addition;" +
                            "AA2:Excercise2.agents.Addition;" +
                            "AS:Excercise2.agents.Subtraction;" +
                            "AS2:Excercise2.agents.Subtraction;" +
                            "AD:Excercise2.agents.Division;" +
                            "AD2:Excercise2.agents.Division;" +
                            "AM:Excercise2.agents.Multiplication;" +
                            "AM2:Excercise2.agents.Multiplication";
        Boot.main(new String[]{"-gui", agents});
    }
    
    
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            private MessageTemplate mt = MessageTemplate.MatchConversationId("GUI");

            public void action() {
                ACLMessage msg = receive(mt);
                if(msg != null) {
                    addBehaviour(new CalculateExpression(msg.getContent()));
                }
            }
        });
    }


    private Node depthFirstComputableSearch(Node root) {
        if(root.isComputable()) {
            root.setProcessing(true);
            return root;
        } else {
            for(Node child: root.getChildren()) {
                Node candidate = depthFirstComputableSearch(child);
                if(candidate != null) return candidate;
            }
        }

        return null;
    }


    class CalculateExpression extends Behaviour {
        private Node root;
        private String expression;
        private boolean done = false;

        public CalculateExpression(String expression) {
            this.root = Parser.convertToPostfix(expression);
            this.expression = expression;
        }


        @Override
        public void action() {
            if(root.isFinished()) {
                System.out.println("The answer to " + expression + " is " + root.getValue());
                done = true;
            }


            Node next = depthFirstComputableSearch(root);
            if(next == null) return;

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(next.getOperator().name());
            template.addServices(sd);
            try {
                DFAgentDescription result[] = DFService.search(myAgent, template);
                AID sellerAgents[] = new AID[result.length];
                for (int i = 0; i < result.length; ++i)
                    sellerAgents[i] = result[i].getName();

                myAgent.addBehaviour(new FirstPriceSealedBid(sellerAgents, next));
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }

        @Override
        public boolean done() {
            return done;
        }
    }
}
