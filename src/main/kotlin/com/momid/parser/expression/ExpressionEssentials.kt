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

fun inline(multiExpression: MultiExpression): CustomExpressionValueic {
    return CustomExpressionValueic() { tokens, startIndex ->
        val inlinedExpressionResults = ArrayList<ExpressionResult>()
        val expressionResult = evaluateExpressionValueic(multiExpression, startIndex, tokens) ?: return@CustomExpressionValueic null
        expressionResult.forEach {
            if (it is MultiExpressionResult) {
                inlinedExpressionResults.addAll(it)
            } else {
                inlinedExpressionResults.add(it)
            }
        }
        expressionResult.expressionResults.clear()
        expressionResult.expressionResults.addAll(inlinedExpressionResults)
        return@CustomExpressionValueic ContentExpressionResult(ExpressionResult(multiExpression, expressionResult.range), expressionResult)
    }
}

fun spaced(expressions: () -> MultiExpression): MultiExpression {
    val spacedExpressions = MultiExpression(arrayListOf())
    val expressions = expressions()
    for (expressionIndex in 0..expressions.lastIndex - 1) {
        spacedExpressions.expressions.add(expressions[expressionIndex])
        spacedExpressions.expressions.add(spaces)
    }
    spacedExpressions.expressions.add(expressions[expressions.lastIndex])
    return spacedExpressions
}

fun insideOfParentheses(parenthesesStart: Char, parenthesesEnd: Char): CustomExpression {
    return CustomExpression() { tokens, startIndex ->
        var numberOfLefts = 1
        for (tokenIndex in startIndex..tokens.lastIndex) {
            if (tokens[tokenIndex] == parenthesesStart) {
                numberOfLefts += 1
            }
            if (tokens[tokenIndex] == parenthesesEnd) {
                numberOfLefts -= 1
            }
            if (numberOfLefts == 0) {
                return@CustomExpression tokenIndex
            }
        }
        return@CustomExpression -1
    }
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
