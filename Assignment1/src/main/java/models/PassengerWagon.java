package models;


/**
 * A PassengerWagon is a node in a doubly linked list of wagons.
 */
public class PassengerWagon extends Wagon {
    public int numberOfSeats;

    public PassengerWagon(int wagonId, int numberOfSeats) {
        super(wagonId);
        this.numberOfSeats = numberOfSeats;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }
}
