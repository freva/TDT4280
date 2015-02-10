package Excercise2;

import Excercise2.parser.Parser;
import Excercise2.parser.Node;
import jade.Boot;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;


public class TaskAdministrator extends jade.core.Agent {
    public static void main(String args[]) {
        Boot.main(new String[]{"-gui", "TA:Excercise2.TaskAdministrator;AA:Excercise2.agents.AdditionAgent;AA2:Excercise2.agents.AdditionAgent"});

    }


    protected void setup() {

        addBehaviour(new TickerBehaviour(this, 15000) {
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("Addition");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    System.out.println("Found the following addition agents:");
                    AID [] sellerAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        sellerAgents[i] = result[i].getName();
                        System.out.println(sellerAgents[i].getName());
                    }
                }
                catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        } );
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    Node tree = Parser.convertToPostfix(msg.getContent());

                    System.out.println(tree.toString());
                }

            }
        });
    }
}
