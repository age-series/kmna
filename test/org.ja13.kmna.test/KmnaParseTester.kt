package org.ja13.kmna.test

import net.eln.mna.passive.Capacitor
import net.eln.mna.passive.ResistorSwitch
import net.eln.mna.state.CurrentState


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
            ${'$'} 1 0.000005 10.20027730826997 50 5 50
            r 256 176 256 304 0 100
            172 304 176 304 128 0 7 5 5 0 0 0.5 Voltage
            g 256 336 256 352 0
            w 256 304 256 336 1
            r 352 176 352 304 0 1000
            w 352 304 352 336 1
            g 352 336 352 352 0
            w 304 176 352 176 0
            w 256 176 304 176 0
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

            var runs = 0
            val timesToRun = 3

            while (timesToRun > runs) {
                root.step()
                runs++

                println("Time: $runs (time elapsed: ${runs * dt})")
                root.systems.forEach {
                    it.states.forEach {
                        val un: String
                        if(it is CurrentState) {
                            un = "A"
                        } else {
                            un = "V"
                        }
                        println("${it.id}: %.4f $un".format(it.state))
                    }
                }
            }

            root.systems.forEach {
                println("Subsystem: ${it}")
            }
        }
    }
}