package Exercise3.containers;

public enum Item {
    BRONZE(10), IRON(15), STEEL(20), SILVER(25), GOLD(30), COPPER(35), PLATINUM(40), TITANIUM(50);

    private int value;
    private Item(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
