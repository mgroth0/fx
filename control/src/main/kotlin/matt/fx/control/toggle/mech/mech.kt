@file:Suppress("UNCHECKED_CAST")

/*slightly modified code I stole from tornadofx*/

package matt.fx.control.toggle.mech

import matt.collect.weak.WeakMap
import matt.fx.control.inter.select.SelectableValue
import matt.fx.control.wrapper.button.radio.RadioButtonWrapper
import matt.fx.control.wrapper.button.radio.radiobutton
import matt.fx.graphics.wrapper.FXNodeWrapperDSL
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.weak.common.WeakRefInter
import matt.lang.weak.weak
import matt.log.warn.common.warn
import matt.model.flowlogic.keypass.KeyPass
import matt.model.flowlogic.recursionblocker.RecursionBlocker
import matt.obs.col.change.SetAdditionBase
import matt.obs.col.change.SetRemovalBase
import matt.obs.col.oset.basicObservableSetOf
import matt.obs.listen.MyListenerInter
import matt.obs.prop.writable.BindableProperty


fun <V: Any> NW.toggles(
    initialValue: V,
    op: ToggleDSL<V>.() -> Unit = {}
): ToggleMechanism<V> {
    val dsl = ToggleDSL<V>(this).apply(op)
    dsl.mech.selectedValue.value = initialValue
    return dsl.mech
}

@FXNodeWrapperDSL class ToggleDSL<V: Any>(private val nw: NodeWrapper) {
    internal val mech = ToggleMechanism<V>()
    fun radio(text: String, v: V, op: RadioButtonWrapper.() -> Unit = {}) =
        nw.radiobutton(text = text, group = mech, value = v, op = op)
}


/*ToggleGroup was a better name, but that is taken... */
class ToggleMechanism<V: Any>() {


    val toggles = basicObservableSetOf<WeakRefInter<SelectableValue<V>>>()
    fun derefToggles(): Set<SelectableValue<V>> {
        val itr = toggles.iterator()
        val r = mutableSetOf<SelectableValue<V>>()
        while (itr.hasNext()) {
            val n = itr.next()
            val de = n.deref()
            if (de == null) {
                itr.remove()
            } else {
                r += de
            }
        }
        return r
    }

    val selectedToggle = BindableProperty<SelectableValue<V>?>(null)
    val selectedValue = BindableProperty<V?>(null)

    fun selectToggle(toggle: SelectableValue<V>?) {
        selectedToggle.value = toggle
    }

    fun selectValue(value: V?) {
        selectedValue.value = value
    }

    init {
        val rBlocker = RecursionBlocker()
        selectedToggle.onChangeWithOld { old, new ->
            old?.selectedProperty?.value = false
            new?.selectedProperty?.value = true
            require(new == null || new in derefToggles())
            rBlocker.with {
                selectedValue.value = new?.value
            }
        }
        selectedValue.onChange { newVal ->
            rBlocker.with {
                selectedToggle.value =
                    newVal?.let {
                        derefToggles().first { it.value == newVal }
                    }
            }
        }
    }

    private val listeners = WeakMap<SelectableValue<V>, MyListenerInter<*>>()

    private val selecting = KeyPass()
    private fun didSelectToggle(toggle: SelectableValue<V>) =
        selecting.with {
            derefToggles().filter { it != toggle }.forEach {
                it.isSelected = false
            }
            selectedToggle.value = toggle
        }

    private fun didUnSelectToggle(toggle: SelectableValue<V>) {
        if (selecting.isNotHeld) {
            val theVal = selectedToggle.value
            if (theVal != toggle) {
                warn(
                    "odd issue: expected selectedToggle.value to be $toggle but it was $theVal. I don't , but it still should be figured out."
                )
            }
            selectedToggle.value = null
        }
    }

    fun hasSelection() = selectedValue.value != null

    fun removeToggle(s: SelectableValue<V>) {
        toggles.removeAll { it.deref() == s }
    }

    fun addToggle(s: SelectableValue<V>) {
        toggles.add(weak(s))
    }

    init {
        toggles.onChange { change ->
            (change as? SetAdditionBase)?.addedElements?.forEach { toggle ->
                val togg = toggle.deref()!!
                println("adding tog listener")
                listeners[togg] =
                    togg.selectedProperty.onChangeWithAlreadyWeak(toggle) { tog: SelectableValue<V>, sel ->
                        /*println("selectedProperty of


		 toggle ${toggle} changed to ${it}")
		  println("selectedToggle.value=${selectedToggle.value}")
		  println("selectedValue.value=${selectedValue.value}")*/

                        println("tog selected: $tog, $sel")


                        if (sel) didSelectToggle(tog)
                        else didUnSelectToggle(tog)
                    }
                if (togg.isSelected) {
                    if (hasSelection()) togg.isSelected = false
                    else selectedToggle.value = togg
                }
            }
            (change as? SetRemovalBase)?.removedElements?.forEach { toggle ->
                if (selectedToggle.value == toggle) {
                    selectedToggle.value = null
                }
                listeners.remove(toggle.deref())?.removeListener()
            }
        }
    }
}
