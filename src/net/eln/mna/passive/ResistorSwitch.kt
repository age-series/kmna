package net.eln.mna.passive

import net.eln.mna.misc.MnaConst
import net.eln.mna.state.State

open class ResistorSwitch : Resistor {

    constructor(name: String) : super(name)

    constructor(name: String, aPin: State?, bPin: State?) : super(aPin, bPin) {
        this.name = name
    }

    internal var ultraImpedance = false

    internal var state = false

    override var r: Double
        set(r) {
            baseR = r
        }
        get() {
            return if (state) baseR else if (ultraImpedance) MnaConst.ultraImpedance else MnaConst.highImpedance
        }

    override var rInv: Double
        get() = 1.0 / r
        set(value) {}

    protected var baseR = 1.0

    fun setState(state: Boolean) {
        val oldState = this.state
        this.state = state
        if (oldState != state) dirty()
    }

    fun getState(): Boolean {
        return state
    }

    fun mustUseUltraImpedance() {
        ultraImpedance = true
    }

    init {
        connectTo(aPin, bPin)
    }
}
