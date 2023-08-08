package matt.fx.node.console.text.parseterm

fun parseTerminalOutput(text: String): List<TransformableOutput> {
    val packageStart = text.indexOf("at matt.")
    if (packageStart >= 0) {

        val packageEnd = text.indexOf("(")
        if (packageEnd > packageStart) {

            val nameStart = packageStart + 3
            @Suppress("UNUSED_VARIABLE")
            val qualifiedName = text.substring(nameStart, packageEnd - 1)
            val filePartStart = packageEnd + 1
            val filePart = text.substring(filePartStart)
            val colon = filePart.indexOf(":")
            if (colon >= 0) {
                val file = filePart.substring(0, colon)
                val endParentheses = filePart.indexOf(")")
                val fileLine = filePart.substring(colon + 1, endParentheses).toIntOrNull()
                if (fileLine != null) {
                    if (file.endsWith(".kt")) {
                        val theIndices = packageStart..(filePartStart + endParentheses)
                        return listOf(
                            StackTraceLine(
                                indices = theIndices,
                                text = text.substring(theIndices),
                                fileName = file,
                                qualifiedName=qualifiedName,
                                lineNumber = fileLine
                            )
                        )
                    }
                }
            }
        }

    }
    return listOf()

}


sealed interface TransformableOutput {
    val indices: IntRange
}

class StackTraceLine(
    override val indices: IntRange,
    val text: String,
    val fileName: String,
    val qualifiedName: String,
    val lineNumber: Int
) : TransformableOutput


