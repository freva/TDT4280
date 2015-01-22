package Exercise1;

import java.util.ArrayList;

public class Tournament {
    private static ArrayList<Player> players = new ArrayList<Player>();


    public static void main(String[] args) {
        int numberOfRounds = Integer.parseInt(args[0]);

        players.add(new AlwaysCooperate("Always Cooperate"));
        players.add(new AlwaysDefect("Always Defect"));
        players.add(new TitForTat("TIT-for-TAT"));
        players.add(new TitForEveryOtherTat("TIT-for-every-other-TAT"));


        for(int i=0; i<players.size()-1; i++) {
            for(int j=i+1; j<players.size(); j++) {
                Player player1 = players.get(i);
                Player player2 = players.get(j);

                for(int k=0; k<numberOfRounds; k++) {
                    Agent.Action player1Action = player1.getMove();
                    Agent.Action player2Action = player2.getMove();

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