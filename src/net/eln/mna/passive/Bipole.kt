package net.eln.mna.passive

import net.eln.common.IDotLine
import net.eln.mna.state.State

abstract class Bipole : Component, IDotLine {
    var aPin: State? = null
    var bPin: State? = null

    override fun getConnectedStates() = arrayOf<State?>(aPin, bPin)

    abstract fun getCurrent(): Double

    fun getVoltage(): Double {
        return (aPin?.state ?: 0.0) - (bPin?.state ?: 0.0)
    }

    fun getBipoleU() = getVoltage()

    constructor() {}

    constructor(name: String) {
        this.name = name
    }

    constructor(aPin: State?, bPin: State?) {
        this.name = "Bipole"
        connectTo(aPin, bPin)
    }

    constructor(name:String, aPin: State?, bPin: State?) {
        this.name = name
        connectTo(aPin, bPin)
    }

    fun connectTo(aPin: State?, bPin: State?): Bipole {
        breakConnection()

        this.aPin = aPin
        this.bPin = bPin

        aPin?.add(this)
        bPin?.add(this)
        return this
    }

    fun connectGhostTo(aPin: State, bPin: State): Bipole {
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

    override fun dotLine(): String {
        return "${aPin?.dotNodeID()?: "null"} -- ${bPin?.dotNodeID()?: "null"} [label=${this.javaClass.simpleName}_${name.replace(" ","_")}]"
    }
}
