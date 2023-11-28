package matt.fx.node.perfdiagram

import matt.fig.modell.sankey.SankeyConnection
import matt.fig.modell.sankey.SankeyIr
import matt.log.profile.stopwatch.Stopwatch

fun Stopwatch.analysisNodeIr() = SankeyIr(
    increments().map {
        SankeyConnection(from = prefix ?: "null", to = it.second, weight = it.first.inWholeMilliseconds)
    }
)