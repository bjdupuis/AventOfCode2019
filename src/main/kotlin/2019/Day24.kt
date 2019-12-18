package `2019`

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import java.lang.Math.pow
import kotlin.math.exp
import kotlin.math.pow

fun main(args: Array<String>) {
    val input = """
##...
#.###
.#.#.
#....
..###
    """.trimIndent()

    println("Biodiversity state is "+Day24(input).calculateBiodiversity())
}

class Day24(input: String) {
    private val currentLife: MutableMap<Point, Char> = input.lines().mapIndexed { index, string ->
        mutableMapOf<Point, Char>().apply {
            string.mapIndexed { charIndex, c ->
                this[Point(charIndex, index)] = c
            }
        }
    }.reduce { acc, map ->
        map.entries.forEach {
            acc[it.key] = it.value
        }
        acc
    }

    private val terminal: Terminal = DefaultTerminalFactory().createTerminal()
    private val textGraphics = terminal.newTextGraphics()

    open class Point(val x: Int, val y: Int) {
        fun adjacents(): Set<Point> {
            return mutableSetOf<Point>().apply {
                if (y > 0) add(Point(x, y - 1))
                if (y < 4) add(Point(x, y + 1))
                if (x < 4) add(Point(x + 1, y))
                if (x > 0) add(Point(x - 1, y))
            }
        }

        override fun equals(other: Any?): Boolean {
            return if (other is Point) {
                x == other.x && y == other.y
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            return x * 31000 + y
        }
    }

    fun calculateBiodiversity(): Long {
        terminal.enterPrivateMode()
        terminal.addResizeListener { terminal, _ -> terminal.clearScreen() }
        val textGraphics = terminal.newTextGraphics()
        terminal.setCursorVisible(false)
        terminal.setBackgroundColor(TextColor.ANSI.BLACK)
        terminal.setForegroundColor(TextColor.ANSI.WHITE)

        var done = false
        var newLife = currentLife
        updateTerminal(newLife)
        val biodiversities = mutableSetOf<Long>()
        while (!done) {
            Thread.sleep(10)
            newLife = calculateNextTimeStep(newLife)
            updateTerminal(newLife)
            val biodiversity = calculateBiodiversityFor(newLife)
            textGraphics.putString(0, 0, "Biodiversity is $biodiversity")
            terminal.flush()
            if (biodiversities.contains(biodiversity)) {
                return biodiversity
            } else {
                biodiversities.add(biodiversity)
            }
        }

        return 0
    }

    private fun calculateBiodiversityFor(newLife: MutableMap<Point, Char>): kotlin.Long {
        var power = 1L
        var biodiversity = 0L
        for (y in 0..4) {
            for (x in 0..4) {
                if (newLife[Point(x, y)] == '#') {
                    biodiversity += power
                }
                power *= 2
            }
        }

        return biodiversity
    }

    private fun calculateNextTimeStep(current: Map<Point,Char>): MutableMap<Point,Char> {
        val newLife = mutableMapOf<Point, Char>()

        for (x in 0..4) {
            for (y in 0..4) {
                val location = Point(x,y)
                when (current[location]) {
                    '.' -> {
                        val adjacentBugs = current.filterKeys { it in location.adjacents() }
                            .entries.filter { it.value == '#' }
                            .size
                        newLife[location] = if (adjacentBugs == 1 || adjacentBugs == 2) '#' else '.'
                    }
                    '#' -> {
                        val adjacentBugs = current.filterKeys { it in location.adjacents() }
                            .entries.filter { it.value == '#' }
                            .size
                        newLife[location] = if (adjacentBugs == 1) '#' else '.'
                    }
                }
            }
        }
        return newLife
    }

    private fun updateTerminal(newLife: MutableMap<Point, Char>) {
        terminal.clearScreen()

        newLife.entries.forEach {
            terminal.setCursorPosition(it.key.x, it.key.y + 2)
            terminal.putCharacter(it.value)
        }

        terminal.flush()
    }

}