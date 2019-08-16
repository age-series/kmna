package org.ja13.kmna.test

import net.eln.common.PrintValue
import net.eln.mna.SubSystem
import net.eln.mna.passive.*
import net.eln.mna.state.CurrentState
import net.eln.mna.state.VoltageState

class CapacitorTest {
    companion object {
        @JvmStatic
        fun main(argv: Array<String>) {
            val ss = SubSystem(null, 0.05)

            val vsv = VoltageSource("voltage")
            vsv.u = 200.0
            val vsg = VoltageSource("ground")
            vsg.u = 0.0

            val rsPwr = ResistorSwitch("Power Switch")
            rsPwr.setState(false)
            val rsLoop = ResistorSwitch("Loop Switch")
            rsLoop.setState(false)

            val r1 = Resistor()
            r1.r = 100.0

            val c1 = Capacitor("Capacitor")
            c1.c = 200 * 1e-6

            val s0 = VoltageState()
            val s1 = VoltageState()
            val s2 = VoltageState()
            val s3 = VoltageState()

            vsv.aPin = s0
            vsg.aPin = s2

            rsPwr.aPin = s0
            rsPwr.bPin = s1
            rsLoop.aPin = s1
            rsLoop.bPin = s2

            c1.aPin = s1
            c1.bPin = s3

            r1.aPin = s3
            r1.bPin = s2

            ss.addState(s0)
            ss.addState(s1)
            ss.addState(s2)
            ss.addState(s3)

            ss.addComponent(vsv)
            ss.addComponent(vsg)
            ss.addComponent(rsPwr)
            ss.addComponent(rsLoop)
            ss.addComponent(r1)
            ss.addComponent(c1)

            fun printStates() {
                println((1..16).joinToString("") { "=" })
                ss.states.forEach {
                    val un = if (it is CurrentState) {
                        "A"
                    } else {
                        "V"
                    }
                    println("${it.id}: %.4f $un".format(it.state))
                }
            }

            fun doStep(count: Int) {
                for (x in (1 .. count))
                    ss.step()
                printStates()
            }

            doStep(1)
            rsPwr.setState(true)
            rsLoop.setState(false)
            println("power applied")
            println(PrintValue.plotEnergy(c1.getE()))
            doStep(1)
            println(PrintValue.plotEnergy(c1.getE()))
            doStep(1)
            println(PrintValue.plotEnergy(c1.getE()))
            doStep(1)
            rsPwr.setState(false)
            rsLoop.setState(true)
            println("power removed")
            println(PrintValue.plotEnergy(c1.getE()))
            doStep(1)
            println(PrintValue.plotEnergy(c1.getE()))
            doStep(1)
            println(PrintValue.plotEnergy(c1.getE()))
            doStep(1)
            println(PrintValue.plotEnergy(c1.getE()))

            println(ss.dotGraph())

            println("c1: ${PrintValue.plotValue(c1.c, "F")}")
            println("r1: ${PrintValue.plotOhm(r1.r)}")
            println("voltage: ${PrintValue.plotVolt(vsv.u)}")
            println("step speed: ${ss.dt}s")
        }
    }
}