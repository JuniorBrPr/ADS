package models;

/**
 * A Wagon is a node in a doubly linked list of wagons.
 */
public abstract class Wagon {
    protected int id;
    private Wagon nextWagon;
    private Wagon previousWagon;

    public Wagon(int wagonId) {
        this.id = wagonId;
    }

    public int getId() {
        return id;
    }

    public Wagon getNextWagon() {
        return nextWagon;
    }

    public Wagon getPreviousWagon() {
        return previousWagon;
    }

    /**
     * @return whether this wagon has a wagon appended at the tail
     */
    public boolean hasNextWagon() {
        return nextWagon != null;
    }

    /**
     * @return whether this wagon has a wagon prepended at the front
     */
    public boolean hasPreviousWagon() {
        return previousWagon != null;
    }

    /**
     * Returns the last wagon attached to it,
     * if there are no wagons attached to it then this wagon is the last wagon.
     *
     * @return the last wagon
     */
    public Wagon getLastWagonAttached() {
        Wagon currentWagon = this;

        while (currentWagon.hasNextWagon()) {
            currentWagon = currentWagon.getNextWagon();
        }

        return currentWagon;
    }

    /**
     * @return the length of the sequence of wagons towards the end of its tail
     * including this wagon itself.
     */
    public int getSequenceLength() {
        int length = 1;
        Wagon currentWagon = this;
        while (currentWagon.hasNextWagon()) {
            currentWagon = currentWagon.getNextWagon();
            length++;
        }
        return length;
    }

    /**
     * Attaches the tail wagon and its connected successors behind this wagon,
     * if and only if this wagon has no wagon attached at its tail
     * and if the tail wagon has no wagon attached in front of it.
     *
     * @param tail the wagon to attach behind this wagon.
     * @throws IllegalStateException if this wagon already has a wagon appended to it.
     * @throws IllegalStateException if tail is already attached to a wagon in front of it.
     *                               The exception should include a message that reports the conflicting connection,
     *                               e.g.: "%s is already pulling %s"
     *                               or:   "%s has already been attached to %s"
     */
    public void attachTail(Wagon tail) {
        if (tail == null) {
            return;
        }
        if (this.hasNextWagon()) {
            throw new IllegalStateException(this + " has already been attached to " + this.getNextWagon());
        }
        if (tail.hasPreviousWagon()) {
            throw new IllegalStateException(tail.getPreviousWagon() + " is already pulling " + tail);
        }
        this.nextWagon = tail;
        tail.previousWagon = this;
    }

    /**
     * Detaches the tail from this wagon and returns the first wagon of this tail.
     *
     * @return the first wagon of the tail that has been detached
     * or <code>null</code> if it had no wagons attached to its tail.
     */
    public Wagon detachTail() {
        Wagon tail = this.getNextWagon();
        if (tail != null) {
            tail.previousWagon = null;
            this.nextWagon = null;
        }
        return tail;
    }

    /**
     * Detaches this wagon from the wagon in front of it.
     * No action if this wagon has no previous wagon attached.
     *
     * @return the former previousWagon that has been detached from,
     * or <code>null</code> if it had no previousWagon.
     */
    public Wagon detachFront() {
        Wagon front = this.getPreviousWagon();
        if (front != null) {
            front.nextWagon = null;
            this.previousWagon = null;
        }
        return front;
    }

    /**
     * Replaces the tail of the <code>front</code> wagon by this wagon and its connected successors
     * Before such reconfiguration can be made,
     * the method first disconnects this wagon form its predecessor,
     * and the <code>front</code> wagon from its current tail.
     *
     * @param front the wagon to which this wagon must be attached to.
     */
    public void reAttachTo(Wagon front) {
        if (this == front || front == null) {
            return; // No change needed
        }

        front.detachTail();
        this.detachFront();

        front.attachTail(this);
    }

    /**
     * Removes this wagon from the sequence that it is part of,
     * and reconnects its tail to the wagon in front of it, if any.
     */
    public void removeFromSequence() {
        if (!this.hasPreviousWagon()) {
            this.detachTail();
            return;
        }
        if (!this.hasNextWagon()) {
            this.detachFront();
            return;
        }
        Wagon front = this.detachFront();
        Wagon tail = this.detachTail();
        tail.reAttachTo(front);
    }

    /**
     * Reverses the order in the sequence of wagons from this Wagon until its final successor.
     * The reversed sequence is attached again to the wagon in front of this Wagon, if any.
     * No action if this Wagon has no succeeding next wagon attached.
     *
     * @return the new start Wagon of the reversed sequence (with is the former last Wagon of the original sequence)
     */
    public Wagon reverseSequence() {
        Wagon originalHead = this.getPreviousWagon();
        Wagon current = this;
        Wagon prev = null;

        while (current != null) {
            Wagon next = current.getNextWagon();
            current.nextWagon = prev;
            current.previousWagon = next;
            prev = current;
            current = next;
        }

        if (originalHead != null) {
            prev.previousWagon = originalHead;
            originalHead.nextWagon = prev;
        }

        return prev;
    }



    @Override
    public String toString() {
        return "[Wagon-" + id + "]";
    }
}
