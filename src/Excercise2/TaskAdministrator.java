package Excercise2;

import Excercise2.parser.Parser;
import Excercise2.parser.Node;
import jade.Boot;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;


public class TaskAdministrator extends jade.core.Agent {
    public static void main(String args[]) {
        Boot.main(new String[]{"-gui", "TA:Excercise2.TaskAdministrator()"});

    }


    protected void setup() {
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
