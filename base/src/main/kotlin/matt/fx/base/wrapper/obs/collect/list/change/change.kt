package matt.fx.base.wrapper.obs.collect.list.change

import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import matt.lang.assertions.require.requireEquals
import matt.obs.col.change.AddAt
import matt.obs.col.change.AddAtEnd
import matt.obs.col.change.AtomicListChange
import matt.obs.col.change.ClearList
import matt.obs.col.change.ListAdditionBase
import matt.obs.col.change.ListChange
import matt.obs.col.change.ListRemovalBase
import matt.obs.col.change.MultiAddAt
import matt.obs.col.change.MultiAddAtEnd
import matt.obs.col.change.RemoveAt
import matt.obs.col.change.RemoveAtIndices
import matt.obs.col.change.RemoveElementFromList
import matt.obs.col.change.RemoveElements
import matt.obs.col.change.ReplaceAt
import matt.obs.col.change.RetainAllList

internal fun <E> ListChange<E>.toFXChange(list: ObservableList<E>) = MBackedFXListChange(this, list)

class MBackedFXListChange<E>(
    private val mChange: ListChange<E>,
    list: ObservableList<E>
) : Change<E>(list) {

    private var index = -1

    private val mChanges =
        when (mChange) {
            is AtomicListChange -> mChange.changes
            else                -> listOf(mChange)
        }
    private var currentMChange: ListChange<E>? = null

    override fun next(): Boolean {
        if (index == mChanges.lastIndex) return false
        index += 1
        currentMChange = mChanges[index]
        return true
    }

    override fun reset() {
        index = -1
        currentMChange = null
    }

    override fun getFrom() =
        currentMChange!!.run {
            when (this) {
                is RemoveAt              -> index
                is ClearList             -> 0
                is AddAtEnd              -> collection.size - 1
                is RemoveElementFromList -> index
                is MultiAddAtEnd         -> collection.size - added.size
                is AddAt                 -> index
                is MultiAddAt            -> TODO("${this::class.simpleName}")
                is ReplaceAt             -> lowestChangedIndex
                is RemoveAtIndices       -> lowestChangedIndex
                is RemoveElements        -> TODO("${this::class.simpleName}")
                is RetainAllList         -> lowestChangedIndex
                is AtomicListChange      -> TODO("${this::class.simpleName}")
            }
        }

    override fun getTo() =
        currentMChange!!.run {
            when (this) {
                is RemoveAt              -> index
                is ClearList             -> 0
                is AddAtEnd              -> collection.size
                is RemoveElementFromList -> index
                is MultiAddAtEnd         -> collection.size
                is AddAt                 -> index + 1
                is MultiAddAt            -> TODO("${this::class.simpleName}")
                is ReplaceAt             -> lowestChangedIndex + 1
                is RemoveAtIndices       -> {
                    removedElementsIndexed.zipWithNext { a, b -> requireEquals(b.index.i, a.index.i + 1) }
                    lowestChangedIndex
                }

                is RemoveElements        -> TODO("${this::class.simpleName}")
                is RetainAllList         -> {
                    if (removedSize > 1) TODO()
                    lowestChangedIndex
                }

                is AtomicListChange      -> TODO("${this::class.simpleName}")
            }
        }

    override fun getRemoved() = (currentMChange!! as? ListRemovalBase)?.removedElements ?: listOf()
    override fun getAddedSubList() = (currentMChange!! as? ListAdditionBase)?.addedElements ?: listOf()
    override fun getPermutation() = intArrayOf()
}

