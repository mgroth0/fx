package matt.fx.base.mtofx

import javafx.beans.InvalidationListener
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import matt.lang.common.NOT_IMPLEMENTED
import matt.obs.listen.OldAndNewListenerImpl
import matt.obs.listen.OldNewListener
import matt.obs.listen.update.ValueChange
import matt.obs.prop.ObsVal
import matt.obs.prop.writable.WritableMObservableVal
import matt.service.scheduler.Scheduler

fun <T> ObsVal<T>.createROFXPropWrapper(scheduler: Scheduler? = null): ReadOnlyObjectProperty<T> {
    val w = ReadOnlyObjectWrapper(value)
    if (scheduler != null) onChange {
        scheduler.schedule {
            w.value = it
        }
    }
    else onChange {
        w.value = it
    }
    return w.readOnlyProperty
}


fun <T> WritableMObservableVal<T, ValueChange<T>, OldNewListener<T>>.createWritableFXPropWrapper(): Property<T> = MtoFXWritablePropWrapper(this)

class MtoFXWritablePropWrapper<T>(val p: WritableMObservableVal<T, ValueChange<T>, OldNewListener<T>>): Property<T> {
    override fun addListener(listener: ChangeListener<in T>) {
        p.addListener(
            OldAndNewListenerImpl { o, n ->
                listener.changed(this@MtoFXWritablePropWrapper, o, n)
            }
        )
    }

    override fun addListener(listener: InvalidationListener) {
        p.onChange {
            listener.invalidated(this)
        }
    }

    override fun removeListener(listener: ChangeListener<in T>) = NOT_IMPLEMENTED
    override fun removeListener(listener: InvalidationListener) = NOT_IMPLEMENTED

    override fun getValue(): T = p.value

    override fun getBean() = NOT_IMPLEMENTED
    override fun getName() = NOT_IMPLEMENTED

    override fun setValue(value: T) {
        p.value = value
    }

    override fun bind(observable: ObservableValue<out T>) = NOT_IMPLEMENTED
    override fun unbind() = NOT_IMPLEMENTED
    override fun isBound(): Boolean = NOT_IMPLEMENTED
    override fun bindBidirectional(other: Property<T>) = NOT_IMPLEMENTED
    override fun unbindBidirectional(other: Property<T>) = NOT_IMPLEMENTED
}
