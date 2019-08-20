package net.eln.mna

import net.eln.mna.state.ElectricalLoad
import net.eln.mna.misc.IRootSystemPreStepProcess
import net.eln.mna.misc.ISubSystemProcessFlush
import net.eln.mna.active.InterSystemAbstraction
import net.eln.mna.misc.MnaConst
import net.eln.mna.passive.*
import net.eln.mna.state.Node
import net.eln.mna.state.VoltageNode

import java.util.*

class RootSystem(internal var dt: Double, private var interSystemOverSampling: Int) {

    var systems = ArrayList<SubSystem>()

    var components: MutableSet<Component> = HashSet()
    var nodes = HashSet<Node>()

    private var processF = ArrayList<ISubSystemProcessFlush>()

    private var processPre = ArrayList<IRootSystemPreStepProcess>()

    val subSystemCount: Int
        get() = systems.size

    fun addComponent(c: Component) {
        components.add(c)
        c.onAddToRootSystem()

        for (s in c.getConnectedStates()) {
            if (s == null) continue
            if (s.subSystem != null) {
                breakSystems(s.subSystem!!)
            }
        }
    }

    fun removeComponent(c: Component) {
        val system = c.getSubSystem()
        if (system != null) {
            breakSystems(system)
        }

        components.remove(c)
        c.onRemovefromRootSystem()
    }

    fun addState(s: Node) {
        for (c in s.getConnectedComponentsNotAbstracted().clone() as ArrayList<*>) {
            if (c is Component) {
                if (c.getSubSystem() != null)
                    breakSystems(c.getSubSystem()!!)
            }
        }
        nodes.add(s)
    }

    fun removeState(s: Node) {
        val system = s.subSystem
        if (system != null) {
            breakSystems(system)
        }
        nodes.remove(s)
    }

    fun addProcess(p: ISubSystemProcessFlush) {
        processF.add(p)
    }

    fun removeProcess(p: ISubSystemProcessFlush) {
        processF.remove(p)
    }

    fun addProcess(p: IRootSystemPreStepProcess) {
        processPre.add(p)
    }

    fun removeProcess(p: IRootSystemPreStepProcess) {
        processPre.remove(p)
    }

    fun isRegistred(load: ElectricalLoad): Boolean {
        return load.subSystem != null || nodes.contains(load)
    }


    fun step() {
        generate()
        for (idx in 0 until interSystemOverSampling)
            processPre.forEach { it.rootSystemPreStepProcess() }
        systems.forEach {it.stepCalc()}
        systems.forEach { it.stepFlush() }
        processF.forEach { it.simProcessFlush() }
    }

    // TODO: This function is terrible and undocumented.
    private fun generate() {
        if (components.isNotEmpty() || nodes.isNotEmpty()) {
            generateLine()
            generateSystems()
            generateInterSystems()

            var stateCnt = 0
            var componentCnt = 0

            systems.forEach {
                stateCnt += it.states.size
                componentCnt += it.component.size
            }

            MnaConst.logger.info("Ran generate")
        }
    }

    // TODO: This function is terrible and undocumented.
    private fun generateLine() {
        val stateScope = HashSet<Node>()
        nodes.filter{isValidForLine(it)}.forEach {stateScope.add(it)}

        while (stateScope.isNotEmpty()) {
            val sRoot = stateScope.iterator().next()

            var sPtr = sRoot
            var rPtr = sPtr.getConnectedComponentsNotAbstracted()[0] as Resistor
            while (true) {
                for (c in sPtr.getConnectedComponentsNotAbstracted()) {
                    if (c !== rPtr) {
                        rPtr = c as Resistor
                        break
                    }
                }
                var sNext: Node? = null

                if (sPtr !== rPtr.aPin)
                    sNext = rPtr.aPin
                else if (sPtr !== rPtr.bPin) sNext = rPtr.bPin

                if (sNext == null || sNext === sRoot || !stateScope.contains(sNext)) break

                sPtr = sNext
            }

            val lineStates = LinkedList<Node>()
            val lineResistors = LinkedList<Resistor>()

            lineResistors.add(rPtr)
            while (true) {
                lineStates.add(sPtr)
                stateScope.remove(sPtr)
                for (c in sPtr.getConnectedComponentsNotAbstracted()) {
                    if (c !== rPtr) {
                        rPtr = c as Resistor
                        break
                    }
                }
                lineResistors.add(rPtr)

                var sNext: Node? = null

                if (sPtr !== rPtr.aPin)
                    sNext = rPtr.aPin
                else if (sPtr !== rPtr.bPin) sNext = rPtr.bPin

                if (sNext == null || !stateScope.contains(sNext)) break

                sPtr = sNext
            }

            if (lineResistors.first === lineResistors.last) {
                lineResistors.pop()
                lineStates.pop()
            }

            Line.newLine(this, lineResistors, lineStates)
        }
    }

    // TODO: This function is terrible and undocumented.
    private fun generateSystems() {
        val firstState = LinkedList<Node>()
        nodes.filter{it.mustBeFarFromInterSystem()}.forEach {firstState.add(it)}
        firstState.filter{it.subSystem == null}.forEach{buildSubSystem(it)}
        while (nodes.isNotEmpty()) {
            buildSubSystem(nodes.iterator().next())
        }
    }

    // TODO: This function is terrible and undocumented.
    private fun generateInterSystems() {
        val ic = components.iterator()
        while (ic.hasNext()) {
            val c = ic.next()

            if (!c.canBeReplacedByInterSystem()) {
                MnaConst.logger.error("InterSystemError! (RootSystem)")
            } else {
                MnaConst.logger.info("$c")
            }

            if (c is Delay) {
                val r = c as Resistor
                // If a pin is disconnected, we can't be intersystem
                if (r.aPin == null || r.bPin == null) continue

                InterSystemAbstraction(this, r)
                ic.remove()
            }
        }
    }

    // TODO: This function is terrible and undocumented.
    private fun buildSubSystem(root: Node) {
        val componentSet = HashSet<Component>()
        val stateSet = HashSet<Node>()

        val roots = LinkedList<Node>()
        roots.push(root)
        buildSubSystem(roots, componentSet, stateSet)

        components.removeAll(componentSet)
        nodes.removeAll(stateSet)

        val subSystem = SubSystem(this, dt)
        MnaConst.logger.debug(stateSet.toString())
        MnaConst.logger.debug(componentSet.toString())
        subSystem.addState(stateSet)
        subSystem.addComponent(componentSet)

        systems.add(subSystem)
    }

    // TODO: This function is terrible and undocumented.
    private fun buildSubSystem(roots: LinkedList<Node>, componentSet: MutableSet<Component>, stateSet: MutableSet<Node>) {
        val privateSystem = roots.first.isPrivateSubSystem

        while (!roots.isEmpty()) {
            val sExplored = roots.pollFirst()
            if (sExplored != null) {
                stateSet.add(sExplored)
            }

            for (c in sExplored!!.getConnectedComponentsNotAbstracted()) {
                if (privateSystem && roots.size + stateSet.size > maxSubSystemSize && c.canBeReplacedByInterSystem()) {
                    continue
                }
                if (componentSet.contains(c)) continue
                var noGo = false
                for (sNext in c.getConnectedStates()) {
                    if (sNext == null) continue
                    if (sNext.subSystem != null) {
                        noGo = true
                        break
                    }
                    if (sNext.isPrivateSubSystem != privateSystem) {
                        noGo = true
                        break
                    }
                }

                if (noGo) continue
                componentSet.add(c)
                for (sNext in c.getConnectedStates()) {
                    if (sNext == null) continue
                    if (stateSet.contains(sNext)) continue
                    roots.addLast(sNext)
                }
            }
        }
    }

    // TODO: UHHHHH wat are you actually doing?
    private fun breakSystems(sub: SubSystem) {
        if (sub.breakSystem()) {
            for (s in sub.interSystemConnectivity) {
                breakSystems(s)
            }
        }
    }

    private fun isValidForLine(s: Node): Boolean {
        if (!s.canBeSimplifiedByLine()) return false
        val sc = s.getConnectedComponentsNotAbstracted()
        if (sc.size != 2) return false
        for (c in sc) {
            if (c !is Resistor) return false
        }

        return true
    }

    companion object {

        private const val maxSubSystemSize = 100

        // TODO: Move test elsewhere in the testing suite, doesn't need to be here.
        @JvmStatic
        fun main(args: Array<String>) {
            val s = RootSystem(0.1, 1)

            val n1= VoltageNode("n1")
            val n2= VoltageNode("n2")
            val u1 = VoltageSource("u1")
            val r1 = Resistor("r1")
            val r2 = Resistor("r2")

            u1.u = 1.0
            r1.r = 10.0
            r2.r = 20.0

            r1.connectTo(n1, n2)
            r2.connectTo(n2, null)
            u1.connectTo(n1, null)

            s.addState(n1)
            s.addState(n2)
            s.addComponent(u1)
            s.addComponent(r1)
            s.addComponent(r2)

            val n11 = VoltageNode("n11")
            val n12 = VoltageNode("n12")
            val u11 = VoltageSource("u11")
            val r11 = Resistor("r11")
            val r12 = Resistor("r12")
            val r13 = Resistor("r13")

            u11.u = 1.0
            r11.r = 10.0
            r12.r = 30.0
            r13.r = 30.0

            u11.connectTo(n11, null)
            r11.connectTo(n11, n12)
            r12.connectTo(n12, null)
            r13.connectTo(n12, null)

            s.addState(n11)
            s.addState(n12)
            s.addComponent(u11)
            s.addComponent(r11)
            s.addComponent(r12)


            val is1 = InterSystem()
            is1.r = 10.0
            is1.connectTo(n2, n12)

            s.addComponent(is1)

            for (i in 0..1) {
                s.step()
            }

            s.addComponent(r13)

            for (i in 0..1) {
                s.step()
            }

            s.step()

            for (d in s.systems) {
                print("system: $d")
            }
        }
    }
}

//TODO: garbadge collector - where?
//TODO: ghost suppression - why?
