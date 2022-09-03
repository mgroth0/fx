package matt.fx.node.perfdiagram

import matt.async.date.Stopwatch
import matt.fx.web.WebViewPane
import matt.math.MILLION
import matt.html.sankey.sankeyHTML
import kotlin.math.roundToInt

fun Stopwatch.analysisNode() = StopWatchAnalysisNode(this)

class StopWatchAnalysisNode(val sw: Stopwatch): WebViewPane(sankeyHTML(sw.increments().joinToString("\n") {
  "[ '${sw.prefix}', '${it.second}', ${(it.first/MILLION).roundToInt()} ],"
}))