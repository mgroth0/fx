package matt.fx.control.tfx.listview

/*slightly modified code I stole from tornadofx*/

import javafx.beans.property.Property
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.wrapped


/**
 * Execute action when the enter key is pressed or the mouse is clicked
 * @param clickCount The number of mouse clicks to trigger the action
 * @param action The runnable to execute on select
 */
fun <T> ListView<T>.onUserSelect(clickCount: Int = 2, action: (T) -> Unit) {
    addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
        val selectedItem = this.selectedItem
        if (event.clickCount == clickCount && selectedItem != null && event.target.wrapped().isInsideRow())
            action(selectedItem)
    }

    addEventFilter(KeyEvent.KEY_PRESSED) { event ->
        val selectedItem = this.selectedItem
        if (event.code == KeyCode.ENTER && !event.isMetaDown && selectedItem != null)
            action(selectedItem)
    }
}

val <T> ListView<T>.selectedItem: T?
    get() = selectionModel.selectedItem


fun <T> ListView<T>.onUserDelete(action: (T) -> Unit) {
    addEventFilter(KeyEvent.KEY_PRESSED) { event ->
        val selectedItem = this.selectedItem
        if (event.code == KeyCode.BACK_SPACE && selectedItem != null)
            action(selectedItem)
    }
}

class ListCellCache<T>(private val cacheProvider: (T) -> Node) {
    private val store = mutableMapOf<T, Node>()
    fun getOrCreateNode(value: T) = store.getOrPut(value, { cacheProvider(value) })
}





fun <T> ListView<T>.bindSelected(property: Property<T>) {
    selectionModel.selectedItemProperty().onChange {
        property.value = it
    }
}

fun <T> ListView<T>.multiSelect(enable: Boolean = true) {
    selectionModel.selectionMode = if (enable) SelectionMode.MULTIPLE else SelectionMode.SINGLE
}
