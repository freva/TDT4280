package Exercise3.containers;

import jade.core.AID;

import java.io.Serializable;

public class AuctionItem implements Serializable {
    private AID owner;
    private Item item;
    private int amount;

    public AuctionItem(AID owner, Item item, int amount) {
        this.owner = owner;
        this.item = item;
        this.amount = amount;
    }

    public AID getOwner() {
        return owner;
    }

    public Item getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public int getMarketValue() {
        return item.getValue() * amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String toString() {
        return item + "x" + amount;
    }
}