package net.eln.thermal

import net.eln.util.ValueWatchdog

class ThermalLoadWatchDog : ValueWatchdog() {

    internal var state: ThermalLoad? = null

    override fun getValue(): Double? = state?.t

    fun set(state: ThermalLoad): ThermalLoadWatchDog {
        this.state = state
        return this
    }

    fun setTMax(tMax: Double): ThermalLoadWatchDog {
        this.max = tMax
        this.min = -40.0
        this.timeoutReset = tMax * 0.1 * 10.0
        return this
    }

    fun setInitializer(t: ThermalLoadInitializer): ThermalLoadWatchDog {
        this.max = t.warmLimit
        this.min = t.coolLimit
        this.timeoutReset = max * 0.1 * 10.0
        return this
    }

    fun setLimit(thermalWarmLimit: Double, thermalCoolLimit: Double): ThermalLoadWatchDog {
        this.max = thermalWarmLimit
        this.min = thermalCoolLimit
        this.timeoutReset = max * 0.1 * 10.0
        return this
    }

    fun setLimit(t: ThermalLoadInitializerByPowerDrop): ThermalLoadWatchDog {
        this.max = t.warmLimit
        this.min = t.coolLimit
        this.timeoutReset = max * 0.1 * 10.0
        return this
    }
}
