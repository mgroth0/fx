package matt.fx.node.chart.hist

import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import matt.fig.render.Histogram
import matt.fx.control.popup.tooltip.fixed.tooltip
import matt.fx.control.wrapper.label.label
import matt.fx.graphics.wrapper.inter.titled.Titled
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.shape.rect.rectangle
import matt.fx.graphics.wrapper.pane.pane
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.model.data.mathable.MathAndComparable
import matt.obs.bind.binding
import matt.obs.math.double.op.div
import matt.obs.math.double.op.minus
import matt.obs.math.double.op.times
import matt.obs.prop.BindableProperty
import matt.obs.prop.VarProp


class HistogramImpl<Y : MathAndComparable<Y>> : RegionWrapperImpl<Region, NodeWrapper>(VBox()), Titled, Histogram<Y> {



    private val root = node as VBox

    override val titleProperty = BindableProperty<String?>("insert title here")

    override var title by titleProperty


    private val dataProperty = VarProp(listOf<Y>())
    override var data by dataProperty

    val numBins = VarProp(5)

    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        if (index != null) {
            root.children.add(index, child.node)
        } else {
            root.children.add(child.node)
        }

    }


    @Synchronized
    fun refresh() {

        val hist = this

        val safeNumBins = numBins.value
        val safeData = data.toList()




        root.children.clear()

        label {
            textProperty.bind(hist.titleProperty.binding { it ?: "" })
        }

        val num = safeData.size
        if (num > 0) {

            val minX = safeData.minOf { it }
            val maxX = safeData.maxOf { it }

            val range = maxX - minX
            val binRatio = 1.0 / safeNumBins
            val bins = List(safeNumBins) {
                Bin(
                    minX = minX + (range * (binRatio * it)),
                    maxX = minX + (range * (binRatio * (it + 1)))
                )
            }

            safeData.forEach { d ->
                bins.first { b ->
                    b.minX <= d && b.maxX >= d
                }.count += 1
            }

            val max = bins.maxOf { it.count }

            pane<NW> {

                vgrow = ALWAYS

                val thePane = this

                val widths = thePane.widthProperty / safeNumBins.toDouble()


                bins.forEachIndexed { idx, bin ->

                    rectangle {
                        fill = Color.BLUE
                        widthProperty.bind(widths)
                        heightProperty.bind(
                            thePane.heightProperty * (bin.count.toDouble() / max)
                        )
                        xProperty.bind(widths * idx.toDouble())
                        yProperty.bind(
                            thePane.heightProperty - heightProperty
                        )

                        tooltip(bin.toString())


                    }
                }


            }

        }


    }

    init {
        root.isFillWidth = true

        refresh()
        dataProperty.onChange {
            refresh()
        }
        numBins.onChange {
            refresh()
        }

    }


    data class Bin<X : MathAndComparable<X>>(
        val minX: X,
        val maxX: X,
        var count: Int = 0
    )

}

