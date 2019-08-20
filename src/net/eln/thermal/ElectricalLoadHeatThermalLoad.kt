package net.eln.thermal

import net.eln.common.IProcess
import net.eln.mna.state.ElectricalLoad

class ElectricalLoadHeatThermalLoad(internal var r: ElectricalLoad, internal var load: ThermalLoad) : IProcess {

    override fun process(dt: Double) {
        if (r.isNotSimulated()) return
        val I = r.i
        load.movePowerTo(I * I * r.rs * 2.0)
    }
}
