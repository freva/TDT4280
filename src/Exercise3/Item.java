package Exercise3;

public enum Item {
    bronze(0, 10), iron(1, 15), steel(2, 20), silver(3, 25), gold(4, 30), platinum(5, 35), titanium(6, 40), uranium(7, 50);

    private int id, value;
    private Item(int id, int value){
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public int getValue() {
        return value;
    }
}
