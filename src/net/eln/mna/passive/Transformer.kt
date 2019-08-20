package net.eln.mna.passive

import net.eln.mna.SubSystem
import net.eln.mna.state.CurrentNode
import net.eln.mna.state.Node

class Transformer : Bipole {

    override val typeString: String
        get() = "T"

    var aCurrentState = CurrentNode()
    var bCurrentState = CurrentNode()

    var ratio = 1.0

    override fun getCurrent() = 0.0

    constructor() {}

    constructor(aPin: Node, bPin: Node) : super(aPin, bPin) {}

    override fun quitSubSystem() {
        getSubSystem()!!.states.remove(aCurrentState)
        getSubSystem()!!.states.remove(bCurrentState)
        super.quitSubSystem()
    }

    override fun addedTo(s: SubSystem) {
        super.addedTo(s)
        s.addState(aCurrentState)
        s.addState(bCurrentState)
    }

    override fun applyTo(s: SubSystem) {
        s.addToA(bPin, bCurrentState, 1.0)
        s.addToA(bCurrentState, bPin, 1.0)
        s.addToA(bCurrentState, aPin, -ratio)

        s.addToA(aPin, aCurrentState, 1.0)
        s.addToA(aCurrentState, aPin, 1.0)
        s.addToA(aCurrentState, bPin, -1 / ratio)

        s.addToA(aCurrentState, aCurrentState, 1.0)
        s.addToA(aCurrentState, bCurrentState, ratio)
        s.addToA(bCurrentState, aCurrentState, 1.0)
        s.addToA(bCurrentState, bCurrentState, ratio)
    }

    override fun exportProperties(): Pair<Map<String, String>, List<Node?>> {
        val s = super.exportProperties()
        val prop = s.first.toMutableMap()
        prop["RATIO"] = ratio.toString()
        return Pair(prop, s.second)
    }

    override fun importProperties(data: Map<String, String>, pins: List<Node?>) {
        super.importProperties(data, pins)
        ratio = data["RATIO"]?.toDouble()?: ratio
    }
}
