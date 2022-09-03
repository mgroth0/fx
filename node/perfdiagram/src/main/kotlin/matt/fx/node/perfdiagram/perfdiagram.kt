@file:OptIn(ExperimentalTime::class)

package matt.fx.node.perfdiagram

import matt.fx.web.WebViewPane
import matt.html.sankey.sankeyHTML
import matt.log.profile.Stopwatch
import kotlin.math.roundToInt
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.ExperimentalTime

fun Stopwatch.analysisNode() = StopWatchAnalysisNode(this)

class StopWatchAnalysisNode(val sw: Stopwatch): WebViewPane(sankeyHTML(sw.increments().joinToString("\n") {
  it.first
  "[ '${sw.prefix}', '${it.second}', ${it.first.inWholeMilliseconds} ],"
}))