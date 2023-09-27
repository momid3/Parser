package com.momid.parser.expression

open class SomeExpressionResult(val hasExpressionResult: Boolean)

class SimpleExpressionResult(val expression: Expression, val range: IntRange): SomeExpressionResult(true)

class ExpressionResult(private val expressionResults: List<SomeExpressionResult> = emptyList(), val mainExpressionResult: SimpleExpressionResult): List<SomeExpressionResult> by expressionResults, SomeExpressionResult(true)

class NoExpressionResult(): SomeExpressionResult(false)

fun SomeExpressionResult.getRange(): IntRange? {
    when (this) {
        is SimpleExpressionResult -> return this.range
        is ExpressionResult -> return this.mainExpressionResult.range
//        else -> throw(Throwable("ExpressionResult is whether NoExpressionResult or unknown"))
        else -> return null
    }
}

fun SomeExpressionResult.getExpression(): Expression {
    when (this) {
        is SimpleExpressionResult -> return this.expression
        is ExpressionResult -> return this.mainExpressionResult.expression
        else -> throw(Throwable("ExpressionResult is whether NoExpressionResult or unknown"))
    }
}