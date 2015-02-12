package Exercise2.agents;

import Exercise2.parser.Node;
import Exercise2.parser.Operator;


public class Addition extends Arithmetic {
    protected void setup() {
        super.setup(Operator.ADD);
    }


    protected double doCalculation(Node job) {
        return job.getChildren().get(0).getValue() + job.getChildren().get(1).getValue();
    }
}
