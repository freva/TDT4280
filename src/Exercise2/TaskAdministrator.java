package Exercise2;

import Exercise2.auction.FirstPriceSealedBid;
import Exercise2.parser.Parser;
import Exercise2.parser.Node;
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

                if(sellerAgents.length == 0)
                    throw new IllegalArgumentException("Could not find any agent that could execute " + next.getOperator().name());
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
