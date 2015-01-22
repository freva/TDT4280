package Exercise1.Strategy;


import Exercise1.Player;

import java.util.List;

public class AlwaysCooperate extends Player {
    public AlwaysCooperate() {
        super("Always Cooperate");
    }


    @Override
    public Action dilemma(List<Action> opponentPreviousActions) {
        return Action.COOPERATE;
    }
}
