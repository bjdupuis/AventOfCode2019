package `2019`

import org.w3c.dom.NodeList
import kotlin.math.ceil
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val input="""
4 ZDGD, 1 HTRQV => 3 VRKNQ
15 XKZQZ, 1 MWZQ => 4 LHWX
1 WVPKL => 2 HJLX
1 LFDGN => 7 DMPX
14 HJLX, 3 KGKVK, 1 XQSVS => 6 HGSM
8 FKQS, 8 MWVCW => 3 MVSK
2 HGZLR, 2 WVPKL, 29 VRKNQ => 2 MDKZ
1 RLGBD, 22 VWFV => 6 MSGJ
24 PHLTR, 2 MWVCW => 8 JZMS
5 XSJLQ, 2 PFTM => 1 NCRJ
3 QNBK => 8 LKWK
16 HGSM => 3 BKHV
138 ORE => 5 SBXDS
3 KGKVK => 8 MTCZW
1 MDKZ, 8 HGNB => 5 HLDW
9 BKHV, 5 WDVS, 1 HGSM => 4 PFTM
1 PFNM, 14 MVSK => 3 VQCQ
16 LTXF => 4 TSKNX
5 VQCQ, 16 NFSVL, 5 HJLX, 1 TSKNX, 16 DMPX, 1 MSGJ, 3 BKHV => 7 CTHPF
8 WVPKL, 5 LHWX => 4 KGKVK
2 HLDW, 21 KSCS, 4 MTCZW, 1 DMPX, 1 LKWK, 7 NGVH, 12 HJLX, 18 MVSK => 9 VLGFJ
195 ORE => 3 NTZKP
6 FKQS => 1 GMJRS
6 LSLR, 8 HJLX => 4 NFSVL
16 NTZKP, 3 ZDGD => 8 XKZQZ
20 HLDW, 1 WDVS, 6 KGKVK => 7 PFNM
9 LHWX, 2 HLDW, 2 JZMS => 4 QNBK
1 RLGBD, 8 CKSPZ => 7 WDVS
3 RLGBD => 9 LTXF
14 SBXDS, 1 NTZKP => 7 FZBGM
14 CKSPZ, 1 MWZQ, 4 RLGBD => 8 NGVH
1 FKQS => 1 QWVC
6 MWZQ => 4 PBWF
4 ZDGD, 5 WVPKL => 4 FLWK
5 HLDW, 6 FKQS, 35 VLGFJ, 20 MVSK, 13 QKVZ, 5 CTHPF => 1 FUEL
5 QWVC, 10 LFDGN => 5 CKSPZ
4 QWVC, 4 FKQS, 1 MWVCW => 9 VWFV
1 SBXDS => 2 XQSVS
160 ORE => 9 HGZLR
1 KGKVK, 3 HJLX, 2 HGNB => 8 KSCS
6 GMJRS => 9 PHLTR
1 LFDGN, 9 XQSVS, 37 PBWF => 3 LSLR
7 FZBGM, 4 FNJX => 7 KFHFS
4 MVSK, 1 NFSVL, 2 NCRJ, 24 BKHV, 5 RLGBD, 5 NGVH => 9 QKVZ
4 VWFV, 1 RLGBD => 3 CMZQF
102 ORE => 4 ZDGD
1 SBXDS => 2 WVPKL
2 HTRQV => 1 HGNB
2 KFHFS, 7 FLWK, 5 WVPKL => 9 FKQS
5 GMJRS, 10 LHWX => 4 RLGBD
8 BKHV, 8 CMZQF => 3 XSJLQ
2 XQSVS => 6 LFDGN
103 ORE => 5 HTRQV
28 HGZLR, 2 ZDGD => 5 FNJX
6 VRKNQ, 1 XKZQZ => 7 MWZQ
10 ZDGD => 8 MWVCW
    """.trimIndent().lines()

    val day14 = Day14()
    val nodeList = day14.processInput(input)
    println("Amount of ore required to produce a single fuel is "+ day14.processPart1(nodeList))

    println("Amount of fuel created is "+day14.processPart2(nodeList))
}


class Day14 {

    fun processPart2(nodeList: List<TreeNode>): Long {
        val oreAvailable = 1000000000000L
        var idealFuel = 0L

        var done = false
        var high = 20000000L
        var low = 0L
        var mid = 0L
        while (!done) {
            mid = low + (high - low) / 2
            print("Trying $mid...")
            val fuel = nodeList.first { it.name == "FUEL" }
            val factory = ProductFactory(nodeList)
            val oreRemaining = oreAvailable - factory.produce(fuel, mid);
            println(" got $oreRemaining")
            when {
                oreRemaining > 0 -> {
                    low = mid + 1
                }
                oreRemaining < 0 -> {
                    high = mid - 1
                }
                else -> {
                    return mid
                }
            }

            if (mid == low || mid == high) {
                return if (oreRemaining < 0) mid - 1 else mid
            }
        }

        return idealFuel;
    }

    fun processPart1(nodeList: List<TreeNode>): Long {
        return ProductFactory(nodeList).produce(nodeList.first { it.name == "FUEL" })
    }

    class TreeNode(val name: String, var numberNeededToSatisfy: Long) {
        val dependencies: MutableList<TreeNode> = mutableListOf()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TreeNode

            if (name != other.name) return false

            return true
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }

    }

    class ProductFactory(private val recipes: List<TreeNode>) {
        private val inventory: MutableMap<TreeNode, Long> = mutableMapOf()

        fun produce(product: TreeNode?, produceQuantity: Long): Long {
            var oreRequired = 0L

            if (product == null) {
                println("Unexpected")
                exitProcess(-1)
            }

            if (product.name == "ORE") {
                return produceQuantity
            }
            val recipe = recipes.firstOrNull { it.name == product.name } ?: exitProcess(-1)
            if (inventory.getOrDefault(product, 0) < produceQuantity) {
                val amountToProduce = produceQuantity - inventory.getOrDefault(product, 0)
                inventory[product] = 0

                val recipesToMake = ceil((amountToProduce.toFloat() / recipe.numberNeededToSatisfy.toFloat())).toLong()
                inventory[product] = (recipesToMake * recipe.numberNeededToSatisfy) - amountToProduce

                for (dependency in recipe.dependencies) {
                    oreRequired += produce(dependency, recipesToMake * dependency.numberNeededToSatisfy)
                }
            } else {
                inventory[product] = inventory.getOrDefault(product, 0) - produceQuantity
            }


            return oreRequired;
        }

        fun produce(product: TreeNode?): Long {
            var oreRequired = 0L
            if (product == null) {
                println("Unexpected")
                exitProcess(-1)
            }
            val recipe = recipes.firstOrNull { it.name == product.name } ?: exitProcess(-1)
            inventory[recipe] = inventory.getOrDefault(recipe, 0) - 1

            if (inventory.getOrDefault(recipe, 0) < 0) {
                inventory[recipe] = inventory.getOrDefault(recipe, 0L) + recipe.numberNeededToSatisfy
                for (dependency in recipe.dependencies) {
                    oreRequired += if (dependency.name == "ORE") {
                        dependency.numberNeededToSatisfy
                    } else {
                        var interim = 0L
                        for (products in 0 until dependency.numberNeededToSatisfy) {
                            interim += produce(dependency)
                        }
                        interim
                    }
                }
            }
            return oreRequired
        }
    }

    fun processInput(input: List<String>): List<TreeNode> {
        val nodeList: MutableList<TreeNode> = mutableListOf()
        for (line in input) {
            val (inputDependencies, product) = line
                .split(" => ")
                .map {
                    it.split(", ")
                }
                .map { amountAndTypeString ->
                    amountAndTypeString.map { strings ->
                        strings.split(" ").let { amountAndType ->
                            Pair(amountAndType.last(), amountAndType.first().toLong())
                        }
                    }
                }
            with(product.first()) {
                val node = TreeNode(first, second).apply {
                    for (newDependency in inputDependencies) {
                        dependencies.add(TreeNode(newDependency.first, newDependency.second))
                    }
                }
                nodeList.add(node)
            }
        }
        return nodeList
    }
}