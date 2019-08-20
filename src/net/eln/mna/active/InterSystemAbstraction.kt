package net.eln.mna.active

import net.eln.mna.RootSystem
import net.eln.mna.SubSystem
import net.eln.mna.passive.Component
import net.eln.mna.passive.Resistor
import net.eln.mna.misc.IAbstractor
import net.eln.mna.misc.IDestructor
import net.eln.mna.state.Node
import net.eln.mna.state.VoltageNode

class InterSystemAbstraction(internal var root: RootSystem, internal var interSystemResistor: Resistor) : IAbstractor,
    IDestructor {

    internal var aNewState: VoltageNode
    internal var aNewResistor: Resistor
    internal var aNewDelay: DelayInterSystem2
    internal var bNewState: VoltageNode
    internal var bNewResistor: Resistor
    internal var bNewDelay: DelayInterSystem2
    internal var thevnaCalc: DelayInterSystem2.ThevnaCalculator

    internal var aState: Node = interSystemResistor.aPin ?: throw Exception("aPin on InterSystemResistor cannot be null!")
    internal var bState: Node = interSystemResistor.bPin ?: throw Exception("bPin on InterSystemResistor cannot be null!")
    override var abstractorSubSystem: SubSystem = aState.subSystem ?: throw Exception("subsystem connected to aPin cannot be null!")
    internal var bSystem: SubSystem = bState.subSystem ?: throw Exception("subsystem connected to bPin cannot be null!")

    init {
        abstractorSubSystem.interSystemConnectivity.add(bSystem)
        bSystem.interSystemConnectivity.add(abstractorSubSystem)

        aNewState = VoltageNode()
        aNewResistor = Resistor()
        aNewDelay = DelayInterSystem2()
        bNewState = VoltageNode()
        bNewResistor = Resistor()
        bNewDelay = DelayInterSystem2()

        aNewResistor.connectGhostTo(aState, aNewState)
        aNewDelay.connectTo(aNewState, null)
        bNewResistor.connectGhostTo(bState, bNewState)
        bNewDelay.connectTo(bNewState, null)

        calibrate()

        abstractorSubSystem.addComponent(aNewResistor)
        abstractorSubSystem.addState(aNewState)
        abstractorSubSystem.addComponent(aNewDelay)
        bSystem.addComponent(bNewResistor)
        bSystem.addState(bNewState)
        bSystem.addComponent(bNewDelay)

        abstractorSubSystem.breakDestructor.add(this)
        bSystem.breakDestructor.add(this)

        interSystemResistor.abstractedBy = this

        thevnaCalc = DelayInterSystem2.ThevnaCalculator(aNewDelay, bNewDelay)
        root.addProcess(thevnaCalc)
    }

    internal fun calibrate() {
        val u = (aState.state + bState.state) / 2
        aNewDelay.u = u
        bNewDelay.u = u

        val r = interSystemResistor.r / 2
        aNewResistor.r = r
        bNewResistor.r = r
    }

    override fun dirty(component: Component) {
        calibrate()
    }

    override fun destruct() {
        abstractorSubSystem.breakDestructor.remove(this)
        abstractorSubSystem.removeComponent(aNewDelay)
        abstractorSubSystem.removeComponent(aNewResistor)
        abstractorSubSystem.removeState(aNewState)
        bSystem.breakDestructor.remove(this)
        bSystem.removeComponent(bNewDelay)
        bSystem.removeComponent(bNewResistor)
        bSystem.removeState(bNewState)

        root.removeProcess(thevnaCalc)

        interSystemResistor.abstractedBy = null

        abstractorSubSystem.component.add(interSystemResistor)
    }
}
