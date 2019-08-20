package net.eln.mna.passive

import net.eln.mna.SubSystem
import net.eln.mna.misc.MnaConst
import net.eln.mna.state.Node

open class Resistor : Bipole {

    override val typeString: String
        get() = "R"

    constructor()
    constructor(name: String) : super(name)
    constructor(aPin: Node?, bPin: Node?) : super(aPin, bPin)

    open var r: Double = MnaConst.highImpedance
        get() {return field}
        set(r) {
            if (field != r) {
                field = r
                rInv = 1 / r
                dirty()
            }
        }
    open var rInv: Double = 1.0 / MnaConst.highImpedance


    override fun getCurrent() = getVoltage() * rInv
    fun getPower() = getVoltage() * getCurrent()


    fun highImpedance() {
        r = MnaConst.highImpedance
    }

    fun ultraImpedance() {
        r = MnaConst.ultraImpedance
    }

    fun pullDown() {
        r = MnaConst.pullDown
    }

    override fun applyTo(s: SubSystem) {
        s.addToA(aPin, aPin, rInv)
        s.addToA(aPin, bPin, -rInv)
        s.addToA(bPin, bPin, rInv)
        s.addToA(bPin, aPin, -rInv)
    }

    override fun exportProperties(): Pair<Map<String, String>, List<Node?>> {
        val s = super.exportProperties()
        val prop = s.first.toMutableMap()
        prop["R"] = r.toString()
        return Pair(prop, s.second)
    }

    override fun importProperties(data: Map<String, String>, pins: List<Node?>) {
        super.importProperties(data, pins)
        r = data["R"]?.toDouble()?: MnaConst.highImpedance
    }
}
