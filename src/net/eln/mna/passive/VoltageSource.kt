package net.eln.mna.passive

import net.eln.mna.SubSystem
import net.eln.mna.misc.ISubSystemProcessI
import net.eln.mna.state.CurrentState
import net.eln.mna.state.State

open class VoltageSource : Bipole, ISubSystemProcessI {

    val currentState = CurrentState()

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

    constructor(aPin: State?, bPin: State?) : super(aPin, bPin)

    constructor(name: String, aPin: State?, bPin: State?) : super(aPin, bPin) {
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
}
