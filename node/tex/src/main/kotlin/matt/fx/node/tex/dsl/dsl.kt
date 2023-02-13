package matt.fx.node.tex.dsl

import matt.lang.function.DSL


fun tex(op: DSL<TeXDSL>) = TeXDSL().apply(op)

@DslMarker
annotation class SafeTexDSL


@SafeTexDSL
class TeXDSL {
  private var s = ""

  fun frac(num: DSL<TeXDSL>, denom: DSL<TeXDSL>) {
	s += "\\frac{${TeXDSL().apply(num).code()}}{${TeXDSL().apply(denom).code()}}"
  }

  fun text(text: String) {
	s += "\\textrm{$text}"
  }

  operator fun times(n: Int): TeXDSL {
	s += " * $n"
	return this
  }

  fun code() = s
}