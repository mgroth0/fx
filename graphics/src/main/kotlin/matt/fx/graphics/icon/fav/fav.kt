package matt.fx.graphics.icon.fav

import com.sun.javafx.application.PlatformImpl.runLater
import javafx.scene.image.Image
import matt.async.thread.namedThread
import matt.collect.map.dmap.DefaultStoringMap
import matt.collect.map.dmap.withStoringDefault
import matt.fx.graphics.wrapper.imageview.ImageViewWrapper
import matt.fx.image.toFXImage
import matt.image.fav.tryToLoadImageStreamAndTakeLargestImage
import matt.lang.atomic.AtomicInt
import matt.lang.model.value.Value
import matt.lang.url.getHostName
import matt.log.warn.warn
import matt.model.code.errreport.ThrowReport
import matt.model.data.rect.DoubleRectSize
import java.awt.image.BufferedImage
import java.net.URI
import java.net.URL
import java.util.*

object FaviconLoader {


    @Synchronized
    fun loadSynchronously(url: URL): BufferedImage? {
        return FAVICON_CACHE[url].value
    }


    private val nextThreadID = AtomicInt()

    fun loadAsynchronously(
        url: URL,
        backupImage: Image,
        fitSize: DoubleRectSize
    ): ImageViewWrapper {
        val iv = ImageViewWrapper()
        iv.isPreserveRatio = true
        iv.fitHeight = fitSize.height
        iv.fitWidth = fitSize.width
        iv.image = backupImage
        namedThread(isDaemon = true, name = "Fav Loader ${nextThreadID.getAndIncrement()}") {
            val loaded = loadSynchronously(url)
            if (loaded != null) {
                runLater {
                    /*trying to put FX image inside the FX thread to see if that helps*/
                    iv.image = loaded.toFXImage()
                    /*must to toFXImage in a safe place. I think that when the thread gets an exception in the middle of to a BufferedIm to FXIm conversion, it might cause JavaFX to crash ... or maybe not but this is still safer and more performant in general*/
                }
            }
        }
        return iv
    }


    private val FAVICON_CACHE: DefaultStoringMap<URL, Value<BufferedImage?>> by lazy {
        WeakHashMap<URL, Value<BufferedImage?>>().withStoringDefault {
            Value(
                if (it == null) null
                else {
                    val faviconUrl = URI(it.getHostName() + "favicon.ico").toURL()
                    tryToLoadImageStreamAndTakeLargestImage(faviconUrl, onMinorException = {
                        warn("$it for $faviconUrl")
                        ThrowReport(it).print()
                    })
                }
            )
        }
    }


}




