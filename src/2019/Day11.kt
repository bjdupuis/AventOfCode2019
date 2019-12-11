package `2019`

import java.lang.StringBuilder
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val input = "3,8,1005,8,351,1106,0,11,0,0,0,104,1,104,0,3,8,102,-1,8,10,1001,10,1,10,4,10,108,1,8,10,4,10,102,1,8,28,3,8,1002,8,-1,10,101,1,10,10,4,10,1008,8,0,10,4,10,1002,8,1,51,1006,0,85,2,1109,8,10,3,8,1002,8,-1,10,101,1,10,10,4,10,1008,8,0,10,4,10,102,1,8,80,1,2,2,10,1,1007,19,10,1,1001,13,10,3,8,1002,8,-1,10,1001,10,1,10,4,10,108,1,8,10,4,10,1001,8,0,113,1,2,1,10,1,1109,17,10,1,108,20,10,2,1005,3,10,3,8,102,-1,8,10,1001,10,1,10,4,10,108,1,8,10,4,10,1002,8,1,151,2,5,19,10,1,104,19,10,1,109,3,10,1006,0,78,3,8,102,-1,8,10,1001,10,1,10,4,10,1008,8,0,10,4,10,1002,8,1,189,1006,0,3,2,1004,1,10,3,8,1002,8,-1,10,101,1,10,10,4,10,1008,8,1,10,4,10,1001,8,0,218,1,1008,6,10,1,104,8,10,1006,0,13,3,8,1002,8,-1,10,101,1,10,10,4,10,1008,8,0,10,4,10,102,1,8,251,1006,0,17,1006,0,34,1006,0,24,1006,0,4,3,8,102,-1,8,10,1001,10,1,10,4,10,1008,8,0,10,4,10,102,1,8,285,1006,0,25,2,1103,11,10,1006,0,75,3,8,1002,8,-1,10,1001,10,1,10,4,10,108,1,8,10,4,10,101,0,8,316,2,1002,6,10,1006,0,30,2,106,11,10,1006,0,21,101,1,9,9,1007,9,1072,10,1005,10,15,99,109,673,104,0,104,1,21101,0,937151009684,1,21101,0,368,0,1105,1,472,21102,386979963796,1,1,21102,379,1,0,1106,0,472,3,10,104,0,104,1,3,10,104,0,104,0,3,10,104,0,104,1,3,10,104,0,104,1,3,10,104,0,104,0,3,10,104,0,104,1,21101,179410325723,0,1,21101,426,0,0,1106,0,472,21101,0,179355823195,1,21102,437,1,0,1106,0,472,3,10,104,0,104,0,3,10,104,0,104,0,21101,0,825460785920,1,21101,460,0,0,1105,1,472,21102,1,838429614848,1,21102,1,471,0,1105,1,472,99,109,2,21202,-1,1,1,21102,40,1,2,21102,1,503,3,21101,493,0,0,1105,1,536,109,-2,2106,0,0,0,1,0,0,1,109,2,3,10,204,-1,1001,498,499,514,4,0,1001,498,1,498,108,4,498,10,1006,10,530,1101,0,0,498,109,-2,2106,0,0,0,109,4,2101,0,-1,535,1207,-3,0,10,1006,10,553,21101,0,0,-3,21202,-3,1,1,22101,0,-2,2,21101,0,1,3,21101,572,0,0,1105,1,577,109,-4,2105,1,0,109,5,1207,-3,1,10,1006,10,600,2207,-4,-2,10,1006,10,600,21202,-4,1,-4,1106,0,668,21202,-4,1,1,21201,-3,-1,2,21202,-2,2,3,21102,619,1,0,1105,1,577,22102,1,1,-4,21101,0,1,-1,2207,-4,-2,10,1006,10,638,21101,0,0,-1,22202,-2,-1,-2,2107,0,-3,10,1006,10,660,22101,0,-1,1,21101,660,0,0,106,0,535,21202,-2,-1,-2,22201,-4,-2,-4,109,-5,2105,1,0"
    val initialRegisters = input.split(',').map(String::toLong)
    var robotBrain = IntCode(mutableListOf<Long>().apply { addAll(initialRegisters)}, "Robot1")
    val day11Part1 = Day11Part1(robotBrain)

    println("Total squares painted at least once is " + day11Part1.paintShip())

    robotBrain = IntCode(mutableListOf<Long>().apply { addAll(initialRegisters)}, "Robot2")
    val day11Part2 = Day11Part2(robotBrain)

    for (line in day11Part2.paintShip()) {
        println(line)
    }
}


class Day11Part1(val robotBrain: IntCode) {
    enum class Color(val value: Int) {
        BLACK(0), WHITE(1)
    }

    enum class Turn(val value: Int) {
        LEFT(0),
        RIGHT(1)
    }

    enum class Facing {
        UP, DOWN, LEFT, RIGHT
    }

    enum class RobotState {
        PAINTING, MOVING
    }

    private var currentPosition = Pair(0,0)
    private var currentFacing = Facing.UP
    private var currentState = RobotState.PAINTING
    private val shipSide = mutableMapOf<Pair<Int,Int>, Color>()
    private var panelsPainted = 0

    fun paintShip(): Int {
        val robotFinished = CountDownLatch(1)

        robotBrain.completePublishSubject
            .subscribe { robotFinished.countDown() }

        robotBrain.outputSubject
            .subscribe { output -> processRobotOutput(output) }

        // kick it off
        robotBrain.inputSubject.onNext(0L)

        robotFinished.await()

        return panelsPainted
    }

    private fun processRobotOutput(output: Long) {
        when (currentState) {
            RobotState.PAINTING -> {
                if (!shipSide.contains(currentPosition)) {
                    panelsPainted++
                }
                shipSide[currentPosition] = when(output) {
                    0L -> Color.BLACK
                    1L -> Color.WHITE
                    else -> {
                        println("Unexpected color $output")
                        exitProcess(-1)
                    }
                }
                currentState = RobotState.MOVING
            }

            RobotState.MOVING -> {
                when(output) {
                    0L -> {
                        currentFacing = when(currentFacing) {
                            Facing.UP -> Facing.LEFT
                            Facing.LEFT -> Facing.DOWN
                            Facing.DOWN -> Facing.RIGHT
                            Facing.RIGHT -> Facing.UP
                        }
                    }
                    1L -> {
                        currentFacing = when(currentFacing) {
                            Facing.UP -> Facing.RIGHT
                            Facing.LEFT -> Facing.UP
                            Facing.DOWN -> Facing.LEFT
                            Facing.RIGHT -> Facing.DOWN
                        }
                    }
                }
                currentPosition = when(currentFacing) {
                    Facing.UP -> Pair(currentPosition.first, currentPosition.second-1)
                    Facing.LEFT -> Pair(currentPosition.first-1, currentPosition.second)
                    Facing.DOWN -> Pair(currentPosition.first, currentPosition.second+1)
                    Facing.RIGHT -> Pair(currentPosition.first+1, currentPosition.second)
                }

                currentState = RobotState.PAINTING

                val color = shipSide.getOrDefault(currentPosition, Color.BLACK)
                robotBrain.inputSubject.onNext(color.value.toLong())
            }
        }
    }
}

class Day11Part2(val robotBrain: IntCode) {
    enum class Color(val value: Int) {
        BLACK(0), WHITE(1)
    }

    enum class Turn(val value: Int) {
        LEFT(0),
        RIGHT(1)
    }

    enum class Facing {
        UP, DOWN, LEFT, RIGHT
    }

    enum class RobotState {
        PAINTING, MOVING
    }

    private var currentPosition = Pair(0,0)
    private var currentFacing = Facing.UP
    private var currentState = RobotState.PAINTING
    private val shipSide = mutableMapOf<Pair<Int,Int>, Color>()

    fun paintShip(): List<String> {
        val robotFinished = CountDownLatch(1)

        robotBrain.completePublishSubject
            .subscribe { robotFinished.countDown() }

        robotBrain.outputSubject
            .subscribe { output -> processRobotOutput(output) }

        // kick it off
        robotBrain.inputSubject.onNext(1L)

        robotFinished.await()

        val minX = shipSide.keys.minBy { it.first }!!.first
        val maxX = shipSide.keys.maxBy { it.first }!!.first
        val minY = shipSide.keys.minBy { it.second }!!.second
        val maxY = shipSide.keys.maxBy { it.second }!!.second

        val finalPaintedPanel = mutableListOf<String>()
        for (y in minY..maxY) {
            val stringBuilder = StringBuilder()
            for (x in minX..maxX) {
                stringBuilder.append(when (shipSide.getOrDefault(Pair(x,y),Color.BLACK)) {
                    Color.BLACK -> ' '
                    Color.WHITE -> '*'
                })
            }
            finalPaintedPanel.add(stringBuilder.toString())
        }

        return finalPaintedPanel
    }

    private fun processRobotOutput(output: Long) {
        when (currentState) {
            RobotState.PAINTING -> {
                shipSide[currentPosition] = when(output) {
                    0L -> Color.BLACK
                    1L -> Color.WHITE
                    else -> {
                        println("Unexpected color $output")
                        exitProcess(-1)
                    }
                }
                currentState = RobotState.MOVING
            }

            RobotState.MOVING -> {
                when(output) {
                    0L -> {
                        currentFacing = when(currentFacing) {
                            Facing.UP -> Facing.LEFT
                            Facing.LEFT -> Facing.DOWN
                            Facing.DOWN -> Facing.RIGHT
                            Facing.RIGHT -> Facing.UP
                        }
                    }
                    1L -> {
                        currentFacing = when(currentFacing) {
                            Facing.UP -> Facing.RIGHT
                            Facing.LEFT -> Facing.UP
                            Facing.DOWN -> Facing.LEFT
                            Facing.RIGHT -> Facing.DOWN
                        }
                    }
                }
                currentPosition = when(currentFacing) {
                    Facing.UP -> Pair(currentPosition.first, currentPosition.second-1)
                    Facing.LEFT -> Pair(currentPosition.first-1, currentPosition.second)
                    Facing.DOWN -> Pair(currentPosition.first, currentPosition.second+1)
                    Facing.RIGHT -> Pair(currentPosition.first+1, currentPosition.second)
                }

                currentState = RobotState.PAINTING

                val color = shipSide.getOrDefault(currentPosition, Color.BLACK)
                robotBrain.inputSubject.onNext(color.value.toLong())
            }
        }
    }
}
