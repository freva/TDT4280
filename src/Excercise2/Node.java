package Excercise2;

import java.util.ArrayList;

/**
 * Created by BrageEkroll on 10.02.2015.
 */
public class Node {
    private boolean isComputable = false;
    private double value;
    private Operator operator;
    private ArrayList<Node> children = new ArrayList<Node>();
    private Node parent;

    public Node(Node parent, String value){
        this.parent = parent;
        getContent(value);
        parent.addChild(this);
    }

    private void getContent(String value){
        for(Operator operator : Operator.values()){
            if(value == operator.name()){
                this.operator = operator;
                break;
            }
        }
        setValue(Double.valueOf(value));
    }

    public void addChild(Node child){
        children.add(child);
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

    public boolean isComputable(){
        return isComputable;
    }

    public void setComputable(boolean isComputable){
        this.isComputable = isComputable;
    }

    public double getValue(){
        return value;
    }

    public void setValue(double value){
        this.value = value;
        setComputable(true);
    }
}
