package Excercise2.parser;

import java.util.Stack;
import java.util.StringTokenizer;

public class Parser {
    private static boolean isOperator(char c) {
        return c == '+'  ||  c == '-'  ||  c == '*'  ||  c == '/' || c=='(' || c==')';
    }


    private static boolean isSpace(char c) {
        return (c == ' ');
    }


    private static boolean lowerPrecedence(char op1, char op2) {
        switch (op1) {
            case '+':
            case '-':
                return !(op2=='+' || op2=='-') ;

            case '*':
            case '/':
                return op2=='^' || op2=='(';

            case '^':
                return op2=='(';

            case '(': return true;

            default:
                return false;
        }
    }


    public static Node convertToPostfix(String infix) {
        Stack<String> operatorStack = new Stack<String>();

        StringTokenizer parser = new StringTokenizer(infix, "+-*/^() ", true);
        StringBuilder postfix = new StringBuilder(infix.length());

        // Process the tokens.
        while (parser.hasMoreTokens()) {
            String token = parser.nextToken();
            char c = token.charAt(0);

            if ( (token.length() == 1) && isOperator(c) ) {
                while (!operatorStack.empty() && !lowerPrecedence((operatorStack.peek()).charAt(0), c))
                    postfix.append(" ").append(operatorStack.pop());

                if (c==')') {
                    String operator = operatorStack.pop();
                    while (operator.charAt(0)!='(') {
                        postfix.append(" ").append(operator);
                        operator = operatorStack.pop();
                    }
                } else
                    operatorStack.push(token);// Push this operator onto the stack.

            } else if ( !((token.length() == 1) && isSpace(c)) ) {
                postfix.append(" ").append(token);  // output the operand
            }
        }

        while (!operatorStack.empty())
            postfix.append(" ").append(operatorStack.pop());

        return fromStackToTree(postfix.toString().substring(1).split(" "));
    }


    private static Node fromStackToTree(String[] stack) {
        Node root = new Node(null, stack[stack.length-1]);

        Node current = root;
        for(int i=stack.length-2; i>=0; i--) {
            while(current.getChildren().size() == 2) current = current.getParent();
            Node next = new Node(current, stack[i]);
            if(next.getOperator() != null) current = next;
        }
        return root;
    }
}
