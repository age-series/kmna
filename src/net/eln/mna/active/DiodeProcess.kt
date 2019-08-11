package net.eln.mna.active

import net.eln.common.IProcess
import net.eln.mna.passive.ResistorSwitch

class DiodeProcess(internal var resistor: ResistorSwitch) : IProcess {
    override fun process(time: Double) {
        resistor.setState(resistor.getVoltage() > 0)
    }
}
