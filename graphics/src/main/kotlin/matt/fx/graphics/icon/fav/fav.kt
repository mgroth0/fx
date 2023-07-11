package matt.fx.graphics.icon.fav

import com.sun.javafx.application.PlatformImpl.runLater
import javafx.scene.image.Image
import matt.collect.map.dmap.DefaultStoringMap
import matt.collect.map.dmap.withStoringDefault
import matt.fx.graphics.wrapper.imageview.ImageViewWrapper
import matt.fx.image.toFXImage
import matt.lang.url.getHostName
import matt.lang.url.toURL
import matt.log.warn.warn
import matt.model.code.errreport.ThrowReport
import java.awt.image.BufferedImage
import java.io.FileNotFoundException
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URL
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import kotlin.concurrent.thread


/*

NOTES

svg or im?

tried to cache b4 but was hard because wasn't always a raster


does JavaFX ImageView load asynchronously?
does JavaFX ImageView have a backup image option?

http://www.google.com/s2/favicons?domain_url=https://docs.google.com/document/d/18h1lPGGLuJaGkBJ-NC2bTlfiBSt7xS4MZAnXkEfFwys
 val faviconUrl = String.format(
	"http://www.google.com/s2/favicons?domain_url=${location}", URLEncoder.encode(location, "UTF-8")
)

https://stackoverflow.com/questions/28474791/javafx-2-2-image-support-for-ico

this just gets the small one!!
val im = ImageIO.read(URI(faviconUrl).toURL()).toFXImage()

* */

object FaviconLoader {


    @Synchronized
    fun loadSynchronously(url: URL): BufferedImage? {
        return FAVICON_CACHE[url]
    }


    private val nextThreadID = AtomicInteger()

    fun loadAsynchronously(
        url: URL,
        backupImage: Image,
        fitWidth: Double,
        fitHeight: Double
    ): ImageViewWrapper {
        val iv = ImageViewWrapper()
        iv.isPreserveRatio = true
        iv.fitHeight = fitHeight
        iv.fitWidth = fitWidth
        iv.image = backupImage
        thread(isDaemon = true, name = "Fav Loader ${nextThreadID.getAndIncrement()}") {
            val loaded = loadSynchronously(url)
            if (loaded != null) {
                runLater {
                    /*trying to put FX image inside the FX thread to see if that helps*/
                    iv.image = loaded.toFXImage()
                }
            }
        }
        return iv
    }


    private val FAVICON_CACHE: DefaultStoringMap<URL, BufferedImage?> by lazy {
        WeakHashMap<URL, BufferedImage?>().withStoringDefault {
            if (it == null) null
            else loadFavicon(it.toString())
        }
    }


    private fun loadFavicon(location: String): BufferedImage? {
        val faviconUrl = location.toURL().getHostName() + "favicon.ico"
        val ims = try {
            val stream = ImageIO.createImageInputStream(URI(faviconUrl).toURL().openStream())
            val readerIterator = ImageIO.getImageReaders(stream)
            if (!readerIterator.hasNext()) {
                println("READER ITERATOR HAS NO NEXT")
                return null
            }
            readerIterator.next().run {
                input = stream
                val images = mutableListOf<BufferedImage>()
                for (i in 0..<getNumImages(true)) {
                    val image = this.read(
                        i,
                        null
                    )
                    images += image
                }
                images
            }
        } catch (e: FileNotFoundException) {
            ThrowReport(e).print()
            return null
        } catch (e: IOException) {
            ThrowReport(e).print()
            return null
        }
        /*must to toFXImage in a safe place. I think that when the thread gets an exception in the middle of to a BufferedIm to FXIm conversion, it might cause JavaFX to crash ... or maybe not but this is still safer and more performant in general*/
        val im = ims.maxBy { it.height }
        return try {
            im
        } catch (ex: UnsupportedEncodingException) {
            warn("$ex for $faviconUrl")
            null
        } catch (ex: UnknownFormatConversionException) {
            warn("$ex for $faviconUrl")
            null
        }
    }

}




