package Exercise3.containers;

import java.io.Serializable;
import java.util.HashMap;

public class AuctionState implements Serializable {
    private AuctionItem ai;
    private HashMap<Item, Integer> wantedItems;
    private Bid bestBid;

    public AuctionState(AuctionItem ai, HashMap<Item, Integer> wantedItems, Bid bestBid) {
        this.ai = ai;
        this.wantedItems = wantedItems;
        this.bestBid = bestBid;
    }

    public AuctionItem getAi() {
        return ai;
    }

    public HashMap<Item, Integer> getWantedItems() {
        return wantedItems;
    }

    public Bid getBestBid() {
        return bestBid;
    }
}
