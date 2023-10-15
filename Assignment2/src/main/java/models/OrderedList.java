package models;

import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Represents a list of items that can be sorted and searched by binary search.
 *
 * @param <E> the type of the items in the list
 */
public interface OrderedList<E> extends List<E> {
    Comparator<? super E> getSortOrder();

    void sort();

    int indexOfByBinarySearch(E searchItem);

    boolean merge(E item, BinaryOperator<E> merger);

    double aggregate(Function<E, Double> mapper);
}
