package net.eln.thermal

class ThermalLoad {

    var Tc: Double = 0.toDouble()
    var Rp: Double = 0.toDouble()
    var Rs: Double = 0.toDouble()
    var C: Double = 0.toDouble()
    var PcTemp: Double = 0.toDouble()
    var Pc: Double = 0.toDouble()
    var Prs: Double = 0.toDouble()
    var Psp: Double = 0.toDouble()
    var PrsTemp = 0.0
    var PspTemp = 0.0

    var isSlow: Boolean = false
        internal set

    val power: Double
        get() = if (java.lang.Double.isNaN(Prs) || java.lang.Double.isNaN(Pc) || java.lang.Double.isNaN(Tc) || java.lang.Double.isNaN(
                Rp
            ) || java.lang.Double.isNaN(Psp)
        ) 0.0 else (Prs + Math.abs(Pc) + Tc / Rp + Psp) / 2

    val t: Double
        get() = if (java.lang.Double.isNaN(Tc)) 0.0 else Tc

    constructor() {
        setHighImpedance()
        Tc = 0.0
        PcTemp = 0.0
        Pc = 0.0
        Prs = 0.0
        Psp = 0.0
    }

    constructor(Tc: Double, Rp: Double, Rs: Double, C: Double) {
        this.Tc = Tc
        this.Rp = Rp
        this.Rs = Rs
        this.C = C
        PcTemp = 0.0
    }

    fun setRsByTao(tao: Double) {
        Rs = tao / C
    }

    fun setHighImpedance() {
        Rs = 1000000000.0
        C = 1.0
        Rp = 1000000000.0
    }

    fun settRp(Rp: Double) {
        if (java.lang.Double.isNaN(Rp)) {
            ThermalConst.logger.error("TL.j sRp NaN!")
        }
        this.Rp = Rp
    }

    operator fun set(Rs: Double, Rp: Double, C: Double) {
        this.Rp = Rp
        this.Rs = Rs
        this.C = C
    }

    fun movePowerTo(power: Double) {
        if (java.lang.Double.isNaN(power)) {
            ThermalConst.logger.error("TL.j mpt NaN!")
            return
        }
        val absI = Math.abs(power)
        PcTemp += power
        PspTemp += absI
    }

    fun setAsSlow() {
        isSlow = true
    }

    fun setAsFast() {
        isSlow = false
    }

    companion object {

        val externalLoad = ThermalLoad(0.0, 0.0, 0.0, 0.0)

        fun moveEnergy(energy: Double, time: Double, from: ThermalLoad, to: ThermalLoad) {
            val I = energy / time
            val absI = Math.abs(I)
            from.PcTemp -= I
            to.PcTemp += I
            from.PspTemp += absI
            to.PspTemp += absI
        }

        fun movePower(power: Double, from: ThermalLoad, to: ThermalLoad) {
            val absI = Math.abs(power)
            from.PcTemp -= power
            to.PcTemp += power
            from.PspTemp += absI
            to.PspTemp += absI
        }

        /*
    fun checkThermalLoad(thermalRs: Double, thermalRp: Double, thermalC: Double): Boolean {
        if (thermalC < getMinimalThermalC(thermalRs, thermalRp)) {
            DP.println(DPType.MNA, "checkThermalLoad ERROR")
            Minecraft.getMinecraft().shutdown()
        }
        return true
    }

    fun getMinimalThermalC(Rs: Double, Rp: Double): Double {
        return thermalPeriod * 3 / (1 / (1 / Rp + 1 / Rs))
    }

    */

        fun getMinimalThermalC(Rs: Double, Rp: Double): Double {
            return 1 / ThermalConst.thermalFrequency * 3 / (1 / (1 / Rp + 1 / Rs))
        }

        fun checkThermalLoad(thermalRs: Double?, thermalRp: Double, thermalC: Double): Boolean {
            if (thermalC < getMinimalThermalC(thermalRs!!, thermalRp)) {
                ThermalConst.logger.error("checkThermalLoad ERROR")
                return false
            }
            return true
        }
    }
}
