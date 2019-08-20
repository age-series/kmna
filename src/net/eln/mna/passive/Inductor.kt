package net.eln.mna.passive

import net.eln.mna.SubSystem
import net.eln.mna.misc.ISubSystemProcessI
import net.eln.mna.state.CurrentNode
import net.eln.mna.state.Node

open class Inductor : Bipole, ISubSystemProcessI {

    override val typeString: String
        get() = "L"

    var l = 0.0
        set(l) {
            field = l
            dirty()
        }
    internal var ldt: Double = 0.toDouble()

    val currentState = CurrentNode()

    override fun getCurrent() = currentState.state

    fun getE() = getCurrent() * getCurrent() * l / 2

    constructor(name: String) {
        this.name = name
    }

    constructor(aPin: Node?, bPin: Node?) : super(aPin, bPin)

    constructor(name: String, aPin: Node, bPin: Node) : super(aPin, bPin) {
        this.name = name
    }

    override fun applyTo(s: SubSystem) {
        ldt = -this.l / s.dt

        s.addToA(aPin, currentState, 1.0)
        s.addToA(bPin, currentState, -1.0)
        s.addToA(currentState, aPin, 1.0)
        s.addToA(currentState, bPin, -1.0)
        s.addToA(currentState, currentState, ldt)
    }

    override fun simProcessI(s: SubSystem) {
        s.addToI(currentState, ldt * currentState.state)
    }

    override fun quitSubSystem() {
        getSubSystem()!!.states.remove(currentState)
        getSubSystem()!!.removeProcess(this)
        super.quitSubSystem()
    }

    override fun addedTo(s: SubSystem) {
        super.addedTo(s)
        s.addState(currentState)
        s.addProcess(this)
    }

    fun resetStates() {
        currentState.state = 0.0
    }

    override fun exportProperties(): Pair<Map<String, String>, List<Node?>> {
        val s = super.exportProperties()
        val prop = s.first.toMutableMap()
        prop["L"] = l.toString()
        return Pair(prop, s.second)
    }

    override fun importProperties(data: Map<String, String>, pins: List<Node?>) {
        super.importProperties(data, pins)
        l = data["L"]?.toDouble()?: l
    }
}
