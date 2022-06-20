package matt.fx.node.perfdiagram

import matt.async.date.Stopwatch
import matt.fx.web.WebViewPane
import matt.klib.math.MILLION
import matt.klib.sankey.sankeyHTML
import kotlin.math.roundToInt


fun Stopwatch.analysisNode(): WebViewPane {

  val code = increments().joinToString("\n") {
	"[ '${prefix}', '${it.second}', ${(it.first/MILLION).roundToInt()} ],"
  }


  val html = sankeyHTML(code)



  return WebViewPane(html).apply {
	//	prefWidth = 2000.0
	//	prefHeight = 2000.0
  }
}


//@Language("HTML")
//fun sankeyHTML(code: String) = """
//  <html>
//    <head>
//      <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
//      <script type="text/javascript">
//      ${sankeyJS(code)}
//      </script>
//    </head>
//    <body>
//      <div id="sankey_basic" style="width: 900px; height: 300px;"></div>
//    </body>
//  </html>
//""".trimIndent()
//
//@Language("JavaScript")
//fun sankeyJS(code: String) = """
//     google.charts.matt.gui.ser.load('current', {'packages':['sankey']});
//        google.charts.setOnLoadCallback(drawChart);
//
//        function drawChart() {
//          var data = new google.visualization.DataTable();
//          data.addColumn('string', 'From');
//          data.addColumn('string', 'To');
//          data.addColumn('number', 'Weight');
//          data.addRows([
////            [ 'A', 'X', 5.3 ],
////            [ 'A', 'Y', 7 ],
////            [ 'A', 'Z', 6 ],
////            [ 'B', 'X', 2 ],
////            [ 'B', 'Y', 9 ],
////            [ 'B', 'Z', 4 ],
//            ${code}
//          ]);
//
//          // Sets chart options.
//          var options = {
//            width: 600,
//          };
//
//          // Instantiates and draws our chart, passing in some options.
//          var chart = new google.visualization.Sankey(document.getElementById('sankey_basic'));
//          chart.draw(data, options);
//        }
//""".trimIndent()