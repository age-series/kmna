package net.eln.mna.passive

import net.eln.mna.RootSystem
import net.eln.mna.SubSystem
import net.eln.mna.misc.IAbstractor
import net.eln.mna.state.State

abstract class Component {

    var name = ""

    internal var subSystem: SubSystem? = null

    var abstractedBy: IAbstractor? = null

    abstract fun getConnectedStates(): Array<State?>

    open fun addedTo(s: SubSystem) {
        this.subSystem = s
    }

    fun getSubSystem(): SubSystem? {
        if (abstractedBy == null) {
            return subSystem
        } else {
            return abstractedBy?.abstractorSubSystem
        }
    }

    abstract fun applyTo(s: SubSystem)

    open fun canBeReplacedByInterSystem(): Boolean {
        return false
    }

    open fun breakConnection() {}

    open fun returnToRootSystem(root: RootSystem?) {
        root!!.addComponents.add(this)
    }

    fun dirty() {
        if (abstractedBy != null) {
            abstractedBy!!.dirty(this)
        } else if (getSubSystem() != null) {
            getSubSystem()!!.invalidate()
        }
    }

    open fun quitSubSystem() {
        subSystem = null
    }

    open fun onAddToRootSystem() {}

    open fun onRemovefromRootSystem() {}

    override fun toString(): String {
        return "(" + this.javaClass.simpleName + "_" + name + ")"
    }
}
