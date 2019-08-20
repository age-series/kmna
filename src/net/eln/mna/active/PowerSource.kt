package net.eln.mna.active

import net.eln.mna.SubSystem
import net.eln.mna.passive.VoltageSource
import net.eln.mna.misc.IRootSystemPreStepProcess
import net.eln.mna.misc.Th
import net.eln.mna.state.Node
open class PowerSource(name: String, aPin: Node) : VoltageSource(name, aPin, null), IRootSystemPreStepProcess {

    var p: Double = 0.0
    var Umax: Double = 0.0
    var Imax: Double = 0.0

    fun getEffectiveP() = getBipoleU() * getCurrent()

    override fun getPower(): Double {
        return p
    }

    override fun quitSubSystem() {
        getSubSystem()!!.root?.removeProcess(this)
        super.quitSubSystem()
    }

    override fun addedTo(s: SubSystem) {
        super.addedTo(s)
        getSubSystem()!!.root?.addProcess(this)
        s.addProcess(this)
    }

    override fun rootSystemPreStepProcess() {
        val t = Th.getTh(aPin!!, this)

        var U = (Math.sqrt(t.U * t.U + 4.0 * p * t.R) + t.U) / 2
        U = Math.min(Math.min(U, Umax), t.U + t.R * Imax)
        if (java.lang.Double.isNaN(U)) U = 0.0
        if (U < t.U) U = t.U

        u = U
    }
}
