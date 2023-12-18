package matt.fx.node.procpane


import javafx.animation.Timeline
import javafx.application.Platform.runLater
import javafx.beans.property.StringProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority.ALWAYS
import matt.async.thread.daemon
import matt.auto.process.destroyNiceThenForceThenWait
import matt.fx.base.time.toFXDuration
import matt.fx.control.lang.actionbutton
import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.control.wrapper.control.button.button
import matt.fx.graphics.anim.animation.keyframe
import matt.fx.graphics.anim.animation.timeline
import matt.fx.graphics.fxthread.runLaterReturn
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.visibleWhen
import matt.fx.graphics.wrapper.pane.hbox.hbox
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.fx.graphics.wrapper.text.text
import matt.fx.node.console.Console
import matt.fx.node.console.ProcessConsole
import matt.fx.node.procpane.inspect.ProcessInspectPane
import matt.gui.menu.context.mcontextmenu
import matt.lang.file.toJFile
import matt.lang.model.file.FsFile
import matt.lang.trip
import matt.log.logInvocation
import matt.obs.bindings.bool.not
import matt.obs.math.double.op.times
import matt.obs.prop.BindableProperty
import matt.obs.prop.VarProp
import matt.obs.prop.toggle
import matt.time.ONE_MINUTE
import java.lang.Thread.sleep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit


interface ConsoleNode {
    val console: Console
}

@Suppress("SpellCheckingInspection")
interface ProcessNode : ConsoleNode {
    val name: String
    fun rund(): Thread
    fun stopd(): Thread
    val runningProp: BindableProperty<Boolean>
    var process: Process?
}


context(matt.shell.context.ReapingShellExecutionContext)
@Suppress("unused")
class ProcessConsolePane(
    override val name: String,
    val processBuilder: ProcessBuilder,
) : VBoxWrapperImpl<NodeWrapper>(), ProcessNode {

    fun clone() = ProcessConsolePane(
        name = name,
        processBuilder = processBuilder,
    )

    constructor(
        name: String,
        vararg command: String,
        workingDir: FsFile? = null,
        statusFolder: FsFile? = null,
        environmentalVars: Map<String, String> = mapOf()
    ) : this(
        name, ProcessBuilder(
            command.toList()
        ).apply {
            environment() += environmentalVars
            directory(workingDir?.toJFile())
        }
    )

    @Suppress("MemberVisibilityCanBePrivate")
    override val console = ProcessConsole(name)


    private val processProp: VarProp<Process?> = VarProp(null)
    override var process: Process? by processProp

    private var runTime = ""

    @Suppress("MemberVisibilityCanBePrivate")
    override val runningProp = BindableProperty(false).apply {
        processProp.onChange {
            if (it == null) {
                value = false
            } else {
                val alive = it.isAlive
                value = alive
                if (alive) startProcessWatchThreads(it)
            }
        }
        onChange {
            if (!it && autoRestart) rund()
        }
    }

    private val timerMonitor = object {}

    /*MESSY, LIKELY THERE ARE RACE CONDITIONS*/
    private fun startProcessWatchThreads(p: Process) {
        var stopTime: Long? = null
        daemon("startProcessWatchThreads Thread 1") {
            p.waitFor()
            stopTime = System.currentTimeMillis()
            runLater {
                runningProp v false
            }
        }
        daemon("startProcessWatchThreads Thread 2") {
            synchronized(timerMonitor) {
                val timerOps = listOf(
                    msTimerTimeline!! to 1.milliseconds trip 1000,
                    secTimerTimeline!! to 5.milliseconds trip 60_000,
                    minTimerTimeline!! to 50.milliseconds trip null
                )
                timerOps.forEach { (timeline, interval, breakMs) ->
                    val sleepTime = interval.inWholeMilliseconds
                    if (stopTime != null) return@forEach
                    timeline.play()
                    while (stopTime == null) {
                        val t = System.currentTimeMillis() - startTime!!
                        runTime = formatMillis(t)
                        if (breakMs == null || t < breakMs) {
                            sleep(sleepTime)
                        } else break
                    }
                    timeline.pause()
                }

                runLaterReturn {
                    theTimerText!!.text = formatMillis(stopTime!!.minus(startTime!!))
                }

            }
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

    private var startTime: Long? = null

    @Suppress("MemberVisibilityCanBePrivate")
    override fun rund() = logInvocation {
        daemon("rund Thread") {
            if (running) {
                stop()
            }
            val p = processBuilder.start()
            startTime = System.currentTimeMillis()
            ensureProcessEndsAtShutdown(p)
            console.attachProcess(p)
            runLater {
                process = p
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun stop() = logInvocation {
        val p = process
        p?.destroyNiceThenForceThenWait()
    }

    override fun stopd() = daemon("stopd Thread") { stop() }

    private var msTimerTimeline: Timeline? = null
    private var secTimerTimeline: Timeline? = null
    private var minTimerTimeline: Timeline? = null
    private fun formatMillis(ms: Long): String {
        return when {
            ms == 0L -> ""
            ms < 1000L -> "$ms ms"
            else -> {
                val d = ms.milliseconds
                when {
                    d < ONE_MINUTE -> d.toString(DurationUnit.SECONDS, decimals = 2)
                    else           -> d.toString(DurationUnit.MINUTES, decimals = 2)
                }
            }


        }
    }

    private fun timerTimeline(
        textProp: StringProperty,
        interval: Duration
    ) = timeline(play = false) {
        keyframe(interval.toFXDuration()) {
            setOnFinished {
                textProp.value = this@ProcessConsolePane.runTime
            }
        }
        cycleCount = Timeline.INDEFINITE
    }

    private var theTimerText: TextWrapper? = null

    init {
        val vbox = this
        processProp.onChange {
            println("processProp:${it}")
            if (it != null) {
                console.reset()
            }
        }
        hbox<ButtonWrapper> {
            alignment = Pos.CENTER_LEFT
            spacing = 5.0
            actionbutton("Run") {
                this@ProcessConsolePane.rund()
            }
            button("Stop") {
                disableProperty.bind(this@ProcessConsolePane.runningProp.not())
                setOnAction {
                    daemon("Stop procpane Thread") {
                        this@ProcessConsolePane.stop()
                    }
                }
            }
            val showTimer = BindableProperty(true)
            button("Toggle Timer") {
                setOnAction {
                    showTimer.toggle()
                }
            }
            this@ProcessConsolePane.theTimerText = text {
                visibleWhen(showTimer)
                val textProp = node.textProperty()
                this@ProcessConsolePane.msTimerTimeline =
                    this@ProcessConsolePane.timerTimeline(textProp, 1.milliseconds)
                this@ProcessConsolePane.secTimerTimeline =
                    this@ProcessConsolePane.timerTimeline(textProp, 10.milliseconds)
                this@ProcessConsolePane.minTimerTimeline =
                    this@ProcessConsolePane.timerTimeline(textProp, 100.milliseconds)
            }

        }
        add(console.apply {
            prefHeightProperty.bind(vbox.heightProperty)
            vgrow = ALWAYS
        })
        val fracOfHeight = vbox.heightProperty * 0.2
        add(inspectBox.apply {
            minHeightProperty.bind(fracOfHeight)
            maxHeightProperty.bind(fracOfHeight)
            vgrow = ALWAYS
        })
        mcontextmenu {
            checkitem("Show process inspect pane", this@ProcessConsolePane.showProcessInspectPaneOption)
            checkitem("Auto-Restart", this@ProcessConsolePane.autoRestartProp)
        }
    }
}