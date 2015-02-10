package Excercise2.parser;

public enum Operator {
    DIVIDE('/'), MULTIPLY('*'), ADD('+'), SUBTRACT('-');

    private char opVal;
    Operator(char opVal) {
        this.opVal = opVal;
    }

    public char getOperator() {
        return opVal;
    }
}
