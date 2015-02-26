package Exercise3.containers;

import java.util.HashMap;

public class Bid {
    private AuctionItem ai;
    private HashMap<Item, Integer> bid;
    private int coins;

    public Bid(AuctionItem ai, HashMap<Item, Integer> bid, int coins) {
        this.ai = ai;
        this.bid = bid;
        this.coins = coins;
    }

    public AuctionItem getAi() {
        return ai;
    }

    public HashMap<Item, Integer> getBid() {
        return bid;
    }

    public int getCoins() {
        return coins;
    }
}
