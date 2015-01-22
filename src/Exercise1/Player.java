package Exercise1;

import java.util.ArrayList;

public abstract class Player implements Agent {
    private ArrayList<Action> prevActions = new ArrayList<Action>();
    private String name;
    private int mScore, numGames, numOpponents;
    private float totScore;

    public Player(String name) {
        this.name = name;
    }


    public void registerMove(Action myMove, Action otherMove) {
        prevActions.add(myMove);
        mScore += getScore(myMove, otherMove);
        numGames++;
    }


    public void finishRound() {
        prevActions.clear();
        totScore += mScore/ numGames;
        numOpponents++;
        mScore = 0;
        numGames = 0;
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


    public ArrayList<Action> getPreviousActions() {
        return prevActions;
    }

    public float getFScore() {
        return totScore/numOpponents;
    }


    public String getName() {
        return name;
    }
}
