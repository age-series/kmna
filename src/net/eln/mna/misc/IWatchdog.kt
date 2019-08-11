package net.eln.mna.misc

import net.eln.common.IProcess

interface IWatchdog: IProcess {
    var min: Double
    var max: Double

    fun getValue(): Double
    fun reset()
}