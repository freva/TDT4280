package Exercise3;

import jade.Boot;

import java.util.Arrays;

public class Main {
    public static final int numAgents = 3;

    public static void main(String[] args) {
        String boot[] = new String[]{"-gui", "Ex:Exchange"};

        for(int i=0; i<numAgents; i++) {
            boot[1] += ";NG" + i + ":Exercise3.NegotiationAgent(" + i + ")";
        }

        System.out.println(Arrays.toString(boot));
        Boot.main(boot);
    }
}
