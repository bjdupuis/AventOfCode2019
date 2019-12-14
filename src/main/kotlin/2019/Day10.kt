package `2019`

import kotlin.math.atan2
import kotlin.math.sign
import kotlin.math.sqrt

fun main(args: Array<String>) {
    val input = """
.##.#.#....#.#.#..##..#.#.
#.##.#..#.####.##....##.#.
###.##.##.#.#...#..###....
####.##..###.#.#...####..#
..#####..#.#.#..#######..#
.###..##..###.####.#######
.##..##.###..##.##.....###
#..#..###..##.#...#..####.
....#.#...##.##....#.#..##
..#.#.###.####..##.###.#.#
.#..##.#####.##.####..#.#.
#..##.#.#.###.#..##.##....
#.#.##.#.##.##......###.#.
#####...###.####..#.##....
.#####.#.#..#.##.#.#...###
.#..#.##.#.#.##.#....###.#
.......###.#....##.....###
#..#####.#..#..##..##.#.##
##.#.###..######.###..#..#
#.#....####.##.###....####
..#.#.#.########.....#.#.#
.##.#.#..#...###.####..##.
##...###....#.##.##..#....
..##.##.##.#######..#...#.
.###..#.#..#...###..###.#.
#..#..#######..#.#..#..#.#
    """.trimIndent()

    val lines = input.lines()
    val result = Day10Part1().processInput(lines)
    println("Max asteroids seen at "+ result.first +" with "+result.second)

    val secondResult = Day10Part2().processInput(lines, result.first)

    println("200th asteroid hit is $secondResult which equates to an answer of "+ (secondResult.first * 100 + secondResult.second))
}

class AsteroidInfo(val angle: Double, val distance: Double, val location: Pair<Int, Int>)

class Day10Part2 {
    fun processInput(lines: List<String>, base: Pair<Int, Int>): Pair<Int, Int> {
        val asteroidInfo = mutableListOf<AsteroidInfo>()
        for (asteroidY in lines.indices) {
            for (asteroidX in lines[asteroidY].indices) {
                if (lines[asteroidY][asteroidX] == '#' && !(asteroidX == base.first && asteroidY == base.second)) {
                    val deltaX: Double = (asteroidX - base.first).toDouble()
                    val deltaY: Double = (asteroidY - base.second).toDouble()
                    asteroidInfo.add(
                        AsteroidInfo(
                            (Math.toDegrees(atan2(deltaX, deltaY)*-1) + 540) % 360,
                            sqrt(deltaX * deltaX + deltaY * deltaY),
                            Pair(asteroidX, asteroidY)
                        )
                    )
                }
            }
        }

        asteroidInfo.sortWith(compareBy({it.angle}, {it.distance}))
        var killIndex = 0
        for (kill in 1..199) {
            val asteroid = asteroidInfo[killIndex]
            println("$kill: Killing "+asteroid.location)
            val angle = asteroid.angle
            asteroidInfo.remove(asteroid)
            while (asteroidInfo[killIndex].angle == angle) {
                killIndex++
            }
            if (killIndex >= asteroidInfo.size) {
                killIndex = 0
            }
        }

        return asteroidInfo[killIndex].location
    }

}

class Day10Part1 {

    fun processInput(lines: List<String>): Pair<Pair<Int,Int>, Int> {
        var maxAsteroidsSeen = 0
        var maxAsteroidsLocation: Pair<Int, Int> = Pair(0,0)
        for (asteroidY in lines.indices) {
            for (asteroidX in lines[asteroidY].indices) {
                val asteroid = lines[asteroidY][asteroidX]
                if (asteroid == '#') {
                    val asteroidsSeen = calculateAsteroidsSeen(lines, asteroidY, asteroidX)
                    if (asteroidsSeen > maxAsteroidsSeen) {
                        maxAsteroidsSeen = asteroidsSeen
                        maxAsteroidsLocation = Pair(asteroidX, asteroidY)
                    }
                }
            }
        }

        return Pair(maxAsteroidsLocation, maxAsteroidsSeen)
    }

    private fun calculateAsteroidsSeen(asteroidField: List<String>, asteroidY: Int, asteroidX: Int): Int {
        var asteroidsSeen = 0
        for (potentialAsteroidY in asteroidField.indices) {
            for (potentialAsteroidX in asteroidField[potentialAsteroidY].indices) {
                if (potentialAsteroidX == asteroidX && potentialAsteroidY == asteroidY) {
                    // skip ourselves
                    continue
                }
                val asteroid = asteroidField[potentialAsteroidY][potentialAsteroidX]
                if (asteroid == '#') {
                    var visible = true
                    val deltaX = asteroidX - potentialAsteroidX
                    val deltaY = asteroidY - potentialAsteroidY
                    var refactoredX = deltaX
                    var refactoredY = deltaY

                    var gcf = getFactors(deltaX).intersect(getFactors(deltaY)).max();
                    while (gcf != null) {
                        refactoredX /= gcf
                        refactoredY /= gcf
                        gcf = getFactors(refactoredX).intersect(getFactors(refactoredY)).max()
                    }

                    val xStep = if (refactoredY == 0) refactoredX.sign else refactoredX
                    val yStep = if (refactoredX == 0) refactoredY.sign else refactoredY

                    var intersectX = potentialAsteroidX + xStep
                    var intersectY = potentialAsteroidY + yStep

                    while (intersectX >= 0 && intersectY >= 0 && intersectX < asteroidField[potentialAsteroidY].length && intersectY < asteroidField.size) {
                        if (asteroidField[intersectY][intersectX] == '#') {
                            visible = intersectX == asteroidX && intersectY == asteroidY
                            break
                        }
                        intersectX += xStep
                        intersectY += yStep
                    }

                    if (visible) {
                        asteroidsSeen++
                    }
                }
            }
        }
        return asteroidsSeen
    }

    private fun getFactors(x: Int): List<Int> {
        val factors = mutableListOf<Int>()
        val primes = listOf<Int>(2, 3, 5, 7, 11, 13, 17, 19)
        for (prime in primes) {
            if (prime > kotlin.math.abs(x)) {
                break
            }
            if (x.rem(prime) == 0) {
                factors += prime
            }
        }

        return factors
    }

}