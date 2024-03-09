package matt.fx.graphics.wrapper.node.tri

import matt.fx.graphics.wrapper.node.shape.poly.PolygonWrapper
import matt.obs.prop.writable.BindableProperty

class Triangle : PolygonWrapper() {
    private fun Double.hToTri(): DoubleArray {
        val h = this / 2
        return listOf(
            0.0,
            0.0,
            0.0,
            h,
            h,
            (h / 2)
        ).toDoubleArray()
    }

    val sideProperty = BindableProperty(2.0)

    init {
        points.bind(
            sideProperty
        ) {
            it.hToTri().toList()
        }
    }
}
