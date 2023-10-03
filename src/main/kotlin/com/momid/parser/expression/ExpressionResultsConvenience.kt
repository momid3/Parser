package com.momid.parser.expression

fun ExpressionResult.correspondingTokens(tokens: List<Char>): List<Char> {
    return tokens.slice(this.range.first until this.range.last)
}

fun ExpressionResult.correspondingTokensText(tokens: List<Char>): String {
    return tokens.slice(this.range.first until this.range.last).joinToString("")
}

fun handleExpressionResult(
    expressionFinder: ExpressionFinder,
    expressionResults: List<ExpressionResult>,
    tokens: List<Char>,
    handle: ExpressionResultsHandlerContext.() -> Unit
) {
    ExpressionResultsHandlerContext(expressionFinder, expressionResults, tokens, handle).handle()
}

class ExpressionResultsHandlerContext(
    val expressionFinder: ExpressionFinder,
    val expressionResults: List<ExpressionResult>,
    val tokens: List<Char>,
    val handle: ExpressionResultsHandlerContext.() -> Unit
) {
    fun continueWith(expressionResult: ExpressionResult, anotherHandler: ExpressionResultsHandlerContext.() -> Unit = handle) {
        ExpressionResultsHandlerContext(expressionFinder, expressionFinder.start(tokens, expressionResult.range), tokens, anotherHandler).anotherHandler()
    }
}
