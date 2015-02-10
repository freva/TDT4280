package Excercise2.agents;

import Excercise2.parser.Node;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.UnreadableException;

public class AdditionAgent extends Agent {
    protected void setup() {

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Addition");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
//        addBehaviour(new CyclicBehaviour(this) {
//            public void action() {
//                try {
//                    Node msg = (Node) receive().getContentObject();
//                } catch (UnreadableException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }
}
