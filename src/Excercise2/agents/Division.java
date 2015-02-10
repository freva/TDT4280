package Excercise2.agents;

import Excercise2.parser.Node;
import Excercise2.parser.Operator;


public class Division extends Arithmetic {
    protected void setup() {
        super.setup(Operator.DIVIDE);
    }


    protected double doCalculation(Node job) {
        return job.getChildren().get(0).getValue() / job.getChildren().get(1).getValue();
    }
}
