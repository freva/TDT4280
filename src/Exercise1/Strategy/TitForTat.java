package Exercise1.Strategy;

import Exercise1.Player;

import java.util.List;

public class TitForTat extends Player {
    public TitForTat() {
        super("TIT-for-TAT");
    }


    @Override
    public Action dilemma(List<Action> opponentPreviousActions) {
        if(opponentPreviousActions.size() == 0) return Action.COOPERATE;
        else return opponentPreviousActions.get(opponentPreviousActions.size()-1);
    }
}
