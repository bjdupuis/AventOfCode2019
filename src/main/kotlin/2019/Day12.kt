package `2019`

import com.jme3.app.SimpleApplication
import com.jme3.asset.AssetManager
import com.jme3.light.DirectionalLight
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Sphere
import com.jme3.util.TangentBinormalGenerator
import kotlin.math.abs

fun main(args: Array<String>) {
    Day12().start()
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