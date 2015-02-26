package Exercise3.containers;

import java.io.Serializable;
import java.util.HashMap;

public class AuctionState implements Serializable {
    private AuctionItem ai;
    private HashMap<Item, Integer> wantedItems;
    private Bid bestBid;
    private int numRounds;

    public AuctionState(AuctionItem ai, HashMap<Item, Integer> wantedItems, Bid bestBid, int numRounds) {
        this.ai = ai;
        this.wantedItems = wantedItems;
        this.bestBid = bestBid;
        this.numRounds = numRounds;
    }

    public AuctionState(AuctionState as, HashMap<Item, Integer> wantedItems, Bid best) {
        this(as.getAuctionItem(), wantedItems, best, as.getNumRounds()+1);
    }

    public AuctionItem getAuctionItem() {
        return ai;
    }

    public HashMap<Item, Integer> getWantedItems() {
        return wantedItems;
    }

    public Bid getBestBid() {
        return bestBid;
    }

    public int getNumRounds() {
        return numRounds;
    }
}
