package net.eln.mna.state

import net.eln.common.ISerializedNode
import net.eln.mna.RootSystem
import net.eln.mna.SubSystem
import net.eln.mna.passive.Component
import net.eln.mna.misc.IAbstractor
import net.eln.mna.misc.MnaConst

import java.util.ArrayList

abstract class Node: ISerializedNode {

    var id = -1
    var name = "Node"

    constructor()
    constructor(name: String) {
        this.name = name
    }

    var state: Double = 0.0
    var subSystem: SubSystem? = null
        get() {
            if (abstractedBy != null) {
                return abstractedBy!!.abstractorSubSystem
            } else {
                return field
            }
        }

    var connectedComponents = ArrayList<Component>()
        internal set

    var isPrivateSubSystem = false
        internal set
    internal var mustBeFarFromInterSystem = false

    var abstractedBy: IAbstractor? = null

    fun getConnectedComponentsNotAbstracted(): ArrayList<Component> {
            val list = ArrayList<Component>()
            for (c in connectedComponents) {
                if (c.abstractedBy != null) continue
                list.add(c)
            }
            return list
        }

    fun isNotSimulated(): Boolean = subSystem == null && abstractedBy == null

    fun addedTo(s: SubSystem) {
        this.subSystem = s
    }

    fun quitSubSystem() {
        subSystem = null
    }

    fun add(c: Component) {
        connectedComponents.add(c)
    }

    fun remove(c: Component) {
        connectedComponents.remove(c)
    }

    open fun canBeSimplifiedByLine(): Boolean {
        return false
    }

    fun setAsPrivate(): Node {
        isPrivateSubSystem = true
        return this
    }

    fun setAsMustBeFarFromInterSystem(): Node {
        mustBeFarFromInterSystem = true
        return this
    }

    fun mustBeFarFromInterSystem(): Boolean {
        return mustBeFarFromInterSystem
    }

    fun returnToRootSystem(root: RootSystem) {
        root.nodes.add(this)
    }

    override fun toString(): String {
        return "(" + this.id + "," + this.javaClass.simpleName + "_" + name + ")"
    }

    fun clean(name: String): String = name.filter {it.isLetterOrDigit()}

    override fun exportProperties(): Map<String, String> {
        return mapOf(Pair("NAME", clean(name)))
    }

    override fun importProperties(data: Map<String, String>) {
        name = data["NAME"]?: name
    }
}

open class VoltageNode : Node {

    override val typeString: String
        get() = "VN"

    constructor() {
        this.name = "VoltageNode"
    }
    constructor(name: String) : super(name)

    var u: Double
        get() = state
        set(state) {
            if (state.isNaN())
                MnaConst.logger.error("node.VoltageNode setU(double node) - node was NaN!")
            this.state = state
        }

    override fun exportProperties(): Map<String, String> {
        val prop = super.exportProperties().toMutableMap()
        prop["U"] = u.toString()
        return prop
    }

    override fun importProperties(data: Map<String, String>) {
        this.u = data["U"]?.toDouble()?: this.u
    }
}

class CurrentNode : Node() {

    override val typeString: String
        get() = "CN"

    init {
        this.name = "CurrentNode"
    }
}

open class VoltageNodeLineReady : VoltageNode() {

    override val typeString: String
        get() = "VNLR"

    init {
        name = "VoltageNodeLineReady"
    }

    var canBeSimplifiedByLine = false

    override fun exportProperties(): Map<String, String> {
        val prop = super.exportProperties().toMutableMap()
        prop["CBSBL"] = canBeSimplifiedByLine.toString()
        return prop
    }

    override fun importProperties(data: Map<String, String>) {
        canBeSimplifiedByLine = data["CBSBL"]?.toBoolean()?: canBeSimplifiedByLine
    }
}
