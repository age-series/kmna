package net.eln.thermal

import net.eln.common.IProcess
import net.eln.mna.passive.Resistor

class ElectricalResistorHeatThermalLoad(internal var electricalResistor: Resistor, internal var thermalLoad: ThermalLoad) :
    IProcess {

    override fun process(time: Double) {
        thermalLoad.PcTemp += electricalResistor.getPower()
    }
}
