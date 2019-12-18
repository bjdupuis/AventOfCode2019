package `2019`

import com.jme3.app.SimpleApplication
import com.jme3.asset.AssetManager
import com.jme3.light.DirectionalLight
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Sphere
import com.jme3.util.TangentBinormalGenerator
import java.util.zip.CRC32
import kotlin.math.abs

fun main(args: Array<String>) {
    val moonLocations = """
<x=8, y=0, z=8>
<x=0, y=-5, z=-10>
<x=16, y=10, z=-5>
<x=19, y=-10, z=-7>
    """.trimIndent().lines().map { line ->
        line.drop(1).dropLast(1).split(", ").map {
            it.drop(2).toInt()
        }
    }

    //Day12().start()
    println ("Part 2 answer is "+Day12Part2().processInput(moonLocations))
}

class Day12Part2 {

    fun processInput(moonLocations: List<List<Int>>): Long {
        val steps = mutableListOf<Long>()
        for (i in 0..2) {
            steps.add(findCycleLength(mutableListOf<Int>().apply {
                for (index in moonLocations.indices) {
                    add(moonLocations[index][i])
                }
            }))

            println ("Found ${steps[i]}")
        }

        return steps.asSequence().map {
            getFactors(it)
        }.map { factors ->
            factors.groupBy { it }
        }.fold(mutableMapOf<Int, List<Int>>()) { initial, current ->
            current.entries.forEach { entry ->
                initial[entry.key] = if (entry.value.size > initial[entry.key]?.size ?: 0) entry.value else initial[entry.key]!!
            }
            initial
        }.map { entry ->
            entry.value
        }.flatten().fold(1L) { acc: Long, i: Int -> acc * i }

    }

    private fun findCycleLength(initialPositions: List<Int>): Long {
        val velocities = MutableList(initialPositions.size) { 0 }
        val positions = mutableListOf<Int>().apply { addAll(initialPositions) }
        val initialCrc = calculateCrc(positions[0], positions[1], positions[2], positions[3], velocities[0], velocities[1], velocities[2], velocities[3])
        var steps = 0L

        do {
            val gravity = MutableList(4) { 0 }
            val indices = mutableListOf<Int>().apply { addAll(positions.indices) }

            while (indices.isNotEmpty()) {
                val index = indices.first()
                indices.remove(index)
                for (compareTo in indices) {
                    gravity[index] += positions[compareTo].compareTo(positions[index])
                    gravity[compareTo] += positions[index].compareTo(positions[compareTo])
                }
            }

            for (i in velocities.indices) {
                velocities[i] += gravity[i]
                positions[i] += velocities[i]
            }

            steps++

        } while(positions != initialPositions || velocities.any { it != 0 })

        return steps
    }

    private fun getFactors(x: Long): List<Int> {
        val factors = mutableListOf<Int>()
        var done = false
        var value = x
        while (!done) {
            if (isPrime(value)) {
                if (value != 1L) {
                    factors.add(value.toInt())
                }
                done = true
                break
            }
            for (i in 2..value/2) {
                if (value.rem(i) == 0L) {
                    factors.add(i.toInt())
                    value /= i
                    break
                }
            }
        }

        if (factors.isEmpty()) {
            factors.add(x.toInt())
        }

        return factors
    }

    private fun isPrime(x: Long): Boolean {
        for (divisor in 2..x/2) {
            if (x.rem(divisor) == 0L) {
                return false
            }
        }
        return true
    }

    private fun calculateCrc(i: Int, i1: Int, i2: Int, i3: Int, v: Int, v1: Int, v2: Int, v3: Int): Long {
        return CRC32().apply {
            update("$i $i1 $i2 $i3 $v $v1 $v2 $v3".toByteArray())
        }.value
    }

    class Point(val x: Int, val y: Int, val z: Int)
}

class Day12: SimpleApplication() {

        val moonLocations = """
<x=8, y=0, z=8>
<x=0, y=-5, z=-10>
<x=16, y=10, z=-5>
<x=19, y=-10, z=-7>
    """.trimIndent().lines().map { line ->
            line.drop(1).dropLast(1).split(", ").map {
                it.drop(2).toFloat()
            }
        }

    val moonNamesAndColors = listOf<Pair<String, ColorRGBA>>(
        Pair("Io", ColorRGBA.Blue),
        Pair("Europa", ColorRGBA.Red),
        Pair("Ganymede", ColorRGBA.Green),
        Pair("Calisto", ColorRGBA.Yellow)
    )

    class Point(var x: Float, var y: Float, var z: Float) {
        override fun toString(): String {
            return "(x=$x, y=$y, z=$z)"
        }
    }

    class Moon(val name: String, private val color: ColorRGBA, val location: Point, val velocity: Point) {
        lateinit var geometry: Geometry

        fun createGeometry(assetManager: AssetManager): Geometry {
            geometry = Geometry(name, Sphere(100, 100, 0.1f).apply {
                textureMode = Sphere.TextureMode.Projected
            }. also {
                TangentBinormalGenerator.generate(it)
            }).apply {

                val material = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md").apply {
                    //setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"))
                    //setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"))
                    setBoolean("UseMaterialColors", true)
                    setColor("Diffuse", color)
                    setColor("Specular", ColorRGBA.White)
                    setFloat("Shininess", 64f)
                }
                setMaterial(material)
                with(location) {
                    setLocalTranslation(x, y, z)
                }
            }

            return geometry
        }

        fun updateLocation(x: Float, y: Float, z: Float) {
            location.x = x
            location.y = y
            location.z = z
        }

        fun updateVelocity(x: Float, y: Float, z: Float) {
            val factor = 1f
            velocity.x = x
            velocity.y = y
            velocity.z = z
            geometry.setLocalTranslation(x*factor, y*factor, z*factor)
        }

        val kineticEnergy: Int
            get() {
                return (abs(velocity.x) + abs(velocity.y) + abs(velocity.z)).toInt()
            }

        val potentialEnergy: Int
            get() {
                return (abs(location.x) + abs(location.y) + abs(location.z)).toInt()
            }
    }

    var moons: MutableList<Moon> = mutableListOf()

    override fun simpleInitApp() {
        for (i in moonNamesAndColors.indices) {
            with(moonNamesAndColors[i]) {
                val moonLocation = moonLocations[i]
                moons.add(Moon(this.first, this.second, Point(moonLocation[0], moonLocation[1], moonLocation[2]), Point(0f, 0f, 0f)))
            }
            rootNode.attachChild(moons[i].createGeometry(assetManager))
        }

        rootNode.addLight(DirectionalLight().apply {
            direction = Vector3f(1f, 0f, -2f).normalizeLocal()
            color = ColorRGBA.White
        })
    }

    private var step = 0
    override fun simpleUpdate(tpf: Float) {
        val factor = tpf
        val moonGravity = List(4) { Point(0f, 0f, 0f) }
        val moonIndices = mutableListOf<Int>().apply { addAll(moons.indices) }
        while (moonIndices.isNotEmpty()) {
            val moonIndex = moonIndices.first()
            val moon = moons[moonIndex]
            moonIndices.remove(moonIndex)
            for (secondIndex in moonIndices) {
                val second = moons[secondIndex]
                moonGravity[moonIndex].x += second.location.x.compareTo(moon.location.x) * factor
                moonGravity[moonIndex].y += second.location.y.compareTo(moon.location.y) * factor
                moonGravity[moonIndex].z += second.location.z.compareTo(moon.location.z) * factor

                moonGravity[secondIndex].x += moon.location.x.compareTo(second.location.x) * factor
                moonGravity[secondIndex].y += moon.location.y.compareTo(second.location.y) * factor
                moonGravity[secondIndex].z += moon.location.z.compareTo(second.location.z) * factor
            }
        }

        step++
        var energy = 0

        for (moonIndex in moons.indices) {
            with(moons[moonIndex]) {
                val gravity = moonGravity[moonIndex]
                updateVelocity(velocity.x + gravity.x, velocity.y + gravity.y, velocity.z + gravity.z)
                updateLocation(location.x + velocity.x, location.y + velocity.y, location.z + velocity.z)
                println("$step: Moon is "+this.location+this.velocity)
                energy += this.kineticEnergy * this.potentialEnergy
            }
        }

        if (step == 1000) {
            println(" -- Energy is $energy")
        }

    }
}