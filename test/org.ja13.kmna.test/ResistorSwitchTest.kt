package org.ja13.kmna.test

import net.eln.mna.SubSystem
import net.eln.mna.passive.ResistorSwitch
import net.eln.mna.passive.VoltageSource
import net.eln.mna.state.CurrentNode
import net.eln.mna.state.VoltageNode
import kotlin.math.absoluteValue

class ResistorSwitchTest {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val ss = SubSystem(null, 0.05)

            val vs5v = VoltageSource("5v")
            vs5v.u = 5.0
            val vs0v = VoltageSource("0v")
            vs0v.u = 0.0

            val rs = ResistorSwitch("Switch")
            rs.r = 1e-2

            val s0 = VoltageNode()
            val s1 = VoltageNode()

            vs5v.aPin = s0
            rs.aPin = s0
            rs.bPin = s1
            vs0v.aPin = s1

            ss.addState(s0)
            ss.addState(s1)

            ss.addComponent(vs5v)
            ss.addComponent(vs0v)
            ss.addComponent(rs)

            fun printStates() {
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

            fun doStep(count: Int) {
                for (x in (1 .. count))
                    ss.step()
                printStates()
            }

            var allPass = true
            var state = Math.random() > 0.5
            rs.setState(state)
            doStep(2)
            for (x in (0 .. 100)) {
                // for reference, the amps passed should be around 500A. We'll check for over 450A.
                var pass = true
                ss.states.filterIsInstance<CurrentNode>().forEach {
                    if (it.state.absoluteValue < 450.0 == state) {
                        pass = false
                        allPass = false
                    }
                }
                println("$state: ${ss.states.filterIsInstance<CurrentNode>().map { it.state.toString() }}")
                println(rs.r)
                state = Math.random() > 0.5
                rs.setState(state)
                println(if (pass) "PASS" else "FAIL")
                doStep(1)
            }

            print(ss.exportCircuit())

            if (allPass) {
                println("TEST PASS")
            } else {
                println("TEST FAIL")
            }
        }
    }
}