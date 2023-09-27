package com.momid.parser.expression

import com.momid.parser.structure.Template

public open class Expression(var name: String? = null, var isValueic: Boolean = false): Template() {

}

class ExactExpression(val value: String) : Expression() {

}

class ConditionExpression(val condition: (Char) -> Boolean): Expression()

open class MultiExpression(val expressions: ArrayList<Expression>): Expression(), List<Expression> by expressions

class RecurringExpression(val expression: Expression, val numberOfRecurring: Int): Expression()

class RecurringSomeExpression(val expression: Expression): Expression()

class RecurringSome0Expression(val expression: Expression): Expression()

class EachOfExpression(private val expressions: List<Expression>): Expression(), List<Expression> by expressions

class EachOfTokensExpression(private val tokens: List<Char>): Expression(), List<Char> by tokens

class CustomExpression(val condition: (tokens: List<Char>, startIndex: Int) -> Int): Expression()

interface Condition {

    public fun invoke(token: Char): Boolean
}

interface EvaluateExpression {

    fun evaluate(startIndex: Int, tokens: List<Char>): Int
}

fun evaluateExpression(expression: Expression, startIndex: Int, tokens: List<Char>): Int {
    when (expression) {
        is ExactExpression -> return evaluateExpression(expression, startIndex, tokens)
        is ConditionExpression -> return evaluateExpression(expression, startIndex, tokens)
        is MultiExpression -> return evaluateExpression(expression, startIndex, tokens)
        is RecurringExpression -> return evaluateExpression(expression, startIndex, tokens)
        is RecurringSomeExpression -> return evaluateExpression(expression, startIndex, tokens)
        is RecurringSome0Expression -> return evaluateExpression(expression, startIndex, tokens)
        is EachOfExpression -> return evaluateExpression(expression, startIndex, tokens)
        is EachOfTokensExpression -> return evaluateExpression(expression, startIndex, tokens)
        is CustomExpression -> return evaluateExpression(expression, startIndex, tokens)
        else -> throw(Throwable("unknown expression kind"))
    }
}

fun evaluateExpressionValueic(expression: Expression, startIndex: Int, tokens: List<Char>): SomeExpressionResult {
    when (expression) {
        is MultiExpression -> return evaluateExpressionValueic(expression, startIndex, tokens)
        else -> {
            val endIndex = evaluateExpression(expression, startIndex, tokens)
            if (endIndex != -1) {
                return SimpleExpressionResult(expression, startIndex .. endIndex)
            } else {
                return NoExpressionResult()
            }
        }
    }
}

fun evaluateExpression(exactExpression: ExactExpression, startIndex: Int, tokens: List<Char>): Int {
    var exactExpressionIndex = 0
    var endIndex = startIndex
    for (index in startIndex until tokens.size) {
        endIndex += 1
        if (tokens[index] == exactExpression.value[exactExpressionIndex]) {
            exactExpressionIndex += 1
            if (exactExpressionIndex == exactExpression.value.length) {
                return endIndex
            }
        } else {
            return -1
        }
    }
    return -1
}

fun evaluateExpression(conditionExpression: ConditionExpression, startIndex: Int, tokens: List<Char>): Int {
    if (conditionExpression.condition(tokens[startIndex])) {
        return startIndex + 1
    } else return -1
}

fun evaluateExpression(multiExpression: MultiExpression, startIndex: Int, tokens: List<Char>): Int {
    var multiExpressionIndex = 0
    var endIndex = startIndex
    while (true) {
        val nextIndex = evaluateExpression(multiExpression[multiExpressionIndex], endIndex, tokens)
        if (nextIndex == -1) {
            return -1
        } else {
            endIndex = nextIndex
            multiExpressionIndex += 1
            if (multiExpressionIndex == multiExpression.size) {
                return endIndex
            }
            if (endIndex >= tokens.size) {
                break
            }
        }
    }
    return -1
}

fun evaluateExpressionValueic(multiExpression: MultiExpression, startIndex: Int, tokens: List<Char>): SomeExpressionResult {
    val expressionResults = ArrayList<SomeExpressionResult>()
    var multiExpressionIndex = 0
    var endIndex = startIndex
    while (true) {
        val evaluationResult = evaluateExpressionValueic(multiExpression[multiExpressionIndex], endIndex, tokens)
        if (evaluationResult is NoExpressionResult) {
            return NoExpressionResult()
        } else {

            val nextIndex = when (evaluationResult) {
                is SimpleExpressionResult -> evaluationResult.range.last
                is ExpressionResult -> evaluationResult.mainExpressionResult.range.last
                else -> throw(Throwable("unknown expression result"))
            }
            val expression = multiExpression[multiExpressionIndex]
            if (expression.isValueic) {
                expressionResults.add(evaluationResult)
            }

            endIndex = nextIndex
            multiExpressionIndex += 1
            if (multiExpressionIndex == multiExpression.size) {
                if (expressionResults.isEmpty()) {
                    return SimpleExpressionResult(multiExpression, startIndex .. endIndex)
                } else {
                    return ExpressionResult(expressionResults, SimpleExpressionResult(multiExpression, startIndex .. endIndex))
                }
            }
            if (endIndex >= tokens.size) {
                break
            }
        }
    }
    return NoExpressionResult()
}

fun evaluateExpression(recurringExpression: RecurringExpression, startIndex: Int, tokens: List<Char>): Int {
    val recurringList = MutableList(recurringExpression.numberOfRecurring) {
        recurringExpression.expression
    }
    return evaluateExpression(MultiExpression(recurringList as ArrayList<Expression>), startIndex, tokens)
}

fun evaluateExpression(eachOfExpression: EachOfExpression, startIndex: Int, tokens: List<Char>): Int {
    eachOfExpression.forEach {
        val endIndex = evaluateExpression(it, startIndex, tokens)
        if (endIndex != -1) {
            return endIndex
        }
    }
    return -1
}

fun evaluateExpression(eachOfTokensExpression: EachOfTokensExpression, startIndex: Int, tokens: List<Char>): Int {
    eachOfTokensExpression.forEach {
        if (tokens[startIndex] == it) {
            return startIndex + 1
        }
    }
    return -1
}

fun evaluateExpression(recurringSomeExpression: RecurringSomeExpression, startIndex: Int, tokens: List<Char>): Int {
    var numberOfRecurring = 0
    var endIndex = startIndex
    while (true) {
        val nextIndex = evaluateExpression(recurringSomeExpression.expression, endIndex, tokens)
        if (nextIndex == -1) {
            break
        } else {
            endIndex = nextIndex
            if (endIndex < tokens.size) {
                numberOfRecurring += 1
            } else {
                break
            }
        }
    }
    if (numberOfRecurring > 0) {
        return endIndex
    } else {
        return -1
    }
}

fun evaluateExpression(recurringSome0Expression: RecurringSome0Expression, startIndex: Int, tokens: List<Char>): Int {
    var numberOfRecurring = 0
    var endIndex = startIndex
    while (true) {
        val nextIndex = evaluateExpression(recurringSome0Expression.expression, endIndex, tokens)
        if (nextIndex == -1) {
            break
        } else {
            endIndex = nextIndex
            if (endIndex < tokens.size) {
                numberOfRecurring += 1
            } else {
                break
            }
        }
    }
    return endIndex
}

fun evaluateExpression(customExpression: CustomExpression, startIndex: Int, tokens: List<Char>): Int {
    val endIndex = customExpression.condition(tokens, startIndex)
    return endIndex
}


fun main() {
    val text = "hello ! what a beautiful day. how are you ?"
    var endIndex = evaluateExpression(ExactExpression("hello"), 0, text.toList())
    endIndex = evaluateExpression(ConditionExpression { it != 'h' }, 0, text.toList())
    endIndex = evaluateExpression(MultiExpression(arrayListOf(ExactExpression("hello"), ConditionExpression { it != 'a' }, ExactExpression("!"))), 0, text.toList())
    println("end index is: " + endIndex)
}
