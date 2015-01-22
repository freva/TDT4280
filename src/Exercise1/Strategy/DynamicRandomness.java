package Exercise1.Strategy;

import Exercise1.Player;
import java.util.List;

public class DynamicRandomness extends Player {
    public DynamicRandomness() {
        super("Dynamic Randomness");
    }


    @Override
    public Action dilemma(List<Action> opponentPreviousActions) {
        if(opponentPreviousActions.size() == 0) return Action.COOPERATE;

        int numCoop = 0;
        for(Action a: opponentPreviousActions)
            if(a == Action.COOPERATE)
                numCoop++;

        return (Math.random() > ((float) numCoop)/opponentPreviousActions.size()) ? Action.DEFECT : Action.COOPERATE;
    }
}
