# Parser
A general-purpose parser capable of parsing various data using custom programmatic expressions and structures.

## How It Works
*Parser* allows you to define regex-like expressions in a flexible and programmatic manner. You can also define structures needed for parsing as simple classes along with their parameters as other structures.

You can easily register the structures you want and match against them. The result is provided as instances of your registered classes, which include the tokens' index range of the structures as well as their parameters.

## Usage
Heres how you would parse a simple while statement (the acutal implementation would be much more complex basicaly):

```kotlin
class While(var expression: Exp? = null, var codeBlock: CodeBlock? = null): Structure(
    spaced {
        !"while" + "(" + !While::expression + ")" + "{" + !While::codeBlock + "}"
    }
)

class Exp : Structure(
    insideParentheses(some(condition { it != '3' }))
)

class CodeBlock : Structure(
    some(condition { it != '{' && it != '}' })
)

val whileText = ("while (some() && true) {" +
        "weAreInsidewhile()" +
        "}").toList()



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
```
