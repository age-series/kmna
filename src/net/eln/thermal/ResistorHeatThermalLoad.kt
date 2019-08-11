package net.eln.thermal

import net.eln.common.IProcess
import net.eln.mna.passive.Resistor

class ResistorHeatThermalLoad(internal var r: Resistor, internal var load: ThermalLoad) : IProcess {

    override fun process(time: Double) {
        load.movePowerTo(r.getPower())
    }
}
