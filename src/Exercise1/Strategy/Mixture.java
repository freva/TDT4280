package Exercise1.Strategy;

import Exercise1.Player;
import java.util.List;

public class Mixture extends Player {
    public Mixture(String s) {
        super(s);
    }


    @Override
    public Action dilemma(List<Action> opponentPreviousActions) {
        if(Math.random() > 0.3) return Action.DEFECT;
        else return Action.COOPERATE;
    }
}
