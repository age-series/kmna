package net.eln.mna.passive

import net.eln.mna.misc.MnaConst
import net.eln.mna.state.Node

open class ResistorSwitch : Resistor {

    override val typeString: String
        get() = "RS"

    constructor(name: String) : super(name)

    constructor(name: String, aPin: Node?, bPin: Node?) : super(aPin, bPin) {
        this.name = name
    }

    private var ultraImpedance = false

    internal var state = false

    override var r: Double
        set(r) {
            closedR = r
        }
        get() {
            return if (state) closedR else openR
        }

    override var rInv: Double
        get() = 1.0 / r
        set(value) {}

    private var closedR = 1.0
    private var openR = if (ultraImpedance) MnaConst.ultraImpedance else MnaConst.highImpedance

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

    override fun exportProperties(): Pair<Map<String, String>, List<Node?>> {
        val s = super.exportProperties()
        val prop = s.first.toMutableMap()
        prop.remove("R")
        prop["OPEN_R"] = openR.toString()
        prop["CLOSED_R"] = closedR.toString()
        prop["STATE"] = state.toString()
        return Pair(prop, s.second)
    }

    override fun importProperties(data: Map<String, String>, pins: List<Node?>) {
        super.importProperties(data, pins)
        openR = data["OPEN_R"]?.toDouble()?: openR
        closedR = data["CLOSED_R"]?.toDouble()?: closedR
        setState(data["STATE"]?.toBoolean()?: state)
    }
}
