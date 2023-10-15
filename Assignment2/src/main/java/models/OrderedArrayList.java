package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.function.BinaryOperator;
import java.util.function.Function;


/**
 * An ArrayList that keeps its items in sorted order.
 * The items in the list shall be sorted according to the comparator that has been used with the latest sort.
 * The items in the list shall be sorted in the first section of the list, and the unsorted items shall be in the
 * second section of the list.
 * The first section of the list shall be of size nSorted, and the second section of the list shall be of
 * size()-nSorted.
 *
 * @param <E> The type of the elements in the list.
 */
public class OrderedArrayList<E> extends ArrayList<E> implements OrderedList<E> {
    protected Comparator<? super E> sortOrder;
    protected int nSorted;

    public OrderedArrayList() {
        this(null);
    }

    public OrderedArrayList(Comparator<? super E> sortOrder) {
        super();
        this.sortOrder = sortOrder;
        this.nSorted = 0;
    }

    /**
     * Clears the list and resets nSorted to 0.
     */
    @Override
    public void clear() {
        super.clear();
        this.nSorted = 0;
    }

    /**
     * Adds the specified element to the list.
     * If the element is added to the sorted section of the list, nSorted is decremented to reflect the new size of
     * the sorted section.
     *
     * @param index Index at which the specified element is to be inserted.
     * @param item  Element to be inserted.
     */
    @Override
    public void add(int index, E item) {
        super.add(index, item);
        if (index < nSorted) {
            nSorted = index;
        }
    }


    /**
     * Removes the element at the specified position in this list.
     * If the element is removed from the sorted section of the list, nSorted is decremented to reflect the new size of
     * the sorted section.
     *
     * @param index The index of the element to be removed.
     * @return The element previously at the specified position.
     */
    @Override
    public E remove(int index) {
        E removedItem = super.remove(index); // Call the super class method to remove the item
        if (index < nSorted) {
            nSorted--; // Decrement nSorted if the removed item was in the sorted section
        }
        return removedItem;
    }

    public Comparator<? super E> getSortOrder() {
        return this.sortOrder;
    }

    /**
     * Returns the index of the first occurrence of the specified element in this list,
     * or -1 if this list does not contain the element.
     * nSorted is decremented to reflect the new size of the sorted section if the element index is in the sorted
     * section.
     *
     * @param item Element to be removed from this list, if present
     * @return The index of the first occurrence of the specified element in this list,
     */
    @Override
    public boolean remove(Object item) {
        int index = indexOf(item);
        if (index >= 0) {
            super.remove(index);
            if (index < nSorted) {
                nSorted--;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the index of the first occurrence of the specified element in the sorted section of this list,
     * or -1 if this list does not contain the element.
     * Binary search is used to find the index of the element in the sorted section. If the element is not found in the
     * sorted section, linear search is used to find the index of the element in the unsorted section.
     *
     * @param item Element to search for.
     * @return The index of the first occurrence of the specified element in the sorted section of this list,
     * or -1 if this list does not contain the element.
     */
    @Override
    public int indexOf(Object item) {
        return this.indexOfByBinarySearch((E) item);
    }

    /**
     * Finds the position of the searchItem by an iterative binary search algorithm in the sorted section of the
     * arrayList, using the this.sortOrder comparator for comparison and equality test. If the item is not found in the
     * sorted section, the unsorted section of the arrayList shall be searched by linear search.
     *
     * @param searchItem The item to be searched on the basis of comparison by
     *                   this.sortOrder.
     * @return The position index of the found item in the arrayList, or -1 if no
     * item matches the search item.
     */
    public int indexOfByIterativeBinarySearch(E searchItem) {
        int start = 0; // Start index of the sorted section
        int end = this.nSorted - 1; // End index of the sorted section
        while (start <= end) {
            int mid = start + ((end - start) / 2); // Mid-index of the sorted section
            if (this.sortOrder.compare(this.get(mid), searchItem) == 0) {
                return mid; // Return the index of the found item
            }
            if (this.sortOrder.compare(this.get(mid), searchItem) < 0) {
                start = mid + 1; // Search the right half of the sorted section
            }
            if (this.sortOrder.compare(this.get(mid), searchItem) > 0) {
                end = mid - 1; // Search the left half of the sorted section
            }
        }

        return indexByLinearSearch(searchItem, this.nSorted, this.size() - 1); // Search the unsorted section
    }

    /**
     * Finds the position of the searchItem by a recursive binary search algorithm in the sorted section of the
     * arrayList, using the this.sortOrder comparator for comparison and equality test. If the item is not found in the
     * sorted section, the unsorted section of the arrayList shall be searched by linear search.
     *
     * @param searchItem The item to be searched on the basis of comparison by
     *                   this.sortOrder
     * @return The position index of the found item in the arrayList, or -1 if no
     * item matches the search item.
     */
    public int indexOfByRecursiveBinarySearch(E searchItem) {
        return RecursiveBinarySearch(searchItem, 0, this.nSorted - 1); // Search the sorted section
    }

    /**
     * Private helper method for recursive binary search.
     *
     * @param searchItem The item to be searched on the basis of comparison by.
     * @param start      The start index of the sorted section.
     * @param end        The end index of the sorted section.
     * @return The position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    private int RecursiveBinarySearch(E searchItem, int start, int end) {
        if (start > end) {
            return indexByLinearSearch(searchItem, this.nSorted, this.size() - 1);
        }
        int mid = start + ((end - start) / 2);
        if (this.sortOrder.compare(this.get(mid), searchItem) == 0) {
            return mid;
        }
        if (this.sortOrder.compare(this.get(mid), searchItem) < 0) {
            return RecursiveBinarySearch(searchItem, mid + 1, end);
        }
        if (this.sortOrder.compare(this.get(mid), searchItem) > 0) {
            return RecursiveBinarySearch(searchItem, start, mid - 1);
        }
        return -1;
    }

    /**
     * Finds the position of the searchItem by a linear search algorithm.
     * Used to search the unsorted section of the list.
     *
     * @param searchItem The item to be searched.
     * @param start      The start index of the unsorted section.
     * @param end        The end index of the unsorted section.
     * @return The position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    private int indexByLinearSearch(E searchItem, int start, int end) {
        for (int i = start; i <= end; i++) {
            if (this.get(i).equals(searchItem)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sorts the list according to the specified comparator.
     *
     * @param c The comparator to be used for sorting.
     */
    @Override
    public void sort(Comparator<? super E> c) {
        super.sort(c);
        this.sortOrder = c;
        this.nSorted = this.size();
    }

    /**
     * Sorts the list if it is not already completely sorted.
     */
    @Override
    public void sort() {
        if (this.nSorted < this.size()) {
            this.sort(this.sortOrder);
        }
    }

    /**
     * Returns the index of the first occurrence of the specified element in the sorted section of this list,
     * or -1 if this list does not contain the element.
     * Binary search is used to find the index of the element in the sorted section. If the element is not found in the
     * sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The binary search algorithm used is either iterative or recursive, depending on the value of a random integer.
     * The random integer is either 0 or 1.
     *
     * @param searchItem The item to be searched on the basis of comparison by.
     * @return The position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    @Override
    public int indexOfByBinarySearch(E searchItem) {
        int randomInt = new Random().nextInt(2);
        if (randomInt == 0) {
            return this.indexOfByIterativeBinarySearch(searchItem);
        } else {
            return this.indexOfByRecursiveBinarySearch(searchItem);
        }
    }

    /**
     * Finds a match of newItem in the list and applies the merger operator with the newItem to that match.
     * i.e. the found match is replaced by the outcome of the merge between the match and the newItem.
     * If no match is found in the list, the newItem is added to the list.
     *
     * @param newItem The item to be merged into the list
     * @param merger  A function that takes two items and returns an item that contains the merged content of the two
     *                items according to some merging rule. e.g. a merger could add the value of attribute X of the
     *                second item to attribute X of the first item and then return the first item.
     * @return Whether a new item was added to the list or not.
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
     * Calculates the total sum of contributions of all items in the list
     *
     * @param mapper A function that calculates the contribution of a single item
     * @return The total sum of all contributions
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
