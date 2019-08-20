package net.eln.thermal

import net.eln.common.IProcess
import net.eln.thermal.ThermalLoad
import net.eln.mna.passive.Resistor

class DiodeHeatThermalLoad(internal var r: Resistor, internal var load: ThermalLoad) : IProcess {
    internal var lastR: Double = 0.toDouble()

    init {
        lastR = r.r
    }

    override fun process(dt: Double) {
        if (r.r == lastR) {
            load.movePowerTo(r.getPower())
        } else {
            lastR = r.r
        }
    }
}
