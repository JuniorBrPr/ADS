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

    @Override
    public void add(int index, E item) {
        System.out.println("Adding " + item + " at index " + index);
        super.add(index, item);
        for (int i = 0; i < this.size(); i++) {
            System.out.println(i +" : " + this.get(i));
        }

        if (index < nSorted - 1) {
            nSorted = index;
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
        return this.indexOfByBinarySearch((E) item);
    }

    @Override
    public int indexOfByBinarySearch(E searchItem) {
        return indexOfByIterativeBinarySearch(searchItem);
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
     * item matches the search item.
     */
    public int indexOfByIterativeBinarySearch(E searchItem) {
        int start = 0;
        int end = this.nSorted - 1;

        while (start <= end) {
            int mid = (start + end) / 2;
            if (this.sortOrder.compare(this.get(mid), searchItem) == 0){
                return mid;
            }
            if (this.sortOrder.compare(this.get(mid), searchItem) > 0) {
                end = mid - 1;
            }
            if (this.sortOrder.compare(this.get(mid), searchItem) < 0) {
                start = mid + 1;
            }
        }

        return indexByLinearSearch(searchItem, this.nSorted, this.size() - 1);
    }

    public int indexByLinearSearch(E searchItem, int start, int end) {
        for (int i = start; i <= end; i++) {
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
        int mid = (start + end) / 2;

        if (this.sortOrder.compare(this.get(mid), searchItem) == 0) {
            return mid;
        }

        if (this.sortOrder.compare(this.get(mid), searchItem) > 0) {
            return indexOfByRecursiveBinarySearch(searchItem, start, mid - 1);
        }

        if (this.sortOrder.compare(this.get(mid), searchItem) < 0) {
            return indexOfByRecursiveBinarySearch(searchItem, mid + 1, end);
        }

        return indexByLinearSearch(searchItem, this.nSorted, this.size() - 1);
    }

    /**
     * finds a match of newItem in the list and applies the merger operator with the
     * newItem to that match
     * i.e. the found match is replaced by the outcome of the merge between the
     * match and the newItem
     * If no match is found in the list, the newItem is added to the list.
     *
     * @param newItem the item to be merged into the list
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
        int matchedItemIndex = this.indexOfByIterativeBinarySearch(newItem);

        if (matchedItemIndex < 0) {
            this.add(newItem);
            return true;
        } else {
            this.set(matchedItemIndex, merger.apply(this.get(matchedItemIndex), newItem));
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
        for (E e : this) {
            sum += mapper.apply(e);
        }
        return sum;
    }
}
