package Exercise3.containers;

import Exercise3.Exchange;
import jade.core.AID;

import java.util.HashMap;

public class Bid {
    private HashMap<Item, Integer> items;
    private int coins;
    private AID bidder;

    public Bid() {
        this.items = new HashMap<Item, Integer>();
        this.bidder = Exchange.getExchange();
    }

    public Bid(AID bidder, HashMap<Item, Integer> bid, int coins) {
        this.items = bid;
        this.coins = coins;
        this.bidder = bidder;
    }

    public HashMap<Item, Integer> getItems() {
        return items;
    }

    public int getCoins() {
        return coins;
    }

    public AID getBidder() {
        return bidder;
    }
}
