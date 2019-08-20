package net.eln.mna.passive

import net.eln.mna.state.Node

abstract class Bipole : Component {
    var aPin: Node? = null
    var bPin: Node? = null

    override fun getConnectedStates() = arrayOf<Node?>(aPin, bPin)

    abstract fun getCurrent(): Double

    fun getVoltage(): Double {
        return (aPin?.state ?: 0.0) - (bPin?.state ?: 0.0)
    }

    fun getBipoleU() = getVoltage()

    constructor() {}

    constructor(name: String) {
        this.name = name
    }

    constructor(aPin: Node?, bPin: Node?) {
        this.name = "Bipole"
        connectTo(aPin, bPin)
    }

    constructor(name:String, aPin: Node?, bPin: Node?) {
        this.name = name
        connectTo(aPin, bPin)
    }

    fun connectTo(aPin: Node?, bPin: Node?): Bipole {
        breakConnection()

        this.aPin = aPin
        this.bPin = bPin

        aPin?.add(this)
        bPin?.add(this)
        return this
    }

    fun connectGhostTo(aPin: Node, bPin: Node): Bipole {
        breakConnection()

        this.aPin = aPin
        this.bPin = bPin
        return this
    }

    override fun breakConnection() {
        aPin?.remove(this)
        bPin?.remove(this)
    }

    override fun toString(): String {
        return "[" + aPin + " " + this.javaClass.simpleName + "_" + name + " " + bPin + "]"
    }

    override fun exportProperties(): Pair<Map<String, String>, List<Node?>> {
        val prop = super.exportProperties().first
        val pins = listOf(aPin, bPin)
        return Pair(prop, pins)
    }

    override fun importProperties(data: Map<String, String>, pins: List<Node?>) {
        super.importProperties(data, pins)
        if (pins.size == 2) {
            aPin = pins[0]
            bPin = pins[1]
        }
    }
}
