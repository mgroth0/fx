package matt.fx.graphics.service

import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Parent
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.anno.Open
import matt.lang.convert.BiConverter
import matt.service.MattService
import matt.service.ServiceHub
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface WrapperService : MattService {
    fun <E : EventTarget> wrapped(e: E): EventTargetWrapper

    @Open
    fun <E : EventTarget, W : EventTargetWrapper> wrapperConverter(
        eClass: KClass<E>,
        wClass: KClass<W>
    ) =
        object : BiConverter<E, W> {
            override fun convertToB(a: E): W = wClass.cast(wrapped(a))

            override fun convertToA(b: W): E = eClass.cast(b.node)
        }
}

object WrapperServiceHub : ServiceHub<WrapperService>()

internal fun EventTarget.wrapped() = WrapperServiceHub.get().wrapped(this)
internal fun Node.wrapped() = WrapperServiceHub.get().wrapped(this) as NodeWrapper

fun <E : EventTarget, W : EventTargetWrapper> wrapperConverter(
    eClass: KClass<E>,
    wClass: KClass<W>
) =
    WrapperServiceHub.get().wrapperConverter<E, W>(eClass = eClass, wClass = wClass)

inline fun <reified E : EventTarget, reified W : EventTargetWrapper> nullableWrapperConverter(): BiConverter<E?, W?> =
    nullableWrapperConverter(
        eClass = E::class,
        wClass = W::class
    )
fun <E : EventTarget,  W : EventTargetWrapper> nullableWrapperConverter(
    eClass: KClass<E>,
    wClass: KClass<W>
): BiConverter<E?, W?> =
    wrapperConverter(
        eClass = eClass,
        wClass = wClass
    ).nullable()

val nullableNodeConverter by lazy {
    nullableWrapperConverter(
        Node::class,
        NodeWrapper::class
    )
}
val nullableParentConverter by lazy {
    nullableWrapperConverter(
        Parent::class,
        NodeWrapper::class
    )
}



