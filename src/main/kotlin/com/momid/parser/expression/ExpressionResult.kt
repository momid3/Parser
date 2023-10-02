package com.momid.parser.expression

open class SomeExpressionResult(val expressionResult: ExpressionResult?)

open class ExpressionResult(val expression: Expression, val range: IntRange)

class MultiExpressionResult(val mainExpressionResult: ExpressionResult, private val expressionResults: List<ExpressionResult> = emptyList()): List<ExpressionResult> by expressionResults, ExpressionResult(mainExpressionResult.expression, mainExpressionResult.range)

class EachOfExpressionResult(val mainExpressionResult: ExpressionResult, val whichExpression: Expression): ExpressionResult(mainExpressionResult.expression, mainExpressionResult.range)

//fun ExpressionResult.getRange(): IntRange? {
//    when (this) {
//        is ExpressionResult -> return this.range
//        is MultiExpressionResult -> return this.mainExpressionResult.range
////        else -> throw(Throwable("ExpressionResult is whether NoExpressionResult or unknown"))
//        else -> return null
//    }
//}
//
//fun ExpressionResult.getExpression(): Expression {
//    when (this) {
//        is ExpressionResult -> return this.expression
//        is MultiExpressionResult -> return this.mainExpressionResult.expression
//        else -> throw(Throwable("ExpressionResult is whether NoExpressionResult or unknown"))
//    }
//}

fun noExpressionResult(): SomeExpressionResult {
    return SomeExpressionResult(null)
}

fun SomeExpressionResult.isNoExpressionResult(): Boolean {
    return this.expressionResult == null
}
