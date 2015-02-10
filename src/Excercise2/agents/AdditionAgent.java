package Excercise2.agents;

import Excercise2.parser.Node;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.UnreadableException;

public class AdditionAgent extends Agent {
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                try {
                    Node msg = (Node) receive().getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
