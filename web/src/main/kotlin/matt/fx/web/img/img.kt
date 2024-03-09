package matt.fx.web.img

import javafx.event.EventHandler
import kotlinx.coroutines.NonCancellable
import matt.async.thread.daemon
import matt.file.JioFile
import matt.file.construct.toMFile
import matt.file.toJioFile
import matt.fx.graphics.fxthread.runLaterReturn
import matt.fx.web.WebViewWrapper
import matt.fx.web.interceptConsole
import matt.fx.web.refreshImages
import matt.gui.refresh.refreshWhileInSceneEvery
import matt.lang.model.file.FsFile
import matt.lang.model.file.MacFileSystem
import matt.time.dur.common.sec
import org.jsoup.Jsoup
import java.nio.file.attribute.FileTime

fun ImageRefreshingWebView(fil: FsFile) =
    WebViewWrapper().apply {
        val file = fil.toJioFile()
        engine.onError = EventHandler { event -> System.err.println(event) }


        var refreshThisCycle = false

        interceptConsole()

        engine.loadWorker.stateProperty().addListener { _, _, new ->
            println("${file.name}:loadstate:$new")
            refreshThisCycle = true
        }


        engine.load(file.toUri().toString())
        daemon(name = "ImageRefreshingWebView thread") {
            val imgFiles = mutableMapOf<JioFile, FileTime>()
            Jsoup.parse(file.readText()).allElements.forEach {
                if (it.tag().name == "img") {
                    val src = it.attributes()["src"]
                    val imgFile =
                        file.parent!!.resolve(src).normalize().toFile().toMFile(MacFileSystem)
                    imgFiles[imgFile] = imgFile.toJioFile().lastModified()
                }
            }
            println("watching mtimes of:")
            for (entry in imgFiles) {
                println("\t" + entry.key.toString())
            }

            refreshWhileInSceneEvery(2.sec) {
                @Suppress("DEPRECATION")
                if (!file.toJioFile().exists()) NonCancellable.cancel()

                for (entry in imgFiles) {
                    if (entry.key.lastModified() != entry.value) {
                        imgFiles[entry.key] = entry.key.lastModified()
                        refreshThisCycle = true
                    }
                }
                if (refreshThisCycle) {
                    refreshThisCycle = false
                    runLaterReturn {
                        println("executing js refresh!")
                        engine.executeScript(refreshImages)
                    }
                }
            }
        }
    }
