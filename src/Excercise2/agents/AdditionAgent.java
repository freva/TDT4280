package Excercise2.agents;

import Excercise2.parser.Node;
import Excercise2.parser.Operator;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


public class AdditionAgent extends Agent {
    private long busyUntil = 0L;


    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Operator.ADD.name());
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new NegotiateJob());
    }


    private int estimateTime(Node job) {
        int timeOnJob = 0;

        for(Node child: job.getChildren()) {
            timeOnJob = (int) (Math.log10(child.getValue() + Math.random()) * 100);
        }
        return Math.max(timeOnJob, (int) (busyUntil-System.currentTimeMillis()) + timeOnJob);
    }


    class NegotiateJob extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage message = myAgent.receive(mt);

            if(message != null) {
                try {
                    Node job = (Node) message.getContentObject();
                    int timeOnJob = estimateTime(job);

                    ACLMessage replyMessage = message.createReply();
                    replyMessage.setPerformative(ACLMessage.PROPOSE);
                    replyMessage.setContent("" + timeOnJob);
                    myAgent.send(replyMessage);
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
