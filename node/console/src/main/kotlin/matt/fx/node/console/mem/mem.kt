package matt.fx.node.console.mem

import matt.async.safe.thread
import matt.async.safe.with
import matt.lang.model.file.FsFile
import matt.file.construct.mFile
import matt.file.toJioFile
import matt.lang.file.toJFile
import matt.lang.model.file.MacFileSystem

import java.util.concurrent.Semaphore

private const val MAX_MEM = 100

class ConsoleMemory(private val fil: FsFile) {
    private val file = fil.toJioFile()
    private var index: Int? = null
    private val mysem = Semaphore(1)

    //    private val myqueue = QueueThread(100.milliseconds, QueueThread.SleepType.WHEN_NO_JOBS)
    //    private var myjob: QueueThread.Job<*>? = null
    private fun resetIndex() {
        index = null
    }

    private fun remember(theInput: String?) {
        file.toJFile().appendText("\n${theInput}")
        file.writeText(file.readText().lines().filterNot { it.isBlank() }.joinToString("\n"))
        val memlines = file.readText().lines()
        if (memlines.size > MAX_MEM) {
            file.writeText(memlines.subList(1, memlines.size).joinToString("\n"))
        }
    }

    private fun mem() = file.readText().let {
        if (it.isBlank()) listOf() else it.lines().toList()
    }

    fun up() = mysem.with {
        //        if (myjob != null) {
        //            myjob!!.waitFor()
        //            myjob = null
        //        }
        val mymem = mem()
        if (mymem.isEmpty()) null
        else {
            index = if (index == null) mymem.size - 1 else index!! - 1
            if (index!! < 0) index = 0
            mymem[index!!]
        }
    }

    fun down() = mysem.with {
        //        if (myjob != null) {
        //            myjob!!.waitFor()
        //            myjob = null
        //        }
        val mymem = mem()
        if (index == null || mymem.isEmpty()) ""
        else {
            index = index!! + 1
            if (index!! > (mymem.size - 1)) {
                index = null
                ""
            } else mymem[index!!]
        }
    }

    fun handle_sent_input(theInput: String) {
        //        println("mem1.1")
        mysem.thread {
            //            println("mem1.2")
            remember(theInput)
            resetIndex()
            //            println("mem1.3")
        }
        //        println("mem1.4")
    }


    constructor(file: String) : this(mFile(file, MacFileSystem).toJioFile())

    init {
        file.parentFile!!.toJioFile().mkdirs()
        file.toJioFile().createNewFile()
    }
}