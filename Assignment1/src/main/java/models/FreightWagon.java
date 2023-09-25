package models;


/**
 * A FreightWagon is a node in a doubly linked list of wagons.
 */
public class FreightWagon extends Wagon {

    public int maxWeight;

    public FreightWagon(int wagonId, int maxWeight) {
        super(wagonId);
        this.maxWeight = maxWeight;
    }

    public int getMaxWeight() {
        return maxWeight;
    }
}
