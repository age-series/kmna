package net.eln.mna.misc

import org.apache.logging.log4j.LogManager

object MnaConst {
    const val ultraImpedance = 1e16
    const val highImpedance = 1e9
    const val pullDown = 1e9
    const val noImpedance = 0.042 // (1.68 * 10e-8) * (25.0 / 1000000.0 / 1.0) * 10_000_000_000 // I swear, it's not magic numbers!
    val logger = LogManager.getLogger("kMNA")
}
