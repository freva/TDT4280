package Exercise3.containers;

public enum Item {
    BRONZE(0, 10), IRON(1, 15), STEEL(2, 20), SILVER(3, 25), GOLD(4, 30), COPPER(5, 35), PLATINUM(6, 40), TITANIUM(7, 50);

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
