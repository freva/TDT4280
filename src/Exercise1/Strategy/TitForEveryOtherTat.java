package Exercise1.Strategy;

import Exercise1.Player;

import java.util.List;

public class TitForEveryOtherTat extends Player {
    public TitForEveryOtherTat() {
        super("TIT-for-every-other-TAT");
    }


    @Override
    public Action dilemma(List<Action> opponentPreviousActions) {
        int size = opponentPreviousActions.size();
        if(opponentPreviousActions.size() < 2 || opponentPreviousActions.get(size-1) != Action.DEFECT || opponentPreviousActions.get(size-2) != Action.DEFECT) return Action.COOPERATE;
        else return Action.DEFECT;
    }
}
