package org.ja13.kmna.test

import net.eln.mna.SubSystem
import net.eln.mna.passive.Delay
import net.eln.mna.passive.Resistor
import net.eln.mna.passive.VoltageSource
import net.eln.mna.state.VoltageNode

class SubSystemTest {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val s = SubSystem(null, 0.1)
            val n1 = VoltageNode()
            val n2 = VoltageNode()
            val n3 = VoltageNode()
            val u1 = VoltageSource("")
            val r1 = Resistor()
            r1.r = 10.0
            r1.connectTo(n1, n2)
            val r2 = Resistor()
            r2.r = 10.0
            r2.connectTo(n3, null)
            val d1 = Delay()
            d1.set(1.0)
            d1.connectTo(n2, n3)

            s.addState(n1)
            s.addState(n2)
            s.addState(n3)

            u1.u = 1.0
            u1.connectTo(n1, null)
            s.addComponent(u1)

            s.addComponent(r1)
            s.addComponent(d1)
            s.addComponent(r2)

            //val p = Profiler()

            //p.add("run")

            // as it turns out, the first step where we build the matrix is what takes the longest time.
            s.step()

            //p.add("first")

            for (idx in 0..49) {
                s.step()
            }
            r1.r = 20.0
            for (idx in 0..49) {
                s.step()
            }
            //p.stop()

            //DP.println(DPType.CONSOLE, "$p ${p.list}")
            println("$s")

            //DP.println(DPType.CONSOLE, "first step finished in ${(p.list[1].nano - p.list.first.nano) / 1000}ps")
            //DP.println(DPType.CONSOLE, "other steps finished in ${(p.list.last.nano - p.list[1].nano) / 100 / 1000}ps")
        }
    }
}