package matt.fx.base.collect

import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import java.util.Collections
import java.util.Random

fun <T> observableListOf(): ObservableList<T> = FXCollections.observableArrayList()

/**
 * Returns an empty new [ObservableList] with the given [extractor]. This list reports element updates.
 */
fun <T> observableListOf(extractor: (T) -> Array<Observable>): ObservableList<T> =
    FXCollections.observableArrayList(extractor)

fun <T> observableSetOf(): ObservableSet<T> = FXCollections.observableSet()

fun <T> List<T>.toObservable(): ObservableList<T> = FXCollections.observableList(toMutableList())


/**
 * Fills the observable list with the provided [value].
 * Fires only **one** change notification on the list.
 */
fun <T> ObservableList<T>.fill(value: T): Unit = FXCollections.fill(this, value)

/**
 * Reverse the order in the observable list.
 * Fires only **one** change notification on the list.
 */
fun <T> ObservableList<T>.reverse(): Unit = FXCollections.reverse(this)

/**
 * Randomly shuffles elements in this observable list.
 * Fires only **one** change notification on the list.
 */
fun <T> ObservableList<T>.shuffle(): Unit = FXCollections.shuffle(this)

/**
 * Randomly shuffles elements in this observable list using the specified [random] instance as the source of randomness.
 * Fires only **one** change notification on the list.
 */
fun <T> ObservableList<T>.shuffle(random: Random): Unit = FXCollections.shuffle(this, random)

/**
 * Sorts elements in the observable list according to their natural sort order.
 * Fires only **one** change notification on the list.
 */
fun <T: Comparable<T>> ObservableList<T>.sort() {
    if (size > 1) FXCollections.sort(this)
}

/**
 * Sorts elements in the observable list according to the order specified with [comparator].
 * Fires only **one** change notification on the list.
 */
fun <T> ObservableList<T>.sortWith(comparator: Comparator<in T>) {
    if (size > 1) FXCollections.sort(this, comparator)
}

/**
 * Sorts elements in the observable list according to natural sort order of the value returned by specified [selector] function.
 * Fires only **one** change notification on the list.
 */
inline fun <T, R: Comparable<R>> ObservableList<T>.sortBy(crossinline selector: (T) -> R?) {
    if (size > 1) sortWith(compareBy(selector))
}

/**
 * Sorts elements in the observable list descending according to natural sort order of the value returned by specified [selector] function.
 * Fires only **one** change notification on the list.
 */
inline fun <T, R: Comparable<R>> ObservableList<T>.sortByDescending(crossinline selector: (T) -> R?) {
    if (size > 1) sortWith(compareByDescending(selector))
}



/**
 * Swaps the position of two items at two respective indices
 */
fun <T> MutableList<T>.swap(indexOne: Int, indexTwo: Int) {
    if (this is ObservableList<*>) {
        if (indexOne == indexTwo) return
        val min = Math.min(indexOne, indexTwo)
        val max = Math.max(indexOne, indexTwo)
        val o2 = removeAt(max)
        val o1 = removeAt(min)
        add(min, o2)
        add(max, o1)
    } else {
        Collections.swap(this, indexOne, indexTwo)
    }
}

/**
 * Swaps the index position of two items
 */
fun <T> MutableList<T>.swap(itemOne: T, itemTwo: T) = swap(indexOf(itemOne), indexOf(itemTwo))
