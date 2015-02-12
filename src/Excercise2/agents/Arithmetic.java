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


public abstract class Arithmetic extends Agent {
    private long busyUntil = 0L;

    protected void setup(Operator op) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(op.name());
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new NegotiateJob());
        addBehaviour(new DoJob());
    }


    private int estimateTime(Node job) {
        int timeOnJob = 0;

        for(Node child: job.getChildren()) {
            timeOnJob += (int) (Math.log10(Math.abs(child.getValue())) + Math.random() * 100);
        }
        return Math.max(timeOnJob, (int) (busyUntil-System.currentTimeMillis()) + timeOnJob);
    }


    protected abstract double doCalculation(Node job);


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
            } else block();
        }
    }


    class DoJob extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage message = myAgent.receive(mt);

            if(message != null) {
                try {
                    Node job = (Node) message.getContentObject();
                    int timeOnJob = estimateTime(job);
                    busyUntil = Math.max(busyUntil+timeOnJob, System.currentTimeMillis()+timeOnJob);
                    myAgent.doWait(timeOnJob);

                    ACLMessage replyMessage = message.createReply();
                    replyMessage.setPerformative(ACLMessage.INFORM);
                    replyMessage.setContent("" + doCalculation(job));
                    myAgent.send(replyMessage);
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            } else block();
        }
    }
}
