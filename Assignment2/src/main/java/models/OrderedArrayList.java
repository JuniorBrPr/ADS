package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class OrderedArrayList<E>
        extends ArrayList<E>
        implements OrderedList<E> {

    protected Comparator<? super E> sortOrder; // the comparator that has been used with the latest sort
    protected int nSorted; // the number of sorted items in the first section of the list
    // representation-invariant
    // all items at index positions 0 <= index < nSorted have been ordered by the
    // given sortOrder comparator
    // other items at index position nSorted <= index < size() can be in any order
    // amongst themselves
    // and also relative to the sorted section

    public OrderedArrayList() {
        this(null);
    }

    public OrderedArrayList(Comparator<? super E> sortOrder) {
        super();
        this.sortOrder = sortOrder;
        this.nSorted = 0;
    }

    public Comparator<? super E> getSortOrder() {
        return this.sortOrder;
    }

    @Override
    public void clear() {
        super.clear();
        this.nSorted = 0;
    }

    @Override
    public void sort(Comparator<? super E> c) {
        super.sort(c);
        this.sortOrder = c;
        this.nSorted = this.size();
    }

    // TODO override the ArrayList.add(index, item), ArrayList.remove(index) and
    // Collection.remove(object) methods
    // such that they both meet the ArrayList contract of these methods (see
    // ArrayList JavaDoc)
    // and sustain the representation invariant of OrderedArrayList
    // (hint: only change nSorted as required to guarantee the representation
    // invariant,
    // do not invoke a sort or reorder items otherwise differently than is specified
    // by the ArrayList contract)

    @Override
    public void add(int index, E item) {
        super.add(index, item); // Call the super class method to insert the item
        if (index <= nSorted) {
            nSorted++; // Increment nSorted if the item is added in the sorted section
        }
    }

    @Override
    public E remove(int index) {
        E removedItem = super.remove(index); // Call the super class method to remove the item
        if (index < nSorted) {
            nSorted--; // Decrement nSorted if the removed item was in the sorted section
        }
        return removedItem;
    }

    @Override
    public boolean remove(Object obj) {
        int index = super.indexOf(obj); // Find the index of the object using the super class method
        if (index >= 0) {
            super.remove(index); // Remove the object
            if (index < nSorted) {
                nSorted--; // Decrement nSorted if the removed object was in the sorted section
            }
            return true;
        }
        return false; // Object not found
    }

    @Override
    public void sort() {
        if (this.nSorted < this.size()) {
            this.sort(this.sortOrder);
        }
    }

    @Override
    public int indexOf(Object item) {
        // efficient search can be done only if you have provided an sortOrder for the
        // list
        if (this.getSortOrder() != null) {
            return indexOfByIterativeBinarySearch((E) item);
        } else {
            return super.indexOf(item);
        }
    }

    @Override
    public int indexOfByBinarySearch(E searchItem) {
        if (searchItem != null) {
            return indexOfByIterativeBinarySearch(searchItem);
        } else {
            return -1;
        }
    }

    /**
     * finds the position of the searchItem by an iterative binary search algorithm
     * in the
     * sorted section of the arrayList, using the this.sortOrder comparator for
     * comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the
     * arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.sortOrder comparator, and
     * that need not to be in agreement with the .equals test.
     * Here we follow the comparator for sorting items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by
     *                   this.sortOrder
     * @return the position index of the found item in the arrayList, or -1 if no
     *         item matches the search item.
     */
    public int indexOfByIterativeBinarySearch(E searchItem) {
        int start = 0;
        int end = this.nSorted - 1;

        while (start <= end) {
            E mid = this.get((start + end) / 2);
            if (this.sortOrder.compare(mid, searchItem) > 0) {
                end = (start + end) / 2 - 1;
            }

            if (this.sortOrder.compare(mid, searchItem) == 0) {
                return (start + end) / 2;
            }

            if (this.sortOrder.compare(mid, searchItem) < 0) {
                start = (start + end) / 2 + 1;
            }
        }

        for (int i = this.nSorted; i < this.size(); i++) {
            if (this.get(i).equals(searchItem)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * finds the position of the searchItem by a recursive binary search algorithm
     * in the
     * sorted section of the arrayList, using the this.sortOrder comparator for
     * comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the
     * arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.sortOrder comparator, and
     * that need not to be in agreement with the .equals test.
     * Here we follow the comparator for sorting items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by
     *                   this.sortOrder
     * @return the position index of the found item in the arrayList, or -1 if no
     *         item matches the search item.
     */
    public int indexOfByRecursiveBinarySearch(E searchItem) {
        return indexOfByRecursiveBinarySearch(searchItem, 0, this.nSorted - 1);
    }

    private int indexOfByRecursiveBinarySearch(E searchItem, int start, int end) {
        if (start > end) {
            for (int i = this.nSorted; i < this.size(); i++) {
                if (this.get(i).equals(searchItem)) {
                    return i;
                }
            }
            return -1;
        }

        int mid = (start + end) / 2;
        if (this.sortOrder.compare(this.get(mid), searchItem) > 0) {
            return indexOfByRecursiveBinarySearch(searchItem, start, mid - 1);
        }

        if (this.sortOrder.compare(this.get(mid), searchItem) < 0) {
            return indexOfByRecursiveBinarySearch(searchItem, mid + 1, end);
        }

        if (this.sortOrder.compare(this.get(mid), searchItem) == 0) {
            return mid;
        }

        return -1;
    }

    /**
     * finds a match of newItem in the list and applies the merger operator with the
     * newItem to that match
     * i.e. the found match is replaced by the outcome of the merge between the
     * match and the newItem
     * If no match is found in the list, the newItem is added to the list.
     *
     * @param newItem
     * @param merger  a function that takes two items and returns an item that
     *                contains the merged content of
     *                the two items according to some merging rule.
     *                e.g. a merger could add the value of attribute X of the second
     *                item
     *                to attribute X of the first item and then return the first
     *                item
     * @return whether a new item was added to the list or not
     */
    @Override
    public boolean merge(E newItem, BinaryOperator<E> merger) {
        if (newItem == null)
            return false;
        int matchedItemIndex = this.indexOfByRecursiveBinarySearch(newItem, 0, this.nSorted - 1);

        if (matchedItemIndex < 0) {
            this.add(newItem);
            return true;
        } else {
            // TODO retrieve the matched item and
            // replace the matched item in the list with the merger of the matched item and
            // the newItem

            return false;
        }
    }

    /**
     * calculates the total sum of contributions of all items in the list
     *
     * @param mapper a function that calculates the contribution of a single item
     * @return the total sum of all contributions
     */
    @Override
    public double aggregate(Function<E, Double> mapper) {
        double sum = 0.0;

        // TODO loop over all items and use the mapper
        // to calculate and accumulate the contribution of each item

        return sum;
    }
}
