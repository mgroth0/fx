package matt.fx.base.rewrite

import kotlin.reflect.KClass

/*


JavaFX Classes I Completely Rewrote
I MUST REFER TO THIS WHEN UPDATING JAVAFX

ALSO:

RESOURCES
CSS
ALL CSS FILES

*/
annotation class ReWrittenFxClass(
    val original: KClass<*> = Any::class,
    val originalName: String = ""
)
