package net.eln.mna.misc

import net.eln.mna.SubSystem
import net.eln.mna.passive.Component

/**
 * IAbstractor
 *
 * I'm not sure what this interface is for quite yet,
 * but it's currently only used by InterSystem and basically creates a isolated resistor for a Thevenin adjacent system.
 */
interface IAbstractor {

    var abstractorSubSystem: SubSystem

    fun dirty(component: Component)
}
