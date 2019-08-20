package net.eln.mna.passive

import net.eln.mna.SubSystem
import net.eln.mna.misc.ISubSystemProcessI
import net.eln.mna.state.Node

open class Capacitor : Bipole, ISubSystemProcessI {

    override val typeString: String
        get() = "C"

    var c = 0.0
        set(c) {
            field = c
            dirty()
        }
    internal var cdt: Double = 0.toDouble()

    override fun getCurrent(): Double {
        return 0.0
    }

    fun getE() = getVoltage() * getVoltage() * this.c / 2

    constructor() {}

    constructor(name: String) {
        this.name = name
    }

    constructor(aPin: Node?, bPin: Node?) {
        this.name = "Capacitor"
        connectTo(aPin, bPin)
    }

    constructor(name: String, aPin: Node?, bPin: Node?) {
        this.name = name
        connectTo(aPin, bPin)
    }


    override fun applyTo(s: SubSystem) {
        cdt = this.c / s.dt

        s.addToA(aPin, aPin, cdt)
        s.addToA(aPin, bPin, -cdt)
        s.addToA(bPin, bPin, cdt)
        s.addToA(bPin, aPin, -cdt)
    }

    override fun simProcessI(s: SubSystem) {
        val add = (s.getXSafe(aPin) - s.getXSafe(bPin)) * cdt
        s.addToI(aPin, add)
        s.addToI(bPin, -add)
    }

    override fun quitSubSystem() {
        getSubSystem()!!.removeProcess(this)
        super.quitSubSystem()
    }

    override fun addedTo(s: SubSystem) {
        super.addedTo(s)
        s.addProcess(this)
    }

    override fun exportProperties(): Pair<Map<String, String>, List<Node?>> {
        val s = super.exportProperties()
        val prop = s.first.toMutableMap()
        prop["F"] = c.toString()
        return Pair(prop, s.second)
    }

    override fun importProperties(data: Map<String, String>, pins: List<Node?>) {
        super.importProperties(data, pins)
        c = data["F"]?.toDouble()?: c
    }
}
