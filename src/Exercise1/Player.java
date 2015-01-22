package Exercise1;

import java.util.ArrayList;

public abstract class Player implements Agent {
    private ArrayList<Action> prevActions = new ArrayList<Action>();
    private String name;
    private int mScore, games;
    private float totScore;

    public Player(String name) {
        this.name = name;
    }


    public void registerMove(Action myMove, Action otherMove) {
        prevActions.add(myMove);
        mScore += getScore(myMove, otherMove);
        games++;
    }


    public void finishRound() {
        totScore += mScore/games;
        mScore = 0;
        games = 0;
    }


    public static int getScore(Action myAction, Action otherAction) {
        if(myAction == Action.COOPERATE && otherAction == Action.COOPERATE) {
            return 3;
        } else if(myAction == Action.COOPERATE && otherAction == Action.DEFECT) {
            return 0;
        } else if(myAction == Action.DEFECT && otherAction == Action.COOPERATE) {
            return 5;
        } else {
            return 2;
        }
    }


    public Action getMove() {
        return dilemma(prevActions);
    }


    public float getFScore() {
        return totScore/5;
    }


    public String getName() {
        return name;
    }
}
