package Exercise1;

import Exercise1.Strategy.*;
import java.util.ArrayList;

public class Tournament {
    private static ArrayList<Player> players = new ArrayList<Player>();
    private static final int numberOfRounds = 100;


    public static void main(String[] args) {
        players.add(new AlwaysCooperate());
        players.add(new AlwaysDefect());
        players.add(new TitForTat());
        players.add(new TitForEveryOtherTat());
        players.add(new Mixture());
        players.add(new DynamicRandomness());


        for(int i=0; i<players.size()-1; i++) {
            for(int j=i+1; j<players.size(); j++) {
                Player player1 = players.get(i);
                Player player2 = players.get(j);

                for(int k=0; k<numberOfRounds; k++) {
                    Agent.Action player1Action = player1.dilemma(player2.getPreviousActions());
                    Agent.Action player2Action = player2.dilemma(player1.getPreviousActions());

                    player1.registerMove(player1Action, player2Action);
                    player2.registerMove(player2Action, player1Action);
                }

                player1.finishRound();
                player2.finishRound();
            }
        }


        for(Player p: players)
            System.out.println(p.getName() + ": " + p.getFScore());
    }
}