package Exercise3;

import java.util.Random;

/**
 * Created by BrageEkroll on 26.02.2015.
 */
public enum Item {
    bronze(0), iron(1), steel(2), silver(3), gold(4), platinum(5), titanium(6), uranium(7);

    private int id;

    private Item(int id){
        this.id = id;
    }

}
