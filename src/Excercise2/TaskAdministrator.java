package Excercise2;

import Excercise2.parser.InfixToPostfix;


public class TaskAdministrator extends jade.core.Agent {
    public static void main(String args[]) {
        //Boot.main(new String[]{"-gui", "TA:Excercise2.TaskAdministrator()"});

        String input = "7-2+5*2";
        System.out.println("Postfix is " + InfixToPostfix.convertToPostfix(input) + '\n');
    }



}
