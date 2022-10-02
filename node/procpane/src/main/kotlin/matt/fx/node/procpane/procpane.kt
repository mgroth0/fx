package matt.fx.node.procpane


import javafx.application.Platform.runLater
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Priority.ALWAYS
import matt.async.thread.daemon
import matt.auto.process.destroyNiceThenForceThenWait
import matt.file.MFile
import matt.file.commons.REGISTERED_FOLDER
import matt.fx.graphics.lang.actionbutton
import matt.fx.graphics.menu.context.mcontextmenu
import matt.fx.node.console.Console
import matt.fx.node.console.ProcessConsole
import matt.fx.node.procpane.inspect.ProcessInspectPane
import matt.fx.node.procpane.status.StatusFolderWatchPane
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.getValue
import matt.hurricanefx.eye.prop.setValue
import matt.hurricanefx.wrapper.control.button.ButtonWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.log.logInvokation
import matt.obs.bindings.bool.not
import matt.obs.bindings.math.times
import matt.obs.prop.BindableProperty

val STATUS_FOLDER = REGISTERED_FOLDER + "status"

interface ConsoleNode {
  val console: Console
}

@Suppress("SpellCheckingInspection")
interface ProcessNode: ConsoleNode {
  val name: String
  fun rund(): Thread
  fun stopd(): Thread
  val runningProp: BindableProperty<Boolean>
  var process: Process?
}


@Suppress("unused")
class ProcessConsolePane(
  override val name: String,
  private val processBuilder: ProcessBuilder,
  val statusFolder: MFile? = null,
): VBoxWrapperImpl<NodeWrapper>(), ProcessNode {

  fun clone() = ProcessConsolePane(
	name = name,
	processBuilder = processBuilder,
	statusFolder = statusFolder
  )

  constructor(
	name: String,
	vararg command: String,
	workingDir: MFile? = null,
	statusFolder: MFile? = null,
	environmentalVars: Map<String, String> = mapOf()
  ): this(
	name,
	ProcessBuilder(
	  command.toList()
	).apply {
	  environment() += environmentalVars
	  directory(workingDir)
	},
	statusFolder
  )

  @Suppress("MemberVisibilityCanBePrivate")
  override val console = ProcessConsole(name)
  private var statusFolderWatchPane: StatusFolderWatchPane? = null


  private val processProp: SimpleObjectProperty<Process> = SimpleObjectProperty<Process>(null)
  override var process: Process? by processProp


  @Suppress("MemberVisibilityCanBePrivate")
  override val runningProp = BindableProperty(false).apply {
	processProp.onChange {
	  if (it == null) {
		value = false
	  } else {
		value = (it.isAlive)
		daemon {
		  it.waitFor()
		  runLater {
			value = false
		  }
		}
	  }
	}
	onChange {
	  if (!it && autoRestart) rund()
	}
  }

  //  override val runningB = runningProp.toBinding()


  @Suppress("MemberVisibilityCanBePrivate")
  var running: Boolean by runningProp

  private val autoRestartProp = BindableProperty(false).apply {
	onChange {
	  if (it && !running) {
		rund()
	  }
	}
  }
  private var autoRestart: Boolean by autoRestartProp

  val showProcessInspectPaneOption = BindableProperty(false)
  private val inspectBox = ProcessInspectPane(process).apply {

	visibleProperty.bind(this@ProcessConsolePane.showProcessInspectPaneOption)
	managedProperty.bind(
	  this@ProcessConsolePane.showProcessInspectPaneOption
	) // without this, still takes up space while invisible
	procProp.bind(this@ProcessConsolePane.processProp)
	this@ProcessConsolePane.runningProp.onChange {
	  println("runningProp:${it}")
	  runLater { refresh() }
	}
  }

  @Suppress("MemberVisibilityCanBePrivate")
  override fun rund() = logInvokation {
	daemon {
	  if (running) {
		stop()
	  }
	  statusFolderWatchPane?.start()
	  val p = processBuilder.start()
	  console.attachProcess(p)
	  runLater {
		println("WEIRD BUG HERE 1")
		process = p
		println("WEIRD BUG HERE 2")
	  }
	}
  }

  @Suppress("MemberVisibilityCanBePrivate")
  fun stop() = logInvokation {
	statusFolderWatchPane?.stop()
	val p = process
	p?.destroyNiceThenForceThenWait()
  }

  override fun stopd() = daemon { stop() }

  init {
	val vbox = this
	processProp.onChange {
	  println("processProp:${it}")
	  if (it != null) {
		console.reset()
	  }
	}
	hbox<ButtonWrapper> {
	  actionbutton("Run") {
		this@ProcessConsolePane.rund()
	  }
	  button("Stop") {
		disableProperty.bind(this@ProcessConsolePane.runningProp.not())
		setOnAction {
		  daemon {
			this@ProcessConsolePane.stop()
		  }
		}
	  }
	}
	add(console.apply {
	  prefHeightProperty.bind(vbox.heightProperty)
	  vgrow = ALWAYS
	})
	statusFolder?.let {
	  statusFolderWatchPane = StatusFolderWatchPane(it).apply {
		minHeightProperty.bind(vbox.heightProperty*(0.20))
		maxHeightProperty.bind(vbox.heightProperty*(0.20))
	  }
	  add(statusFolderWatchPane!!)
	}
	add(inspectBox.apply {
	  minHeightProperty.bind(vbox.heightProperty*(0.20))
	  maxHeightProperty.bind(vbox.heightProperty*(0.20))
	  vgrow = ALWAYS
	})
	mcontextmenu {
	  checkitem("Show process inspect pane", showProcessInspectPaneOption)
	  checkitem("Auto-Restart", autoRestartProp)
	}
  }
}