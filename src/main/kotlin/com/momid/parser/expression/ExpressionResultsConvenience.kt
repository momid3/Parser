package com.momid.parser.expression

fun ExpressionResult.correspondingTokens(tokens: List<Char>): List<Char> {
    return tokens.slice(this.range.first until this.range.last)
}

fun ExpressionResult.correspondingTokensText(tokens: List<Char>): String {
    return tokens.slice(this.range.first until this.range.last).joinToString("")
}

fun handleExpressionResult(
    expressionFinder: ExpressionFinder,
    expressionResult: ExpressionResult,
    tokens: List<Char>,
    handle: ExpressionResultsHandlerContext.() -> Unit
) {
    ExpressionResultsHandlerContext(expressionFinder, expressionResult, tokens, handle).handle()
}

class ExpressionResultsHandlerContext(
    val expressionFinder: ExpressionFinder,
    val expressionResult: ExpressionResult,
    val tokens: List<Char>,
    val handle: ExpressionResultsHandlerContext.() -> Unit
) {
    fun continueWith(expressionResult: ExpressionResult, anotherHandler: ExpressionResultsHandlerContext.() -> Unit = handle) {
        expressionFinder.start(tokens, expressionResult.range).forEach {
            ExpressionResultsHandlerContext(expressionFinder, it, tokens, anotherHandler).anotherHandler()
        }
    }

    fun continueWith(expressionResult: ExpressionResult, vararg registerExpressions: Expression, anotherHandler: ExpressionResultsHandlerContext.() -> Unit = handle) {
        ExpressionFinder().apply { registerExpressions(registerExpressions.toList()) }.start(tokens, expressionResult.range).forEach {
            ExpressionResultsHandlerContext(expressionFinder, it, tokens, anotherHandler).anotherHandler()
        }
    }

    fun print(expressionResult: ExpressionResult) {
        println(expressionResult.correspondingTokensText(tokens))
    }

    fun print(prefix: String, expressionResult: ExpressionResult) {
        println(prefix + " " + expressionResult.correspondingTokensText(tokens))
    }
}
