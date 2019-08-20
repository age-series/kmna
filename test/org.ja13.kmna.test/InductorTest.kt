package org.ja13.kmna.test

import net.eln.mna.SubSystem
import net.eln.mna.passive.Inductor
import net.eln.mna.passive.Resistor
import net.eln.mna.passive.ResistorSwitch
import net.eln.mna.passive.VoltageSource
import net.eln.mna.state.CurrentNode
import net.eln.mna.state.VoltageNode

class InductorTest {
    companion object {
        @JvmStatic
        fun main(argv: Array<String>) {
            val ss = SubSystem(null, 0.05)

            val vs5v = VoltageSource("Five Volts")
            vs5v.u = 5.0
            val vs0v = VoltageSource("Ground")
            vs0v.u = 0.0

            val rsPwr = ResistorSwitch("Power Switch")
            rsPwr.setState(false)
            val rsLoop = ResistorSwitch("Loop Switch")
            rsLoop.setState(false)

            val r1 = Resistor()
            r1.r = 140.0
            val r2 = Resistor()
            r2.r = 140.0

            val l1 = Inductor("inductor")
            l1.l = 3.0

            /*

This is a 3x2 circuit, as follows:

(0,0) ResistorSwitch (1,0) ResistorSwitch (2)
(0,1) Resistor 140Ω (1,1) Resistor 140Ω (2)
(1,0) inductor 3H (1,1)
(0,0) VoltageSource 5v
(0,1) VoltageSource 0v

             */
            val s00 = VoltageNode()
            val s10 = VoltageNode()
            val s2 = VoltageNode()
            val s01 = VoltageNode()
            val s11 = VoltageNode()

            vs5v.aPin = s00
            vs0v.aPin = s10

            rsPwr.aPin = s00
            rsPwr.bPin = s10
            rsLoop.aPin = s10
            rsLoop.bPin = s2

            r1.aPin = s01
            r1.bPin = s11
            r2.aPin = s11
            r2.bPin = s2

            l1.aPin = s10
            l1.bPin = s11

            ss.addState(s00)
            ss.addState(s10)
            ss.addState(s2)
            ss.addState(s01)
            ss.addState(s11)

            ss.addComponent(vs5v)
            ss.addComponent(vs0v)
            ss.addComponent(rsPwr)
            ss.addComponent(rsLoop)
            ss.addComponent(r1)
            ss.addComponent(r2)
            ss.addComponent(l1)

            fun doStep(count: Int) {
                for (x in (1 .. count))
                    ss.step()
                println((1 .. 16).map {"="}.joinToString (""))
                ss.states.forEach {
                    val un: String
                    if (it is CurrentNode) {
                        un = "A"
                    } else {
                        un = "V"
                    }
                    println("${it.id}: %.4f $un".format(it.state))
                }
            }

            doStep(1)
            rsPwr.setState(true)
            println(rsPwr.r)
            doStep(1)
            doStep(1)
            doStep(1)
            rsPwr.setState(false)
            rsLoop.setState(true)
            println(rsPwr.r)
            println(rsLoop.r)
            doStep(1)
            doStep(1)
            doStep(1)

            println(ss)
        }
    }
}