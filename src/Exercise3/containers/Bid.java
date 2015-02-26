package Exercise3.containers;

import java.util.HashMap;

public class Bid {
    private HashMap<Item, Integer> items;
    private int coins;

    public Bid() {
        this.items = new HashMap<Item, Integer>();
    }

    public Bid(HashMap<Item, Integer> bid, int coins) {
        this.items = bid;
        this.coins = coins;
    }

    public HashMap<Item, Integer> getItems() {
        return items;
    }

    public int getCoins() {
        return coins;
    }
}
