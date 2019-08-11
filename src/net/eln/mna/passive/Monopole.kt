package net.eln.mna.passive

import net.eln.mna.state.State
import net.eln.mna.state.VoltageState

abstract class Monopole : Component() {

    internal var pin: VoltageState? = null

    override fun getConnectedStates() = arrayOf<State?>(pin)

    fun connectTo(pin: VoltageState?): Monopole {
        this.pin = pin
        pin?.add(this)
        return this
    }
}
