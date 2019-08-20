package net.eln.common

import net.eln.mna.state.Node

interface ISerializedNode {
    val typeString: String
    fun importProperties(data: Map<String, String>)
    fun exportProperties(): Map<String, String>
}

interface ISerializedComponent {
    val typeString: String
    fun importProperties(data: Map<String, String>, pins: List<Node?>)
    fun exportProperties(): Pair<Map<String, String>, List<Node?>>
}

interface ISerializedCircuit {
    fun importCircuit(data: String)
    fun exportCircuit(): String
}