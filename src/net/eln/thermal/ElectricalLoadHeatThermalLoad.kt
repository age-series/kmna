package mods.eln.sim.thermal

import net.eln.common.IProcess
import net.eln.mna.state.ElectricalLoad
import net.eln.thermal.ThermalLoad

class ElectricalLoadHeatThermalLoad(internal var r: ElectricalLoad, internal var load: ThermalLoad) : IProcess {

    override fun process(time: Double) {
        if (r.isNotSimulated()) return
        val I = r.i
        load.movePowerTo(I * I * r.rs * 2.0)
    }
}
