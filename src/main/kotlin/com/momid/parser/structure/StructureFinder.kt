package com.momid.parser.structure

import kotlin.reflect.KClass

class StructureFinder {

    private val registeredClasses : ArrayList<KClass<*>> = ArrayList()

    fun registerStructures(vararg structures: KClass<*>) {
        registeredClasses.addAll(structures)
    }

    fun start(tokens: List<Char>): List<Structure> {
        var currentTokenIndex = 0
        val foundStructures = ArrayList<Structure>()
        while (true) { whi@
            for (registeredClass in registeredClasses) {
                val structure = evaluateStructure(registeredClass as KClass<Structure>, currentTokenIndex, tokens)
                val structureRange = structure.range
                if (structureRange != null) {
                    if (currentTokenIndex > tokens.lastIndex) {
                        continue
                    } else {
                        val nextTokenIndex = structureRange.last
                        foundStructures.add(structure)
                        currentTokenIndex = nextTokenIndex
                        continue@whi
                    }
                }
            }

            break
        }

        return foundStructures
    }
}

fun Structure.correspondingTokens(tokens: List<Char>): List<Char> {
    val range = this.range!!
    return tokens.slice(range.first until range.last)
}
