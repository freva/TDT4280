package Exercise1.Strategy;

import Exercise1.Player;

import java.util.List;

public class AlwaysDefect extends Player {
    public AlwaysDefect() {
        super("Always Defect");
    }


    @Override
    public Action dilemma(List<Action> opponentPreviousActions) {
        return Action.DEFECT;
    }
}
