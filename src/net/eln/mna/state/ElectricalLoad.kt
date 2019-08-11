package net.eln.mna.state

import net.eln.mna.misc.ElectricalConnection
import net.eln.mna.passive.Bipole
import net.eln.mna.passive.Line
import net.eln.mna.misc.MnaConst

open class ElectricalLoad : VoltageStateLineReady() {

    var rs = MnaConst.highImpedance
        set(Rs) {
            if (this.rs != Rs) {
                field = Rs
                for (c in connectedComponents) {
                    if (c is ElectricalConnection) {
                        c.notifyRsChange()
                    }
                }
            }
        }

    val i: Double
        get() {
            var i = 0.0
            for (c in connectedComponents) {
                if (c is Bipole && c !is Line)
                    i += Math.abs(c.getCurrent())
            }
            return i * 0.5
        }

    val current: Double
        get() = i

    fun highImpedance() {
        rs = MnaConst.highImpedance
    }
}
