package net.eln.mna.misc

import net.eln.mna.passive.InterSystem
import net.eln.mna.state.ElectricalLoad

class ElectricalConnection(internal var L1: ElectricalLoad, internal var L2: ElectricalLoad) : InterSystem() {

    init {
        if (L1 === L2) MnaConst.logger.error("ElectricalConnection: Attempt to connect load to itself!")
    }

    fun notifyRsChange() {
        r = (aPin as ElectricalLoad).rs + (bPin as ElectricalLoad).rs
    }

    override fun onAddToRootSystem() {
        this.connectTo(L1, L2)
        notifyRsChange()
    }

    override fun onRemovefromRootSystem() {
        this.breakConnection()
    }
}
