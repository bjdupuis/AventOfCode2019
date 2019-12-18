package `2019`

import kotlin.math.abs
import kotlin.text.StringBuilder

fun main(args: Array<String>) {
    val input = "03036732577212944063491565474664"
    var signal = StringBuilder().apply {
        for (i in 1..10000) {
            append(input)
        }
    }.toString()

    for (i in 1..100) {
        signal = Day16().calculatePart2(signal)
        println("$i: Output is $signal")
    }
    println("Output is $signal")
}

class Day16 {
    private val basePattern = "0, 1, 0, -1".split(", ").map(String::toInt)

    // eh, maybe revisit to make work sometime
    fun calculatePart2(input: String): String {
        val answerIndex = input.substring(0..6).toInt()

        var current = 0
        for (i in input.length - 1 downTo answerIndex + 8) {
            current = (input[i] - '0' + current) % 10
        }

        val sb = StringBuilder()
        for (i in answerIndex + 7 downTo answerIndex) {
            current = (input[i] - '0' + current) % 10
            sb.append('0' + current)
        }
        return sb.toString().reversed()
    }

    fun calculateFFT(input: String): String {
        val sb = StringBuilder()

        for (characterIndex in input.indices) {
            val table = getMultiplierTable(characterIndex, input.length)
            var result = 0
            for (i in input.indices) {
                result += (input[i] - '0') * table[i]
            }
            sb.append(abs(result % 10))
        }

        return sb.toString()
    }

    private fun getMultiplierTable(outputIndex: Int, size: Int): List<Int> {
        var pattern = mutableListOf<Int>()
        do {
            for (value in basePattern) {
                pattern.addAll(MutableList(outputIndex+1) {value})
            }
        } while (pattern.size <= (size+1))

        return pattern.drop(1)
    }
}