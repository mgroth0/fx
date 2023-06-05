package matt.fx.node.perfdiagram

import matt.fx.web.WebViewPane
import matt.html.fig.sankey.sankeyHTML
import matt.log.profile.stopwatch.Stopwatch
import matt.model.code.idea.FigIdea

fun Stopwatch.analysisNode() = StopWatchAnalysisNode(this)

class StopWatchAnalysisNode(val sw: Stopwatch) : WebViewPane(sankeyHTML(sw.increments().joinToString("\n") {
    it.first
    "[ '${sw.prefix}', '${it.second}', ${it.first.inWholeMilliseconds} ],"
})), FigIdea