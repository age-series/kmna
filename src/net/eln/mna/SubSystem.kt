package net.eln.mna

import net.eln.common.ISerializedCircuit
import net.eln.mna.misc.IDestructor
import net.eln.mna.misc.ISubSystemProcessFlush
import net.eln.mna.misc.ISubSystemProcessI
import net.eln.mna.misc.MnaConst
import net.eln.mna.passive.Component
import net.eln.mna.passive.Resistor
import net.eln.mna.state.CurrentNode
import net.eln.mna.state.Node
import net.eln.mna.state.VoltageNode
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.SingularValueDecomposition
import java.util.*

class SubSystem(root: RootSystem?, val dt: Double): ISerializedCircuit {
    var component = ArrayList<Component>()
    var states: MutableList<Node> = ArrayList()
    var breakDestructor = LinkedList<IDestructor>()
    var interSystemConnectivity = ArrayList<SubSystem>()
    private var processI = ArrayList<ISubSystemProcessI>()
    private var statesTab: Array<Node?>? = null

    var root: RootSystem? = null

    private var matrixValid = false

    private var stateCount: Int = 0
    private var a: RealMatrix? = null
    private var singularMatrix: Boolean = false

    private var aInverseData: Array<DoubleArray>? = null
    private var iData: DoubleArray? = null
    private var xTempData: DoubleArray? = null

    private var breaked = false

    private var processF = ArrayList<ISubSystemProcessFlush>()

    init {
        this.root = root
    }



    fun contains(state: Node): Boolean {
        return states.contains(state)
    }

    fun getXSafe(bPin: Node?): Double {
        return bPin?.state ?: 0.0
    }

    fun addState(s: Node) {
        states.add(s)
        s.addedTo(this)
        invalidate()
    }

    fun addState(i: Iterable<Node>) {
        for (s in i) {
            addState(s)
        }
    }

    fun removeState(s: Node) {
        states.remove(s)
        s.quitSubSystem()
        invalidate()
    }

    fun addComponent(c: Component) {
        component.add(c)
        c.addedTo(this)
        invalidate()
    }

    fun addComponent(i: Iterable<Component>) {
        for (c in i) {
            addComponent(c)
        }
    }

    fun removeComponent(c: Component) {
        component.remove(c)
        c.quitSubSystem()
        invalidate()
    }

    fun addProcess(p: ISubSystemProcessI) {
        processI.add(p)
    }

    fun removeProcess(p: ISubSystemProcessI) {
        processI.remove(p)
        invalidate()
    }

    fun addProcess(p: ISubSystemProcessFlush) {
        processF.add(p)
    }

    fun removeProcess(p: ISubSystemProcessFlush) {
        processF.remove(p)
    }

    fun addToA(a: Node?, b: Node?, v: Double) {
        if (a == null || b == null)
            return
        this.a?.addToEntry(a.id, b.id, v)
    }

    fun addToI(s: Node?, v: Double) {
        if (s == null) return
        iData?.set(s.id, v)
    }



    /**
     * step
     *
     * Only used for testing SubSystem or when you only have one component matrix.
     * RootSystem calls these functions individually.
     */
    fun step() {
        stepCalc()
        stepFlush()
    }

    /**
     * stepCalc
     *
     * Generates a matrix, ???, ???, ???
     */
    fun stepCalc() {
        if (!matrixValid) {
            generateMatrix()
        }
        if (!singularMatrix) {
            for (y in 0 until stateCount) {
                iData?.set(y, 0.0)
            }
            for (p in processI) {
                p.simProcessI(this)
            }
            for (idx2 in 0 until stateCount) {
                var stack = 0.0
                for (idx in 0 until stateCount) {
                    stack += aInverseData!![idx2][idx] * iData?.get(idx)!!
                }
                xTempData!![idx2] = stack
            }
        }
    }

    /**
     * solve
     *
     * Very similar to stepCalc, except it returns a single requested Node state.
     */
    fun solve(pin: Node): Double {
        if (!matrixValid) {
            generateMatrix()
        }

        if (!singularMatrix) {
            for (y in 0 until stateCount) {
                iData?.set(y, 0.0)
            }
            for (p in processI) {
                p.simProcessI(this)
            }

            val idx2 = pin.id
            var stack = 0.0
            for (idx in 0 until stateCount) {
                stack += aInverseData!![idx2][idx] * iData!![idx]
            }
            return stack
        }
        return 0.0
    }

    /**
     * stepFlush
     *
     * ???, then runs flush processes.
     */
    fun stepFlush() {
        if (!singularMatrix) {
            for (idx in 0 until stateCount) {
                statesTab?.get(idx)?.state = xTempData!![idx]
            }
        } else {
            for (idx in 0 until stateCount) {
                statesTab?.get(idx)?.state = 0.0
            }
        }

        for (p in processF) {
            p.simProcessFlush()
        }
    }

    /**
     * generateMatrix
     */
    private fun generateMatrix() {
        stateCount = states.size

        a = MatrixUtils.createRealMatrix(stateCount, stateCount)

        iData = DoubleArray(stateCount)
        xTempData = DoubleArray(stateCount)
        run {
            var idx = 0
            for (s in states) {
                s.id = idx++
            }
        }

        for (c in component) {
            c.applyTo(this)
        }

        val svd = SingularValueDecomposition(a)
        // Broken or large numbers are bad. Inverses are typically pretty ill-conditioned, but we're looking for egregious ones.
        // For every order of magnitude from 10^n, we get n more digits of error (apparently).
        // Some people say 10e8 or 10e12 may be more realistic? Not sure I want that much error. I set 10e4 for now.
        // Doubles have (roughly?) 15 decimal digits of precision. I can see 4 of them go away without too much trouble.
        if(svd.conditionNumber.isNaN() or (svd.conditionNumber > 10e4)) {
            MnaConst.logger.warn("Condition of Matrix: ${svd.conditionNumber}")
            for (row in a!!.data) {
                MnaConst.logger.warn(row.joinToString())
            }
        }

        try {
            aInverseData = QRDecomposition(a).solver.inverse.data
            singularMatrix = false
        } catch (e: org.apache.commons.math3.linear.SingularMatrixException) {
            singularMatrix = true
            if (stateCount > 1) {
                MnaConst.logger.error("//////////SingularMatrix////////////")
                for (row in a!!.data) {
                    MnaConst.logger.error(row.joinToString())
                }
            }
        }

        // TODO: Does this line do anything at all?
        statesTab = arrayOfNulls(stateCount)
        statesTab = states.toTypedArray()

        // TODO: But is it if there is a singular matrix?
        matrixValid = true
    }

    fun breakSystem(): Boolean {
        if (breaked) return false
        while (!breakDestructor.isEmpty()) {
            breakDestructor.pop().destruct()
        }

        for (c in component) {
            c.quitSubSystem()
        }
        for (s in states) {
            s.quitSubSystem()
        }

        if (root != null) {
            for (c in component) {
                c.returnToRootSystem(root)
            }
            for (s in states) {
                s.returnToRootSystem(root!!)
            }
        }
        root!!.systems.remove(this)

        invalidate()

        breaked = true
        return true
    }

    fun invalidate() {
        matrixValid = false
    }

    override fun toString(): String {
        var str = ""
        for (c in component) {
            str += c.toString()
        }
        return str
    }

    override fun exportCircuit(): String {
        val nodeSerializedProperties = states.map {Pair(it, it.exportProperties())}
        val componentSerializedProperties = component.map {Pair(it, it.exportProperties())}
        var exportString = ""
        exportString += nodeSerializedProperties.map {
            "${it.first.typeString} ${it.first.id} ${it.second.map {it.toString()}.joinToString(" ")}"
        }.joinToString("\n", "","\n")
        exportString += componentSerializedProperties.map {
            "${it.first.typeString} ${it.second.second.map { it?.id.toString() }.joinToString(" ")} ${it.second.first.map { it.toString()}.joinToString (" ")}"
        }.joinToString("\n",  "","\n")
        return exportString
    }

    override fun importCircuit(data: String) {
        // TODO: Errrrrr
    }
}
