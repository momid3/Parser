package com.momid.parser.expression

import com.momid.parser.expression.Expression
import com.momid.parser.expression.ExpressionResult
import com.momid.parser.expression.evaluateExpressionValueic

class ExpressionFinder {

    private val registeredExpressions = ArrayList<Expression>()

    fun registerExpressions(expressions: List<Expression>) {
        registeredExpressions.addAll(expressions)
    }

    fun start(tokens: List<Char>): List<ExpressionResult> {
        val foundExpressions = ArrayList<ExpressionResult>()
        var currentTokenIndex = 0
        while (true) { whi@
            for (expression in registeredExpressions) {
                if (currentTokenIndex >= tokens.size) {
                    break@whi
                }
                val expressionResult = evaluateExpressionValueic(expression, currentTokenIndex, tokens) ?: continue
                currentTokenIndex = expressionResult.range.last
                foundExpressions.add(expressionResult)
                continue@whi
            }
            break
        }
        return foundExpressions
    }
}
