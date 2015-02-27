package Exercise3;

import jade.Boot;

import java.util.Arrays;

public class Main {
    public static final int numAgents = 5;

    public static void main(String[] args) {
        String boot[] = new String[]{"-gui", "Ex:Exercise3.Exchange(" + numAgents + ")"};

        for(int i=0; i<numAgents; i++) {
            boot[boot.length-1] += ";NG" + i + ":Exercise3.NegotiationAgent";
        }

        System.out.println(Arrays.toString(boot));
        Boot.main(boot);
    }
}
