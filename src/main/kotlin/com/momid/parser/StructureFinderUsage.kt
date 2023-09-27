package com.momid.parser

import com.momid.parser.expression.condition
import com.momid.parser.expression.insideParentheses
import com.momid.parser.expression.some
import com.momid.parser.structure.*

class While(var expression: Exp? = null, var codeBlock: CodeBlock? = null): Structure(
    spaced {
        !"while" + "(" + !While::expression + ")" + "{" + !While::codeBlock + "}"
    }
)

class Exp(): Structure(
    insideParentheses(some(condition { it != '3' }))
)

class CodeBlock(): Structure(
    some(condition { it != '{' && it != '}' })
)

val whileText = ("while (some()true) { whi@\n" +
        "            for (registeredClass in registeredClasses) \n" +
        "                val structure = evaluateStructure(registeredClass, currentTokenIndex, tokens)\n" +
        "                val structureRange = structure.range\n" +
        "                if (structureRange != null) \n" +
        "                    if (currentTokenIndex > tokens.lastIndex) \n" +
        "                        continue\n" +
        "                     else \n" +
        "                        val nextTokenIndex = structureRange.last\n" +
        "                        foundStructures.add(structure)\n" +
        "                        currentTokenIndex = nextTokenIndex\n" +
        "                        continue@whi\n" +
        "                    \n" +
        "                \n" +
        "            \n" +
        "\n" +
        "            break\n" +
        "        }").toList()

fun main() {

    val finder = StructureFinder()
    finder.registerStructures(While::class)

    val structures = finder.start(whileText)

    structures.forEach {
        if (it is While) {
            println(it.expression!!.correspondingTokens(whileText).joinToString(""))
        }
    }
}