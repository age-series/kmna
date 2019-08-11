package net.eln.mna.passive

import net.eln.mna.SubSystem
import net.eln.mna.misc.ISubSystemProcessI
import net.eln.mna.state.CurrentState
import net.eln.mna.state.State

open class Inductor : Bipole, ISubSystemProcessI {

    var l = 0.0
        set(l) {
            field = l
            dirty()
        }
    internal var ldt: Double = 0.toDouble()

    val currentState = CurrentState()

    override fun getCurrent() = currentState.state

    fun getE() = getCurrent() * getCurrent() * l / 2

    constructor(name: String) {
        this.name = name
    }

    constructor(aPin: State?, bPin: State?) : super(aPin, bPin)

    constructor(name: String, aPin: State, bPin: State) : super(aPin, bPin) {
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
}
