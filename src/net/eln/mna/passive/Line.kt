package net.eln.mna.passive

import net.eln.mna.RootSystem
import net.eln.mna.SubSystem
import net.eln.mna.misc.IAbstractor
import net.eln.mna.misc.ISubSystemProcessFlush
import net.eln.mna.state.Node
import java.util.LinkedList

class Line : Resistor(), ISubSystemProcessFlush, IAbstractor {

    var resistors = LinkedList<Resistor>() //from a to b
    var states = LinkedList<Node>() //from a to b

    override var abstractorSubSystem: SubSystem
        get() = subSystem ?: throw Exception()
        set(value) {}

    internal var ofInterSystem: Boolean = false

    internal fun canAdd(c: Component): Boolean {
        return c is Resistor
    }

    internal fun add(c: Resistor) {
        ofInterSystem = ofInterSystem or c.canBeReplacedByInterSystem()
        resistors.add(c)
    }

    override fun canBeReplacedByInterSystem(): Boolean {
        return ofInterSystem
    }

    fun recalculateR() {
        var R = 0.0
        for (r in resistors) {
            R += r.r
        }
        r = R
    }

    internal fun restoreResistorIntoCircuit() {
        this.breakConnection()
    }

    internal fun removeResistorFromCircuit() {}

    override fun returnToRootSystem(root: RootSystem?) {
        for (r in resistors) {
            r.abstractedBy = null
        }

        for (s in states) {
            s.abstractedBy = null
        }

        restoreResistorIntoCircuit()

        root!!.nodes.addAll(states)
        root.components.addAll(resistors)

        root.removeProcess(this)
    }

    override fun simProcessFlush() {
        val i = (aPin!!.state - bPin!!.state) * rInv
        var u = aPin!!.state
        val ir = resistors.iterator()
        val `is` = states.iterator()

        while (`is`.hasNext()) {
            val s = `is`.next()
            val r = ir.next()
            u -= r.r * i
            s.state = u
        }
    }

    override fun addedTo(s: SubSystem) {
        s.addProcess(this)
        super.addedTo(s)
    }

    override fun quitSubSystem() {}

    override fun dirty(component: Component) {
        recalculateR()
        abstractedBy?.dirty(this)
    }

    companion object {
        fun newLine(root: RootSystem, resistors: LinkedList<Resistor>, states: LinkedList<Node>) {
            if (resistors.isEmpty()) {
            } else if (resistors.size == 1) {
            } else {
                val first = resistors.first
                val last = resistors.last
                val stateBefore = if (first.aPin === states.first) first.bPin else first.aPin
                val stateAfter = if (last.aPin === states.last) last.bPin else last.aPin

                val l = Line()
                l.resistors = resistors
                l.states = states
                l.recalculateR()
                root.components.removeAll(resistors)
                root.nodes.removeAll(states)
                root.components.add(l)
                l.connectTo(stateBefore, stateAfter)
                l.removeResistorFromCircuit()

                root.addProcess(l)

                for (r in resistors) {
                    r.abstractedBy = l
                    l.ofInterSystem = l.ofInterSystem or r.canBeReplacedByInterSystem()
                }

                for (s in states) {
                    s.abstractedBy = l
                }
            }
        }
    }
}
