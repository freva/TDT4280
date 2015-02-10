package Excercise2.parser;

import Excercise2.TaskAdministrator;

import java.io.Serializable;
import java.util.ArrayList;


public class Node implements Serializable {
    private double value;
    private Operator operator;
    private ArrayList<Node> children = new ArrayList<Node>();
    private Node parent;
    private boolean processing = false;


    public Node(Node parent, String value){
        this.parent = parent;
        getContent(value);
        if(parent != null) parent.addChild(this);
    }

    private void getContent(String value){
        String legalOP = "+-/*";
        if(!legalOP.contains(value)) {
            setValue(Double.valueOf(value));
        } else {
            char op = value.charAt(0);
            for (Operator operator : Operator.values()) {
                if (op == operator.getOperator()){
                    this.operator = operator;
                    return;
                }
            }
            throw new IllegalArgumentException("Not op");
        }
    }

    public void addChild(Node child){
        children.add(0, child);
    }

    public ArrayList<Node> getChildren(){
        return children;
    }

    public Operator getOperator(){
        return operator;
    }

    public Node getParent(){
        return parent;
    }

    public boolean isComputable() {
        boolean computable = !processing & children.size() > 0;
        for(Node child: children)
            computable &= child.getOperator() == null;

        return computable;
    }

    public double getValue(){
        return value;
    }

    public void setValue(double value) {
        this.value = value;
        this.operator = null;
        this.processing = false;
        children.clear();
        TaskAdministrator.ta.doWake();
    }


    public void setProcessing(boolean processing) {
        this.processing = processing;
    }

    public String toString() {
        String out = getOperator() == null ? Double.toString(getValue()) : Character.toString(getOperator().getOperator());

        for(Node child: children)
            out += child.toString();
        return out;
    }
}
