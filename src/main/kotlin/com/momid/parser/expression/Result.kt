package com.momid.parser.expression

open class Result<T>

class Ok<T>(ok: T): Result<T>()

class Error<T>(error: String, range: IntRange): Result<T>()
