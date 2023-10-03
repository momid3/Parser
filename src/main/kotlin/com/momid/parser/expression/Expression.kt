package com.momid.parser.expression

import com.momid.parser.structure.Template

public open class Expression(var name: String? = null, var isValueic: Boolean = true): Template() {

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

class NotExpression(val expression: Expression): Expression()

class CustomExpression(val condition: (tokens: List<Char>, startIndex: Int) -> Int): Expression()

interface Condition {

    public fun invoke(token: Char): Boolean
}

interface EvaluateExpression {

    fun evaluate(startIndex: Int, tokens: List<Char>): Int
}

fun evaluateExpression(expression: Expression, startIndex: Int, tokens: List<Char>): Int {
    if (startIndex >= tokens.size) {
        if (expression is RecurringSome0Expression) {
            return startIndex
        } else {
            return -1
        }
    }
    when (expression) {
        is ExactExpression -> return evaluateExpression(expression, startIndex, tokens)
        is ConditionExpression -> return evaluateExpression(expression, startIndex, tokens)
        is MultiExpression -> return evaluateExpression(expression, startIndex, tokens)
        is RecurringExpression -> return evaluateExpression(expression, startIndex, tokens)
        is RecurringSomeExpression -> return evaluateExpression(expression, startIndex, tokens)
        is RecurringSome0Expression -> return evaluateExpression(expression, startIndex, tokens)
        is EachOfExpression -> return evaluateExpression(expression, startIndex, tokens)
        is EachOfTokensExpression -> return evaluateExpression(expression, startIndex, tokens)
        is NotExpression -> return evaluateExpression(expression, startIndex, tokens)
        is CustomExpression -> return evaluateExpression(expression, startIndex, tokens)
        else -> throw(Throwable("unknown expression kind"))
    }
}

fun evaluateExpressionValueic(expression: Expression, startIndex: Int, tokens: List<Char>): ExpressionResult? {
    if (startIndex >= tokens.size) {
        if (expression is RecurringSome0Expression) {
            return ExpressionResult(expression, startIndex..startIndex)
        } else {
            return null
        }
    }
    when (expression) {
        is MultiExpression -> return evaluateExpressionValueic(expression, startIndex, tokens)
        is EachOfExpression -> return evaluateExpressionValueic(expression, startIndex, tokens)
        is RecurringSomeExpression -> return evaluateExpressionValueic(expression, startIndex, tokens)
        is RecurringSome0Expression -> return evaluateExpressionValueic(expression, startIndex, tokens)
        else -> {
            val endIndex = evaluateExpression(expression, startIndex, tokens)
            if (endIndex != -1) {
                return ExpressionResult(expression, startIndex .. endIndex)
            } else {
                return null
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
            if (endIndex > tokens.size) {
                break
            }
        }
    }
    return -1
}

fun evaluateExpressionValueic(multiExpression: MultiExpression, startIndex: Int, tokens: List<Char>): MultiExpressionResult? {
    val expressionResults = ArrayList<ExpressionResult>()
    var multiExpressionIndex = 0
    var endIndex = startIndex
    while (true) {
        val evaluationResult = evaluateExpressionValueic(multiExpression[multiExpressionIndex], endIndex, tokens)
        if (evaluationResult == null) {
            return null
        } else {

            val nextIndex = evaluationResult.range.last
            val expression = multiExpression[multiExpressionIndex]
            if (expression.isValueic) {
                expressionResults.add(evaluationResult)
            }

            endIndex = nextIndex
            multiExpressionIndex += 1
            if (multiExpressionIndex == multiExpression.size) {
                if (expressionResults.isEmpty()) {
                    throw(Throwable("multiExpression subs should not be empty"))
//                    return ExpressionResult(multiExpression, startIndex .. endIndex)
                } else {
                    return MultiExpressionResult(ExpressionResult(multiExpression, startIndex .. endIndex), expressionResults)
                }
            }
            if (endIndex > tokens.size) {
                break
            }
        }
    }
    return null
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

fun evaluateExpressionValueic(eachOfExpression: EachOfExpression, startIndex: Int, tokens: List<Char>): ExpressionResult? {
    eachOfExpression.forEach {
        val expressionResult = evaluateExpressionValueic(it, startIndex, tokens)
        if (expressionResult != null) {
            val endIndex = expressionResult.range.last
            if (endIndex != -1) {
                return expressionResult
            }
        }
    }
    return null
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
            if (endIndex <= tokens.size) {
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

fun evaluateExpressionValueic(recurringSomeExpression: RecurringSomeExpression, startIndex: Int, tokens: List<Char>): ExpressionResult? {
    val expressionResults = ArrayList<ExpressionResult>()
    var numberOfRecurring = 0
    var endIndex = startIndex
    while (true) {
        val expressionResult = evaluateExpressionValueic(recurringSomeExpression.expression, endIndex, tokens) ?: break
        endIndex = expressionResult.range.last
        if (endIndex <= tokens.size) {
            numberOfRecurring += 1
            expressionResults.add(expressionResult)
        } else {
            break
        }
    }
    if (numberOfRecurring > 0) {
        return MultiExpressionResult(ExpressionResult(recurringSomeExpression, startIndex..endIndex), expressionResults)
    } else {
        return null
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
            if (endIndex <= tokens.size) {
                numberOfRecurring += 1
            } else {
                break
            }
        }
    }
    return endIndex
}

fun evaluateExpressionValueic(recurringSome0Expression: RecurringSome0Expression, startIndex: Int, tokens: List<Char>): ExpressionResult {
    val expressionResults = ArrayList<ExpressionResult>()
    var numberOfRecurring = 0
    var endIndex = startIndex
    while (true) {
        val expressionResult = evaluateExpressionValueic(recurringSome0Expression.expression, endIndex, tokens) ?: break
        endIndex = expressionResult.range.last
        if (endIndex <= tokens.size) {
            numberOfRecurring += 1
            expressionResults.add(expressionResult)
        } else {
            break
        }
    }
    return MultiExpressionResult(ExpressionResult(recurringSome0Expression, startIndex..endIndex), expressionResults)
}

fun evaluateExpression(notExpression: NotExpression, startIndex: Int, tokens: List<Char>): Int {
    val endIndex = evaluateExpression(notExpression.expression, startIndex, tokens)
    if (endIndex == -1) {
        return startIndex
    } else {
        return -1
    }
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
