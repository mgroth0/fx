package matt.fx.web.img

import javafx.event.EventHandler
import kotlinx.coroutines.NonCancellable
import matt.async.thread.daemon
import matt.file.MFile
import matt.file.construct.toMFile
import matt.fx.graphics.fxthread.runLaterReturn
import matt.fx.graphics.refresh.refreshWhileInSceneEvery
import matt.fx.web.WebViewWrapper
import matt.fx.web.interceptConsole
import matt.fx.web.refreshImages
import matt.time.dur.sec
import org.jsoup.Jsoup

fun ImageRefreshingWebView(file: MFile) = WebViewWrapper().apply {

  engine.onError = EventHandler { event -> System.err.println(event) }


  var refreshThisCycle = false

  interceptConsole()

  engine.loadWorker.stateProperty().addListener { _, _, new ->
	println("${file.name}:loadstate:${new}")
	refreshThisCycle = true
  }


  engine.load(file.toURI().toString())
  daemon {
	val imgFiles = mutableMapOf<MFile, Long>()
	Jsoup.parse(file.readText()).allElements.forEach {
	  if (it.tag().name == "img") {
		val src = it.attributes()["src"]
		val imgFile = file.parentFile!!.toPath().resolve(src).normalize().toFile().toMFile()
		imgFiles[imgFile] = imgFile.lastModified()
	  }
	}
	println("watching mtimes of:")
	for (entry in imgFiles) {
	  println("\t" + entry.key.toString())
	}

	refreshWhileInSceneEvery(2.sec) {
	  @Suppress("DEPRECATION")
	  if (!file.exists()) NonCancellable.cancel() // NOSONAR

	  for (entry in imgFiles) {
		if (entry.key.lastModified() != entry.value) {
		  imgFiles[entry.key] = entry.key.lastModified()
		  refreshThisCycle = true
		}
	  }
	  if (refreshThisCycle) {
		refreshThisCycle = false
		//                println("refresh2(${file.absolutePath})")
		runLaterReturn {
		  println("executing js refresh!")
		  engine.executeScript(refreshImages)
		}
	  }
	}
  }
}