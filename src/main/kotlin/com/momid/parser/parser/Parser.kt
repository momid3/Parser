package com.momid.parser.parser

import com.momid.parser.expression.*
import com.momid.parser.isOf
import com.momid.parser.not

val variableName =
    condition { it.isLetter() } + some0(condition { it.isLetterOrDigit() })

val number =
    condition { it.isDigit() } + some0(condition { it.isDigit() })

val atomicExp =
    anyOf(variableName, number)

val operator =
    anyOf('+', '-', '*', '/')

val simpleExpression =
    some(anyOf(spaces + atomicExp + spaces, spaces + operator + spaces))

val expressionInParentheses =
    insideParentheses

val simpleExpressionInParentheses =
    !"(" + expressionInParentheses["insideParentheses"] + ")"

val complexExpression =
    some(anyOf(simpleExpression, simpleExpressionInParentheses))

fun ExpressionResult.correspondingTokens(tokens: List<Char>): List<Char> {
    return tokens.slice(this.range.first until this.range.last)
}

fun ExpressionResult.correspondingTokensText(tokens: List<Char>): String {
    return tokens.slice(this.range.first until this.range.last).joinToString("")
}

private fun handleExpressionResults(expressionFinder: ExpressionFinder, expressionResults: List<ExpressionResult>, tokens: List<Char>) {
    expressionResults.forEach {
        it.isOf(complexExpression) {
            println("complex expression: " + it.correspondingTokensText(tokens))
            (it as MultiExpressionResult).forEach {
                it.isOf(simpleExpression) {
                    println("simple: " + it.correspondingTokensText(tokens))
                }
                it.isOf(simpleExpressionInParentheses) {
//                    println("kind of simpleExpressionInParentheses " + simpleExpressionInParentheses::class.simpleName)
//                    println(it::class)
                    println("simple in parentheses: " + it.correspondingTokensText(tokens))
                    handleExpressionResults(expressionFinder, expressionFinder.start(it["insideParentheses"].correspondingTokens(tokens)), it["insideParentheses"].correspondingTokens(tokens))
                }
            }
        }
    }
}

fun main() {

    val text = "someVar + 3 + 7 + (3 + 37 + (373 + 373))".toList()
    val finder = ExpressionFinder()
    finder.registerExpressions(listOf(complexExpression))
    val expressionResults = finder.start(text)
    handleExpressionResults(finder, expressionResults, text)
    expressionResults.forEach {
//        println(it.correspondingTokens(text).joinToString(""))
//        printExpressionResult(it)
    }
}
