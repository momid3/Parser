package com.momid.parser.expression

val spaces = some0(condition { it.isWhitespace() })

val insideParentheses = CustomExpression() { tokens, startIndex ->
    var numberOfLefts = 1
    for (tokenIndex in startIndex..tokens.lastIndex) {
        if (tokens[tokenIndex] == '(') {
            numberOfLefts += 1
        }
        if (tokens[tokenIndex] == ')') {
            numberOfLefts -= 1
        }
        if (numberOfLefts == 0) {
            return@CustomExpression tokenIndex
        }
    }
    return@CustomExpression -1
}

fun insideParentheses(expression: Expression): Expression {
    return combineExpressions(insideParentheses, expression)
}

fun matchesFully(expression: Expression, tokenSlice: List<Char>): Boolean {
    return evaluateExpression(expression, 0, tokenSlice) == tokenSlice.size
}

fun combineExpressions(expression: Expression, otherExpression: Expression): Expression {
    return CustomExpression() { tokens, startIndex ->
        val expressionNextIndex = evaluateExpression(expression, startIndex, tokens)
        if (expressionNextIndex == -1) {
            return@CustomExpression -1
        }
        if (matchesFully(otherExpression, tokens.slice(startIndex until expressionNextIndex))) {
            return@CustomExpression expressionNextIndex
        } else {
            return@CustomExpression -1
        }
    }
}