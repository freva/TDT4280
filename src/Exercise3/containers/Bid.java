package Exercise3.containers;

import Exercise3.Exchange;
import jade.core.AID;

import java.io.Serializable;
import java.util.HashMap;

public class Bid implements Serializable, Comparable {
    private HashMap<Item, Integer> items;
    private int coins, marketValue;
    private AID bidder;

    public Bid() {
        this.items = new HashMap<Item, Integer>();
        this.bidder = Exchange.getExchange();
    }

    public Bid(AID bidder, HashMap<Item, Integer> bid, int coins) {
        this.items = bid;
        this.coins = coins;
        this.bidder = bidder;
        for(Item item : bid.keySet()){
            marketValue += item.getValue()*bid.get(item);
        }
        marketValue += coins;
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

    public int getMarketValue(){
        return marketValue;
    }

    public String toString() {
        return "[" + items.entrySet() + " & " + coins + " coins]";
    }

    @Override
    public int compareTo(Object o) {
        if(! (o instanceof Bid)){
            return  Integer.MIN_VALUE;
        }
        Bid other = (Bid) o;
        if(this.items.keySet().size() != other.getItems().keySet().size()){
            return this.items.keySet().size() - other.getItems().keySet().size();
        }
        return this.marketValue - other.marketValue;
    }
}
