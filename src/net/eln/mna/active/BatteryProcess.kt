package net.eln.mna.active

import net.eln.util.FunctionTable
import net.eln.thermal.ThermalLoad
import net.eln.common.IProcess
import net.eln.mna.misc.MnaConst
import net.eln.mna.passive.VoltageSource
import net.eln.mna.state.VoltageState

open class BatteryProcess(
    val positiveLoad: VoltageState,
    val negativeLoad: VoltageState,
    var voltageFunction: FunctionTable,
    var iMax: Double,
    var batteryAging: Boolean,
    val voltageSource: VoltageSource,
    private val thermalLoad: ThermalLoad
) : IProcess {

    var Q = 0.0
    var QNominal = 0.0
    var uNominal = 0.0
    fun uMax() = 1.3 * uNominal
    var life = 1.0

    var isRechargable: Boolean = true

    fun getCharge() = Q / life

    fun setCharge(charge: Double) {
        Q = life * charge
    }

    fun getEnergy(): Double {
        val steps = 50
        val chargeStep: Double = getCharge() / steps
        var chargeIntegrator = 0.0
        var energy = 0.0
        val qPerStep: Double = QNominal * life * chargeStep

        for (i in (0 .. steps)) {
            val voltage = voltageFunction.getValue(chargeIntegrator) * uNominal
            energy += voltage * qPerStep
            chargeIntegrator += chargeStep
        }
        return energy
    }

    fun getEnergyMax(): Double {
        val steps = 50
        val chargeStep: Double = 1.0 / steps
        var chargeIntegrator = 0.0
        var energy = 0.0
        val qPerStep: Double = QNominal * life * 1.0 / steps

        for (i in (0 .. steps)) {
            val voltage = voltageFunction.getValue(chargeIntegrator) * uNominal
            energy += voltage * qPerStep
            chargeIntegrator += chargeStep
        }
        return energy
    }

    fun computeVoltage(): Double {
        return Math.max(0.0, voltageFunction.getValue(Q / life) * uNominal)
    }

    fun getDischargeCurrent(): Double = voltageSource.getCurrent()

    fun getU(): Double = computeVoltage()

    override fun process(time: Double) {
        val lastQ = Q
        var wasteQ = 0.0
        Q = Math.max(Q - voltageSource.getCurrent() * time / QNominal, 0.0)
        if (Q > lastQ && !isRechargable) {
            MnaConst.logger.warn("Battery is recharging when it shouldn't!")
            wasteQ = Q - lastQ
            Q = lastQ
        }
        voltageSource.u = computeVoltage()
        if (wasteQ > 0) {
            thermalLoad.movePowerTo(Math.abs(voltageSource.getPower()))
        }
    }
}

open class BatterySlowProcess(val batteryProcess: BatteryProcess): IProcess {

    var lifeNominalCurrent: Double = 0.0
    var lifeNominalLost: Double = 0.0

    override fun process(time: Double) {
        val U = batteryProcess.computeVoltage()
        if (U > batteryProcess.uMax()) {
            destroy()
            return
        }
        if (batteryProcess.batteryAging) {
            val normalizedI = Math.abs(batteryProcess.voltageSource.getCurrent()) / lifeNominalCurrent
            batteryProcess.life -= normalizedI * normalizedI * lifeNominalLost * time
            if (batteryProcess.life < 0.1) batteryProcess.life = 0.1
        }
    }

    open fun destroy() {
        MnaConst.logger.info("Battery should explode.")
    }
}

