package net.eln.mna.passive

import net.eln.mna.SubSystem
import net.eln.mna.misc.ISubSystemProcessI
import net.eln.mna.misc.MnaConst
import net.eln.mna.state.CurrentNode
import net.eln.mna.state.Node

open class VoltageSource : Bipole, ISubSystemProcessI {

    override val typeString: String
        get() = "VS"

    val currentState = CurrentNode()

    var u: Double = 0.0

    override fun getCurrent(): Double {
        return -currentState.state
    }

    open fun getPower(): Double {
        return getVoltage() * getCurrent()
    }

    constructor(name: String) {
        this.name = name
    }

    constructor(aPin: Node?, bPin: Node?) : super(aPin, bPin)

    constructor(name: String, aPin: Node?, bPin: Node?) : super(aPin, bPin) {
        this.name = name
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

    override fun applyTo(s: SubSystem) {
        s.addToA(aPin, currentState, 1.0)
        s.addToA(bPin, currentState, -1.0)
        s.addToA(currentState, aPin, 1.0)
        s.addToA(currentState, bPin, -1.0)
    }

    override fun simProcessI(s: SubSystem) {
        s.addToI(currentState, u)
    }

    override fun exportProperties(): Pair<Map<String, String>, List<Node?>> {
        val s = super.exportProperties()
        val prop = s.first.toMutableMap()
        prop["U"] = u.toString()
        return Pair(prop, s.second)
    }

    override fun importProperties(data: Map<String, String>, pins: List<Node?>) {
        super.importProperties(data, pins)
        u = data["U"]?.toDouble()?: u
    }
}
