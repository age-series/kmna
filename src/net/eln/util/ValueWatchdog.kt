package net.eln.util

import net.eln.common.IProcess
import org.apache.logging.log4j.LogManager

abstract class ValueWatchdog : IProcess {

    internal var destructable: IDestruct? = null

    var min: Double = 0.toDouble()
    var max: Double = 0.toDouble()

    var timeoutReset = 2.0

    internal var timeout = 0.0
    internal var boot = true
    internal var joker = true

    internal var rand = Math.random() + 0.5

    abstract fun getValue(): Double?

    override fun process(time: Double) {
        if (boot) {
            boot = false
            timeout = timeoutReset
        }
        val value = getValue() ?: 0.0
        var overflow = Math.max(value - max, min - value)
        var rawOverflow = overflow
        if (overflow > 0) {
            if (joker) {
                joker = false
                overflow = 0.0
            }
        } else {
            joker = true
        }

        timeout -= time * overflow * rand
        if (timeout > timeoutReset) {
            timeout = timeoutReset
        }
        if (timeout < 0) {
            LogManager.getLogger("kMNA-Util").info("${javaClass.name} destroying ${destructable?.describe()} for being at value $value when max, min is $max, $min. Also, timeout: $timeout and overflow $rawOverflow")
            destructable?.explode()
        }
    }

    fun disable() {
        this.max = 100000000.0
        this.min = -max
        this.timeoutReset = 10000000.0
    }

    fun reset() {
        boot = true
    }

    fun set(destructable: IDestruct) {
        this.destructable = destructable
    }
}
