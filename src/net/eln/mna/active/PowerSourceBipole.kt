package net.eln.mna.active

import net.eln.mna.passive.VoltageSource
import net.eln.mna.misc.IRootSystemPreStepProcess
import net.eln.mna.misc.MnaConst
import net.eln.mna.misc.Th
import net.eln.mna.state.State

class PowerSourceBipole(private val aPin: State, private val bPin: State, private val aSrc: VoltageSource, private val bSrc: VoltageSource) : IRootSystemPreStepProcess {

    var p: Double = 0.toDouble()
    internal var Umax: Double = 0.toDouble()
    internal var Imax: Double = 0.toDouble()

    internal fun setMax(Umax: Double, Imax: Double) {
        this.Umax = Umax
        this.Imax = Imax
    }

    fun setImax(imax: Double) {
        Imax = imax
    }

    fun setUmax(umax: Double) {
        Umax = umax
    }

    override fun rootSystemPreStepProcess() {
        val a = Th.getTh(aPin, aSrc)
        val b = Th.getTh(bPin, bSrc)

        if (a.U.isNaN()) {
            a.U = 0.0
            a.R = MnaConst.highImpedance
        }
        if (b.U.isNaN()) {
            b.U = 0.0
            b.R = MnaConst.highImpedance
        }

        val Uth = a.U - b.U
        val Rth = a.R + b.R
        if (Uth >= Umax) {
            aSrc.u = a.U
            bSrc.u = b.U
        } else {
            var U = (Math.sqrt(Uth * Uth + 4.0 * p * Rth) + Uth) / 2
            U = Math.min(Math.min(U, Umax), Uth + Rth * Imax)
            if (java.lang.Double.isNaN(U)) U = 0.0

            val I = (Uth - U) / Rth
            aSrc.u = a.U - I * a.R
            bSrc.u = b.U + I * b.R
        }
    }
}
