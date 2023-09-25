package models;

import java.util.HashSet;

public class Train {
    private final String origin;
    private final String destination;
    private final Locomotive engine;
    private Wagon firstWagon;

    /* Representation invariants:
        firstWagon == null || firstWagon.previousWagon == null
        engine != null
     */

    public Train(Locomotive engine, String origin, String destination) {
        this.engine = engine;
        this.destination = destination;
        this.origin = origin;
    }

    /**
     * Indicates whether the train has at least one connected Wagon
     *
     * @return whether the train has at least one connected Wagon
     */
    public boolean hasWagons() {
        return firstWagon != null;
    }

    /**
     * A train is a passenger train when its first wagon is a PassengerWagon
     * (we do not worry about the posibility of mixed compositions here)
     *
     * @return whether the train is a passenger train
     */
    public boolean isPassengerTrain() {
        return firstWagon instanceof PassengerWagon;
    }

    /**
     * A train is a freight train when its first wagon is a FreightWagon
     * (we do not worry about the posibility of mixed compositions here)
     *
     * @return whether the train is a freight train
     */
    public boolean isFreightTrain() {
        return firstWagon instanceof FreightWagon;
    }

    public Locomotive getEngine() {
        return engine;
    }

    public Wagon getFirstWagon() {
        return firstWagon;
    }

    /**
     * Replaces the current sequence of wagons (if any) in the train
     * by the given new sequence of wagons (if any)
     *
     * @param wagon the first wagon of a sequence of wagons to be attached (can be null)
     */
    public void setFirstWagon(Wagon wagon) {
        firstWagon = wagon;
    }

    /**
     * @return the number of Wagons connected to the train
     */
    public int getNumberOfWagons() {
        if (firstWagon == null) {
            return 0;
        }
        return firstWagon.getSequenceLength();
    }

    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {
        if (firstWagon == null) {
            return null;
        }
        return firstWagon.getLastWagonAttached();
    }

    /**
     * @return the total number of seats on a passenger train
     * (return 0 for a freight train)
     */
    public int getTotalNumberOfSeats() {
        if (firstWagon == null || !isPassengerTrain()) {
            return 0;
        }

        int totalNumberOfSeats = 0;

        PassengerWagon currentWagon = (PassengerWagon) firstWagon;
        while (currentWagon.hasNextWagon()) {
            totalNumberOfSeats += currentWagon.getNumberOfSeats();
            currentWagon = (PassengerWagon) currentWagon.getNextWagon();
        }
        return totalNumberOfSeats + currentWagon.getNumberOfSeats();
    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        if (firstWagon == null || !isFreightTrain()) {
            return 0;
        }

        int totalMaxWeight = 0;

        FreightWagon currentWagon = (FreightWagon) firstWagon;
        while (currentWagon.hasNextWagon()) {
            totalMaxWeight += currentWagon.getMaxWeight();
            currentWagon = (FreightWagon) currentWagon.getNextWagon();
        }

        return totalMaxWeight + currentWagon.getMaxWeight();
    }

    /**
     * Finds the wagon at the given position (starting at 0 for the first wagon of the train)
     *
     * @param position the position of the wagon to find
     * @return the wagon found at the given position
     * (return null if the position is not valid for this train)
     */
    public Wagon findWagonAtPosition(int position) {
        if (position < 0 || position >= getNumberOfWagons()) {
            return null;
        }

        Wagon wagon = firstWagon;
        for (int i = 0; i < position; i++) {
            wagon = wagon.getNextWagon();
        }
        return wagon;
    }

    /**
     * Finds the wagon with a given wagonId
     *
     * @param wagonId the id of the wagon to find
     * @return the wagon found
     * (return null if no wagon was found with the given wagonId)
     */
    public Wagon findWagonById(int wagonId) {
        Wagon wagon = firstWagon;
        while (wagon != null) {
            if (wagon.getId() == wagonId) {
                return wagon;
            }
            wagon = wagon.getNextWagon();
        }

        return null;
    }

    /**
     * Determines if the given sequence of wagons can be attached to this train
     * Verifies if the type of wagons match the type of train (Passenger or Freight)
     * Verifies that the capacity of the engine is sufficient to also pull the additional wagons
     * Verifies that the wagon is not part of the train already
     * Ignores the predecessors before the head wagon, if any
     *
     * @param wagon the head wagon of a sequence of wagons to consider for attachment
     * @return whether type and capacity of this train can accommodate attachment of the sequence
     */
    public boolean canAttach(Wagon wagon) {
        if (firstWagon == null) {
            return true;
        }

        if (wagon.getClass() != firstWagon.getClass()) {
            return false;
        }

        if (engine.getMaxWagons() < getNumberOfWagons() + wagon.getSequenceLength()) {
            return false;
        }

        HashSet<Wagon> wagons = new HashSet<>();
        Wagon currentWagon = firstWagon;
        while (currentWagon.hasNextWagon()) {
            wagons.add(currentWagon);
            currentWagon = currentWagon.getNextWagon();
        }

        currentWagon = wagon;
        while (currentWagon.hasNextWagon()) {
            if (wagons.contains(currentWagon)) {
                return false;
            }
            currentWagon = currentWagon.getNextWagon();
        }

        return true;
    }

    /**
     * Tries to attach the given sequence of wagons to the rear of the train
     * No change is made if the attachment cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if attachment is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be attached
     * @return whether the attachment could be completed successfully
     */
    public boolean attachToRear(Wagon wagon) {
        if (!canAttach(wagon)) {
            return false;
        }

        if (wagon.hasPreviousWagon()){
            wagon.detachFront();
        }

        if (firstWagon == null) {
            firstWagon = wagon;
            return true;
        }

        firstWagon.getLastWagonAttached().attachTail(wagon);
        return true;
    }

    /**
     * Tries to insert the given sequence of wagons at the front of the train
     * (the front is at position one, before the current first wagon, if any)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if insertion is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtFront(Wagon wagon) {
        if (!canAttach(wagon)) {
            return false;
        }

        if (firstWagon != null) {
            Wagon originalFirstWagon = firstWagon;
            Wagon currentWagon = wagon;

            while (currentWagon.hasNextWagon()) {
                currentWagon = currentWagon.getNextWagon();
            }
            currentWagon.attachTail(originalFirstWagon);
        }

        firstWagon = wagon;
        return true;
    }

    /**
     * Tries to insert the given sequence of wagons at/before the given position in the train.
     * (The current wagon at given position including all its successors shall then be reattached
     * after the last wagon of the given sequence.)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity
     * or the given position is not valid for insertion into this train)
     * if insertion is possible, the head wagon of the sequence is first detached from its predecessors, if any
     *
     * @param position the position where the head wagon and its successors shall be inserted
     *                 0 <= position <= numWagons
     *                 (i.e. insertion immediately after the last wagon is also possible)
     * @param wagon    the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtPosition(int position, Wagon wagon) {
        if (!canAttach(wagon)) {
            return false;
        }

        Wagon currentWagon = firstWagon;

        for (int i = 0; i < position; i++) {
            currentWagon = currentWagon.getNextWagon();
        }

        Wagon originalNextWagon = currentWagon.getNextWagon();
        currentWagon.attachTail(wagon);

        while (currentWagon.hasNextWagon()) {
            currentWagon = currentWagon.getNextWagon();
        }

        currentWagon.attachTail(originalNextWagon);
        return true;
    }

    /**
     * Tries to remove one Wagon with the given wagonId from this train
     * and attach it at the rear of the given toTrain
     * No change is made if the removal or attachment cannot be made
     * (when the wagon cannot be found, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param wagonId the id of the wagon to be removed
     * @param toTrain the train to which the wagon shall be attached
     *                toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean moveOneWagon(int wagonId, Train toTrain) {
        Wagon wagon = findWagonById(wagonId);

        if (wagon == null) {
            return false;
        }

        if (!toTrain.canAttach(wagon)) {
            return false;
        }

        wagon.removeFromSequence();
        toTrain.attachToRear(wagon);
        return true;
    }

    /**
     * Tries to split this train before the wagon at given position and move the complete sequence
     * of wagons from the given position to the rear of toTrain.
     * No change is made if the split or re-attachment cannot be made
     * (when the position is not valid for this train, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param position 0 <= position < numWagons
     * @param toTrain  the train to which the split sequence shall be attached
     *                 toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean splitAtPosition(int position, Train toTrain) {
        if (position < firstWagon.getSequenceLength()) {
            return false;
        }
        if (toTrain.engine.getMaxWagons() < toTrain.getNumberOfWagons() + firstWagon.getSequenceLength() - position) {
            return false;
        }
        if (!toTrain.canAttach(findWagonAtPosition(position))) {
            return false;
        }

        Wagon wagon = findWagonAtPosition(position);
        wagon.detachFront();
        toTrain.attachToRear(wagon);
        return true;
    }

    /**
     * Reverses the sequence of wagons in this train (if any)
     * i.e. the last wagon becomes the first wagon
     * the previous wagon of the last wagon becomes the second wagon
     * etc.
     * (No change if the train has no wagons or only one wagon)
     */
    public void reverse() {
        if (firstWagon == null || !firstWagon.hasNextWagon()) {
            return;
        }
        firstWagon.reverseSequence();
    }

    @Override
    public String toString() {
        StringBuilder sb =
                new StringBuilder("Train with engine: " + engine + " from " + origin + " to " + destination + "\n");
        if (firstWagon == null) {
            sb.append("No wagons attached");
        } else {
            sb.append("Wagons attached:\n");
            sb.append(firstWagon);
        }
        return sb.toString();
    }
}
