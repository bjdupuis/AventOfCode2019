package `2019`

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import org.jgrapht.graph.DefaultUndirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.graph.builder.GraphTypeBuilder
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val input = """
########################
#f.D.E.e.C.b.A.@.a.B.c.#
######################.#
#d.....................#
########################
    """.trimIndent()

    println("Steps in shortest path to get all keys is "+Day18(input).calculatePart1())


}

class Day18(input: String) {
    private val maze = input.lines()
    private val terminal: Terminal = DefaultTerminalFactory().createTerminal()

    open class Point(val x: Int, val y: Int) {
        fun adjacents(): Set<Point> {
            return mutableSetOf<Point>().apply {
                add(Point(x, y-1))
                add(Point(x, y+1))
                add(Point(x+1, y))
                add(Point(x-1, y))
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

    class Key(x: Int, y: Int, val name: Char) : Point(x, y)

    class Door(x: Int, y: Int, val name: Char) : Point(x, y)

    fun calculatePart1(): Int {
        val tunnelNodes = mutableListOf<Point>()
        val keys = mutableSetOf<Key>()
        val doors = mutableSetOf<Door>()
        var currentLocation: Point? = null
        for (lineIndex in maze.indices) {
            val line = maze[lineIndex]
            for (charIndex in line.indices) {
                val location = Point(charIndex, lineIndex)
                if (line[charIndex] != '#') {
                    tunnelNodes.add(location)
                    when (line[charIndex]) {
                        '@' -> currentLocation = location
                        in 'a'..'z' -> keys.add(Key(charIndex, lineIndex, line[charIndex]))
                        in 'A'..'Z' -> doors.add(Door(charIndex, lineIndex, line[charIndex]))
                    }
                }
            }
        }

        val graph = DefaultUndirectedWeightedGraph<Point, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)
        for (node in tunnelNodes) {
            graph.addVertex(node)
            node.adjacents().stream().filter { tunnelNodes.contains(it) }.forEach {
                graph.addVertex(it)
                graph.addEdge(node, it)?.apply {
                    graph.setEdgeWeight(this, 1.0)
                }
            }
        }

        terminal.enterPrivateMode()
        terminal.addResizeListener { terminal, _ -> terminal.clearScreen() }
        val textGraphics = terminal.newTextGraphics()
        terminal.setCursorPosition(terminal.terminalSize.columns/2, terminal.terminalSize.rows/2)
        terminal.setCursorVisible(false)
        terminal.setBackgroundColor(TextColor.ANSI.BLACK)
        terminal.setForegroundColor(TextColor.ANSI.WHITE)

        updateTerminal(keys, doors, currentLocation)

        var stepsTaken = 0
        while (keys.isNotEmpty()) {
            val availableGraph = determineAvailableGraphForDoors(graph, doors)
            val nextKey = determineNextKeyToGet(availableGraph, keys, doors, currentLocation!!)
            if (nextKey != null) {
                DijkstraShortestPath<Point, DefaultWeightedEdge>(graph).getPath(currentLocation, nextKey).vertexList.drop(1).forEach { vertex ->
                    stepsTaken ++
                    currentLocation = vertex
                    updateTerminal(keys, doors, currentLocation)
                    textGraphics.putString(0, 0, "$stepsTaken steps taken")
                    Thread.sleep(250)
                }
                keys.remove(nextKey)
                doors.removeIf { it.name == nextKey.name.toUpperCase() }
                updateTerminal(keys, doors, currentLocation)
                textGraphics.putString(0, 0, "$stepsTaken steps taken")
            } else {
                println("We're screwed")
                exitProcess(-1)
            }
        }

        terminal.readInput()

        return stepsTaken
    }

    private fun determineAvailableGraphForDoors(graph: Graph<Point, DefaultWeightedEdge>, doors: MutableSet<Door>): Graph<Point, DefaultWeightedEdge> {
        return DefaultUndirectedWeightedGraph<Point, DefaultWeightedEdge>(DefaultWeightedEdge::class.java).apply {
            graph.vertexSet()
                .forEach { vertex ->
                    graph.edgesOf(vertex)
                        .forEach { edge ->
                            addVertex(graph.getEdgeSource(edge))
                            addVertex(graph.getEdgeTarget(edge))
                            addEdge(getEdgeSource(edge), getEdgeTarget(edge))?.let {
                                setEdgeWeight(it, graph.getEdgeWeight(edge))
                            }
                        }
                }
        }.apply {
            for (door in doors) {
                removeVertex(door)
            }
        }
    }

    private fun updateTerminal(keys: MutableSet<Key>, doors: MutableSet<Door>, currentLocation: Point?) {
        terminal.clearScreen()
        for (lineIndex in maze.indices) {
            val line = maze[lineIndex]
            for (charIndex in line.indices) {
                val location = Point(charIndex, lineIndex)
                terminal.setCursorPosition(location.x, location.y+1)
                when (line[charIndex]) {
                    '#' -> terminal.putCharacter(line[charIndex])
                    else -> {
                        if (currentLocation == location) {
                            terminal.putCharacter('@')
                        } else {
                            doors.firstOrNull { it.x == location.x && it.y == location.y }?.let {
                                terminal.putCharacter(it.name)
                            }
                            keys.firstOrNull { it.x == location.x && it.y == location.y }?.let {
                                terminal.putCharacter(it.name)
                            }
                        }
                    }
                }
            }
        }

        terminal.flush()
    }

    private fun determineNextKeyToGet(graph: Graph<Point, DefaultWeightedEdge>, keys: MutableSet<Key>, doors: MutableSet<Door>, location: Point): Key? {
        val paths = DijkstraShortestPath<Point, DefaultWeightedEdge>(graph).getPaths(location)

        return keys.mapNotNull() { key ->
            paths.getPath(key)
        }.filter { path ->
            path.vertexList.intersect(keys.minus(path.endVertex)).isEmpty()
        }.minBy { path ->
            path.weight
        }.let { path ->
            keys.firstOrNull { it.x == path?.endVertex?.x && it.y == path.endVertex.y }
        }
    }

}