package matt.fx.base.wrapper.obs.collect.set

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import matt.collect.map.FakeMutableSet
import matt.lang.function.Consume
import matt.model.op.convert.Converter
import matt.model.op.prints.Prints
import matt.obs.bindhelp.BindableSet
import matt.obs.bindhelp.BindableSetImpl
import matt.obs.col.change.AddIntoSet
import matt.obs.col.change.ClearSet
import matt.obs.col.change.MultiAddIntoSet
import matt.obs.col.change.MultiRemovalFromSet
import matt.obs.col.change.RemoveElementFromSet
import matt.obs.col.change.SetChange
import matt.obs.col.change.SetRemoval
import matt.obs.col.oset.MutableObsSet
import matt.obs.col.oset.ObsSet
import matt.obs.listen.MyListenerInter
import matt.obs.listen.SetListener
import matt.obs.listen.SetListenerBase
import matt.obs.listen.update.SetUpdate
import matt.obs.prop.ObsVal

fun <E> ObservableSet<E>.createImmutableWrapper() = FXBackedImmutableObservableSet(this)
fun <E> ObservableSet<E>.createMutableWrapper() = FXBackedMutableObservableSet(this)

/*"ObservableSetWrapper" is taken*/
interface MyObservableSetWrapper<E>: Observable {
  fun addListener(listener: SetChangeListener<E>)
  fun removeListener(listener: SetChangeListener<E>)
}

fun <E> MyObservableSetWrapper<E>.onAdd(op: Consume<E>) = listen(onAdd = op, onRemove = {})
fun <E> MyObservableSetWrapper<E>.onRemove(op: Consume<E>) = listen(onAdd = { }, onRemove = op)

fun <E> MyObservableSetWrapper<E>.listen(
  onAdd: ((E)->Unit),
  onRemove: ((E)->Unit),
) {
  addListener(SetChangeListener {
	if (it.wasAdded()) {
	  onAdd(it.elementAdded)
	}
	if (it.wasRemoved()) {
	  onRemove(it.elementRemoved)
	}
  })
}


fun <E> MyObservableSetWrapper<E>.changeListener(op: (SetChangeListener.Change<out E>)->Unit) = run {
  val l = SetChangeListener<E> { op(it) }
  addListener(l)
  l
}

fun <E> MyObservableSetWrapper<E>.onChange(op: (SetChangeListener.Change<out E>)->Unit) = apply {
  addListener(SetChangeListener { op(it) })
}


interface MyObservableSetWrapperPlusSet<E>: MyObservableSetWrapper<E>, Set<E>
//interface MyObservableSetWrapperPlusMutableSet<E>: MyObservableSetWrapperPlusSet<E>, MutableSet<E>

fun <E> mfxSetConverter() = object: Converter<ObservableSet<E>, ObsSet<E>> {
  override fun convertToB(a: ObservableSet<E>): ObsSet<E> {
	@Suppress("UNCHECKED_CAST")
	return when (a) {
	  is MBackedFXObservableSet<*> -> a.mSet as ObsSet<E>
	  else                         -> a.createMutableWrapper()
	}
  }

  override fun convertToA(b: ObsSet<E>): ObservableSet<E> {
	@Suppress("UNCHECKED_CAST")
	return when (b) {
	  is FXBackedObsSet<*> -> b.obs as ObservableSet<E>
	  else                 -> b.createFXWrapper()
	}
  }
}

fun <E> mfxMutableSetConverter() = object: Converter<ObservableSet<E>, MutableObsSet<E>> {
  override fun convertToB(a: ObservableSet<E>): MutableObsSet<E> {
	@Suppress("UNCHECKED_CAST")
	return when (a) {
	  is MBackedFXObservableSet<*> -> a.mSet as MutableObsSet<E>
	  else                         -> a.createMutableWrapper()
	}
  }

  override fun convertToA(b: MutableObsSet<E>): ObservableSet<E> {
	@Suppress("UNCHECKED_CAST")
	return when (b) {
	  is FXBackedObsSet<*> -> b.obs as ObservableSet<E>

	  else                 -> b.createMutableFXWrapper()
	}
  }
}

abstract class FXBackedObsSet<E>(internal val obs: ObservableSet<E>)

abstract class ObservableSetWrapperImpl<E>(obs: ObservableSet<E>): FXBackedObsSet<E>(obs),
																   MyObservableSetWrapperPlusSet<E>,
																   Observable by obs {
  override fun addListener(listener: SetChangeListener<E>) = obs.addListener(listener)
  override fun removeListener(listener: SetChangeListener<E>) = obs.removeListener(listener)

}

interface FXOSetWrapperAndBasic<E>: MyObservableSetWrapper<E>, ObsSet<E>, BindableSet<E>
interface FXOLMutableSetWrapperAndBasic<E>: FXOSetWrapperAndBasic<E>, MutableSet<E>

class FXBackedMutableObservableSetBase<E>(obs: ObservableSet<E>): ObservableSetWrapperImpl<E>(obs),
																  MutableObsSet<E>,
																  MutableSet<E> by obs,
																  FXOLMutableSetWrapperAndBasic<E>,
																  BindableSet<E> {


  val bindableSetHelper by lazy { BindableSetImpl(this) }
  override fun <S> bind(source: ObsSet<S>, converter: (S)->E) = bindableSetHelper.bind(source, converter)
  override fun <T> bind(source: ObsVal<T>, converter: (T)->Set<E>) = bindableSetHelper.bind(source, converter)
  override val bindManager get() = bindableSetHelper.bindManager
  override var theBind
	get() = bindManager.theBind
	set(value) {
	  bindManager.theBind = value
	}

  override fun unbind() = bindableSetHelper.unbind()


  override var nam: String? = null

  override var debugger: Prints? = null

  @Synchronized
  override fun onChange(listenerName: String?, op: (SetChange<E>)->Unit): SetListener<E> {
	val listener = SetListener {
	  op(it)
	}
	if (listenerName != null) {
	  listener.name = listenerName
	}
	addListener(listener)
	return listener
  }


  private val listenersMap = mutableMapOf<MyListenerInter<*>, SetChangeListener<E>>()

  @Synchronized
  override fun addListener(listener: SetListenerBase<E>): SetListenerBase<E> {
	val oListener = SetChangeListener<E> {
	  val t = tic(prefix = "invoking $listener for $this", enabled = debugger != null)
	  t.toc("start")

	  if (it.wasRemoved()) {
		listener.notify(SetUpdate(RemoveElementFromSet(obs, it.elementRemoved)))
	  }
	  if (it.wasAdded()) {
		listener.notify(SetUpdate(AddIntoSet(obs, it.elementAdded)))
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


}


class FXBackedImmutableObservableSet<E>(obs: ObservableSet<E>): FXBackedObsSet<E>(obs),
																FXOSetWrapperAndBasic<E> by FXBackedMutableObservableSetBase(
																  obs
																)


class FXBackedMutableObservableSet<E>(obs: ObservableSet<E>): FXBackedObsSet<E>(obs),
															  FXOLMutableSetWrapperAndBasic<E> by FXBackedMutableObservableSetBase(
																obs
															  ),
															  MutableObsSet<E>

fun <E> MutableObsSet<E>.createMutableFXWrapper() = MutableMBackedFXObservableSet(this)
fun <E> ObsSet<E>.createFXWrapper() = MBackedFXObservableSet(this)
open class MBackedFXObservableSet<E>(internal open val mSet: ObsSet<E>): ObservableSet<E>,
																		 MutableSet<E> by FakeMutableSet(mSet) {
  //  override fun addAll(vararg elements: E): Boolean = ILLEGAL
  //  override fun retainAll(vararg elements: E): Boolean = ILLEGAL
  //  override fun removeAll(vararg elements: E): Boolean = ILLEGAL
  //  override fun matt.lang.setall.setAll(col: MutableCollection<out E>): Boolean = ILLEGAL
  //  override fun matt.lang.setall.setAll(vararg elements: E): Boolean = ILLEGAL

  private val invalidationListenerMap = mutableMapOf<InvalidationListener, MyListenerInter<*>>()
  private val changeListenerMap = mutableMapOf<SetChangeListener<in E>, MyListenerInter<*>>()
  override fun addListener(listener: InvalidationListener) {
	invalidationListenerMap[listener] = mSet.observe {
	  listener.invalidated(this)
	}
  }

  override fun removeListener(listener: InvalidationListener) {
	invalidationListenerMap[listener]?.tryRemovingListener()
  }

  override fun addListener(listener: SetChangeListener<in E>) {
	changeListenerMap[listener] = mSet.onChange {

	  listener.onChanged(object: SetChangeListener.Change<E>(this) {


		override fun getElementAdded() = when (it) {
		  is SetRemoval          -> null
		  is ClearSet            -> null
		  is AddIntoSet          -> it.added
		  is MultiAddIntoSet     -> TODO()
		  is MultiRemovalFromSet -> null
		}

		override fun getElementRemoved() = when (it) {
		  is SetRemoval          -> it.removed
		  is ClearSet            -> TODO()
		  is AddIntoSet          -> null
		  is MultiAddIntoSet     -> null
		  is MultiRemovalFromSet -> TODO()
		}

		override fun wasAdded() = when (it) {
		  is SetRemoval          -> false
		  is ClearSet            -> false
		  is AddIntoSet          -> true
		  is MultiAddIntoSet     -> true
		  is MultiRemovalFromSet -> false
		}

		override fun wasRemoved() = when (it) {
		  is SetRemoval          -> true
		  is ClearSet            -> true
		  is AddIntoSet          -> false
		  is MultiAddIntoSet     -> false
		  is MultiRemovalFromSet -> true
		}


	  })


	}
  }

  override fun removeListener(listener: SetChangeListener<in E>) {
	changeListenerMap[listener]?.tryRemovingListener()
  }
}

class MutableMBackedFXObservableSet<E>(override val mSet: MutableObsSet<E>): MBackedFXObservableSet<E>(mSet)