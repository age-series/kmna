package net.eln.mna.passive

import net.eln.mna.state.Node
import net.eln.mna.state.VoltageNode

abstract class Monopole : Component() {

    internal var pin: VoltageNode? = null

    override fun getConnectedStates() = arrayOf<Node?>(pin)

    fun connectTo(pin: VoltageNode?): Monopole {
        this.pin = pin
        pin?.add(this)
        return this
    }

    override fun exportProperties(): Pair<Map<String, String>, List<Node?>> {
        val prop = super.exportProperties().first
        val pins = listOf(pin)
        return Pair(prop, pins)
    }

    override fun importProperties(data: Map<String, String>, pins: List<Node?>) {
        super.importProperties(data, pins)
        if (pins.size == 1)
            pin = pins[0] as VoltageNode
    }
}
