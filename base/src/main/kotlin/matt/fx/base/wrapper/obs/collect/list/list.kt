package matt.fx.base.wrapper.obs.collect.list

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import matt.collect.fake.FakeMutableList
import matt.fx.base.wrapper.obs.collect.list.change.toFXChange
import matt.lang.anno.Open
import matt.lang.common.ILLEGAL
import matt.lang.convert.BiConverter
import matt.lang.function.Op
import matt.lang.setall.setAll
import matt.model.op.prints.Prints
import matt.obs.bindhelp.BindableList
import matt.obs.bindhelp.BindableListImpl
import matt.obs.col.change.AddAt
import matt.obs.col.change.ListChange
import matt.obs.col.change.MultiAddAt
import matt.obs.col.change.RemoveAt
import matt.obs.col.change.RemoveAtIndices
import matt.obs.col.olist.ImmutableObsList
import matt.obs.col.olist.MutableObsList
import matt.obs.listen.CollectionListenerBase
import matt.obs.listen.ListListener
import matt.obs.listen.ListListenerBase
import matt.obs.listen.MyListenerInter
import matt.obs.listen.update.ListUpdate
import matt.obs.prop.ObsVal
import java.util.Spliterator
import java.util.function.Predicate

fun <E> ObservableList<E>.createImmutableWrapper() = FXBackedImmutableObservableList(this)
fun <E> ObservableList<E>.createMutableWrapper() = FXBackedMutableObservableList(this)

/*"ObservableListWrapper" is taken*/
interface MyObservableListWrapper<E> : Observable {
    fun addListener(listener: ListChangeListener<E>)
    fun removeListener(listener: ListChangeListener<E>)
    fun filtered(predicate: Predicate<E>): FilteredList<E>
    fun sorted(comparator: Comparator<E>): SortedList<E>
    fun sorted(): SortedList<E>
}

fun <E> MyObservableListWrapper<E>.listen(
    onAdd: ((E) -> Unit),
    onRemove: ((E) -> Unit)
) {
    addListener(
        ListChangeListener {
            while (it.next()) {
                it.addedSubList.forEach {
                    onAdd(it)
                }
                it.removed.forEach {
                    onRemove(it)
                }
            }
        }
    )
}

fun <E> MyObservableListWrapper<E>.onChange(op: (ListChangeListener.Change<out E>) -> Unit) =
    apply {
        addListener(ListChangeListener { op(it) })
    }


interface MyObservableListWrapperPlusList<E> : MyObservableListWrapper<E>, List<E>

fun <E> mfxMutableListConverter() =
    object : BiConverter<ObservableList<E>, MutableObsList<E>> {
        override fun convertToB(a: ObservableList<E>): MutableObsList<E> =
            when (a) {
                is MBackedFXObservableList -> a.mList as MutableObsList<E>
                else                          -> a.createMutableWrapper()
            }

        override fun convertToA(b: MutableObsList<E>): ObservableList<E> =
            when (b) {
                is FXBackedObsList -> b.obs/* as ObservableList<E>*/

                else                  -> b.createMutableFXWrapper()
            }
    }

abstract class FXBackedObsList<E>(internal val obs: ObservableList<E>): MutableObsList<E>

abstract class ObservableListWrapperImpl<E>(obs: ObservableList<E>) :
    FXBackedObsList<E>(obs),
    MyObservableListWrapperPlusList<E>,
    Observable by obs {


    final override fun addListener(listener: ListChangeListener<E>) = obs.addListener(listener)
    final override fun removeListener(listener: ListChangeListener<E>) = obs.removeListener(listener)
    final override fun filtered(predicate: Predicate<E>): FilteredList<E> = obs.filtered(predicate)
    final override fun sorted(comparator: Comparator<E>): SortedList<E> = obs.sorted(comparator)
    final override fun sorted(): SortedList<E> = obs.sorted()
}

interface FXOListWrapperAndBasic<E> : MyObservableListWrapper<E>, MutableObsList<E>, BindableList<E>
interface FXOLMutableListWrapperAndBasic<E> : FXOListWrapperAndBasic<E>, MutableList<E>

class FXBackedMutableObservableListBase<E>(obs: ObservableList<E>) :
    ObservableListWrapperImpl<E>(obs),
    MutableObsList<E>,
    MutableList<E> by obs,
    FXOLMutableListWrapperAndBasic<E>,
    BindableList<E> {


    override fun spliterator(): Spliterator<E> {
        error("I WOULD REMOVE THIS OVERRIDE BUT CURRENTLY THAT CAUSES A BUGGED COMPILER ERROR")
    }

    val bindableListHelper by lazy { BindableListImpl(this) }
    override fun <S> bind(
        source: ImmutableObsList<S>,
        converter: (S) -> E
    ) = bindableListHelper.bind(source, converter)

    override fun <S> bindWeakly(
        source: ImmutableObsList<S>,
        converter: (S) -> E
    ) =
        bindableListHelper.bindWeakly(source, converter)

    override fun <T> bind(
        source: ObsVal<T>,
        converter: (T) -> List<E>
    ) = bindableListHelper.bind(source, converter)

    override val bindManager get() = bindableListHelper.bindManager
    override var theBind
        get() = bindManager.theBind
        set(value) {
            bindManager.theBind = value
        }

    override fun unbind() = bindableListHelper.unbind()


    override var nam: String? = null

    override var debugger: Prints? = null

    @Synchronized
    override fun onChange(
        listenerName: String?,
        op: (ListChange<E>) -> Unit
    ): ListListener<E> {
        val listener =
            ListListener {
                op(it)
            }
        if (listenerName != null) {
            listener.name = listenerName
        }
        addListener(listener)
        return listener
    }


    private val listenersMap = mutableMapOf<CollectionListenerBase<E, *, *>, ListChangeListener<E>>()

    @Synchronized
    override fun addListener(listener: ListListenerBase<E>): ListListenerBase<E> {
        val oListener =
            ListChangeListener<E> {
                while (it.next()) {
                    if (it.wasRemoved()) when (it.removedSize) {
                        0    -> Unit
                        1    -> listener.notify(ListUpdate(RemoveAt(obs, it.removed[0], it.from)))
                        else ->
                            listener.notify(
                                ListUpdate(
                                    RemoveAtIndices(
                                        obs,
                                        it.removed.zip(it.from..<it.from + it.removed.size)
                                            .map { IndexedValue(it.second, it.first) },
                                        quickIsRange = true
                                    )
                                )
                            )
                    }
                    if (it.wasAdded()) when (it.addedSize) {
                        0    -> Unit
                        1    -> listener.notify(ListUpdate(AddAt(obs, it.addedSubList[0], it.from)))
                        else -> listener.notify(ListUpdate(MultiAddAt(obs, it.addedSubList, it.from)))
                    }
                }
            }
        addListener(oListener)
        listenersMap[listener] = oListener
        return listener
    }


    @Synchronized
    override fun removeListener(listener: MyListenerInter<*>) {
        val l = listenersMap[listener] ?: return
        obs.removeListener(l)
        listenersMap.remove(listener)
    }

    override fun atomicChange(op: MutableObsList<E>.() -> Unit) {
        TODO()
    }

    override fun releaseUpdatesAfter(op: Op) {
        TODO()
    }
}


class FXBackedImmutableObservableList<E>(obs: ObservableList<E>) :
    FXBackedObsList<E>(obs),
    FXOListWrapperAndBasic<E> by FXBackedMutableObservableListBase(
        obs
    )


class FXBackedMutableObservableList<E>(obs: ObservableList<E>) :
    FXBackedObsList<E>(obs),
    FXOLMutableListWrapperAndBasic<E> by FXBackedMutableObservableListBase(
        obs
    ),
    MutableObsList<E>

fun <E> MutableObsList<E>.createMutableFXWrapper() = MutableMBackedFXObservableList(this)
fun <E> ImmutableObsList<E>.createFXWrapper() = MBackedFXObservableList(this)


open class MBackedFXObservableList<E>(internal open val mList: ImmutableObsList<E>) :
    ObservableList<E>,
    MutableList<E> by FakeMutableList(mList) {


    private val fakeMutableList by lazy { FakeMutableList(mList) }

    @Open
    override fun addAll(vararg elements: E): Boolean = ILLEGAL
    @Open override fun remove(
        from: Int,
        to: Int
    ): Unit = ILLEGAL

    @Open override fun retainAll(vararg elements: E): Boolean = ILLEGAL
    @Open override fun removeAll(vararg elements: E): Boolean = ILLEGAL
    @Open override fun setAll(col: MutableCollection<out E>): Boolean = ILLEGAL
    @Open override fun setAll(vararg elements: E): Boolean = ILLEGAL

    private val invalidationListenerMap = mutableMapOf<InvalidationListener, MyListenerInter<*>>()
    private val changeListenerMap = mutableMapOf<ListChangeListener<in E>, MyListenerInter<*>>()
    final override fun addListener(listener: InvalidationListener) {
        invalidationListenerMap[listener] =
            mList.observe {
                listener.invalidated(this)
            }
    }

    final override fun removeListener(listener: InvalidationListener) {
        invalidationListenerMap[listener]?.tryRemovingListener()
    }

    final override fun addListener(listener: ListChangeListener<in E>) {
        changeListenerMap[listener] =
            mList.onChange {
                listener.onChanged(it.toFXChange(this))
            }
    }

    final override fun removeListener(listener: ListChangeListener<in E>) {
        changeListenerMap[listener]?.tryRemovingListener()
    }
}

class MutableMBackedFXObservableList<E>(override val mList: MutableObsList<E>) :
    MBackedFXObservableList<E>(mList) {

    override fun add(element: E) = mList.add(element)

    override fun set(
        index: Int,
        element: E
    ) = mList.set(index, element)

    override fun addAll(vararg elements: E) = mList.addAll(elements)
    override fun remove(
        from: Int,
        to: Int
    ) = mList.subList(from, to).clear()

    override fun retainAll(vararg elements: E) = mList.retainAll(elements)
    override fun removeAll(vararg elements: E) = mList.removeAll(elements)
    override fun setAll(col: MutableCollection<out E>): Boolean {
        mList.setAll(col)
        return true
    }

    override fun setAll(vararg elements: E): Boolean {
        mList.setAll(*elements)
        return true
    }
}
