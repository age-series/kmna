package org.ja13.kmna.test

import net.eln.mna.passive.Capacitor
import net.eln.mna.passive.ResistorSwitch


class KmnaParseTester {
    companion object {

        val example = """
            graph subsystem0 {
                null [label=null];
                0 [label=Node];
                1 [label=VoltageState volts=8];
                2 [label=Node];
                0 -- null [label=VoltageSource volts=10];
                0 -- 1 [label=Resistor ohms=10];
                1 -- 2 [label=Resistor ohms=10];
                2 -- null [label=VoltageSource volts=0];
            }
        """.trimIndent()

        val example2 = """
            ${'$'} 1 0.000005 10.20027730826997 50 5 43
            r 176 80 384 80 0 10
            s 384 80 448 80 0 1 false
            w 176 80 176 352 0
            c 384 352 176 352 0 0.000015 -3.012969337832106
            l 384 80 384 352 0 1 0.04064595520192915
            v 448 352 448 80 0 0 40 5 0 0 0.5
            r 384 352 448 352 0 100
            o 4 64 0 4099 20 0.05 0 2 4 3
            o 3 64 0 4099 20 0.05 1 2 3 3
            o 0 64 0 4099 0.625 0.05 2 2 0 3
            38 3 0 0.000001 0.000101 Capacitance
            38 4 0 0.01 1.01 Inductance
            38 0 0 1 101 Resistance
            h 1 4 3
        """.trimIndent()

        const val dt = 0.5

        @JvmStatic
        fun main(args: Array<String>) {
            println("=====\n" + example2 + "\n=====\n")
            val dot = FalstadHandler.parseFalstad(example2)
            println(dot)
            val root = DotHandler.parseDot(dot)
            println("Number of systems: ${root.subSystemCount}")
            root.systems.forEach {
                println("Subsystem: ${it}")
            }

            val resistorSwitches = root.systems[0].component.filterIsInstance<ResistorSwitch>()
            val capacitors = root.systems[0].component.filterIsInstance<Capacitor>()

            var runs = 0
            val timesToRun = 10

            while (timesToRun > runs) {

                if (runs < 5) {
                    resistorSwitches.forEach { it.setState(true) }
                    resistorSwitches.forEach { println(it.r) }
                } else {
                    resistorSwitches.forEach { it.setState(false) }
                    resistorSwitches.forEach { println(it.r) }
                }
                capacitors.forEach { println(it.getE()) }

                root.step()
                runs++

                println("Time: $runs (time elapsed: ${runs * dt})")
                root.systems.forEach {
                    it.states.forEach {
                        println("${it.id}: ${it.state}")
                    }
                }
            }

            root.systems.forEach {
                println("Subsystem: ${it}")
            }
        }
    }
}