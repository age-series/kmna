package org.ja13.kmna.test

import net.eln.mna.RootSystem
import net.eln.mna.SubSystem
import net.eln.mna.passive.*
import net.eln.mna.state.State

class KmnaParseTester {
    companion object {

        val dotNames = mapOf(
            Pair("Resistor", Resistor::class),
            Pair("Inductor", Inductor::class),
            Pair("Capacitor", Capacitor::class),
            Pair("VoltageSource", VoltageSource::class),
            Pair("ResistorSwitch", ResistorSwitch::class)
        )

        val example = """
            graph subsystem0 {
                null [label=null]
                0 [label=Node]
                1 [label=Node]
                2 [label=Node]
                0 -- null [label=VoltageSource volts=10]
                0 -- 1 [label=Resistor ohms=10]
                1 -- 2 [label=Resistor ohms=10]
                2 -- null [label=VoltageSource volts=0]
            }
        """.trimIndent()

        const val dt = 0.05

        @JvmStatic
        fun main(args: Array<String>) {
            val pt = parseDot(example)
            println("Number of systems: ${pt.subSystemCount}")
            pt.systems.forEach {
                println("Subsystem: ${it}")
            }

            pt.step()
            pt.step()
            pt.step()

            pt.systems.forEach {
                it.states.forEach {
                    println("${it.id}: ${it.state}")
                }
            }
        }

        fun parseFalstad(str: String) {

        }

        fun parseDot(str: String): RootSystem {
            val rootSystem = RootSystem(dt, 10)
            val lines = str.split("\n")
            val lineItr = lines.iterator()
            while(lineItr.hasNext()) {
                val sline = lineItr.next()
                if ("graph" in sline) {
                    val lsplit = sline.split(" ","\t")
                    if (lsplit.size > 2) {
                        val subsystemName = lsplit[1]
                        println("parsing $subsystemName")
                        val subSystem = SubSystem(null, dt)
                        val componentList = mutableListOf<Component>()
                        val nodeList = mutableMapOf<String, State?>()
                        nodeList["null"] = null
                        while (lineItr.hasNext()) {
                            val line = lineItr.next()
                            if (line.trim() == "}") {
                                // this is the end
                                for (node  in nodeList) {
                                    val v = node.value
                                    if (v != null)
                                        subSystem.addState(v)
                                }
                                for (component in componentList)
                                    subSystem.addComponent(component)
                                subSystem.root = rootSystem
                                rootSystem.systems.add(subSystem)
                                println("finished parsing $subsystemName")
                                break
                            }
                            val parts = line.replace("]","").trim().split("[")
                            // this will make two parts - the first is the connections or name, the second is the properties
                            if (parts.size != 2) {
                                println("Error parsing line, no properties detected: \n$line")
                                break
                            }

                            val propertiesText: String = parts[1]
                            val properties = mutableMapOf<String, String>()
                            propertiesText.split(" ").forEach {
                                var a = it.split("=")
                                a = a.map { it2 -> it2.trim() }
                                if (a.size == 2)
                                    properties[a[0]] = a[1]
                            }

                            if ("--" in parts[0]) {
                                // component
                                val component: Component?

                                // === Handle the connections ===
                                var connections = parts[0].split("--")
                                connections = connections.map { it.trim() }
                                if (connections.size < 2) {
                                    println("Error parsing line, too few connections: \n$line")
                                    break
                                }
                                var valid = true
                                connections.forEach {
                                    if (it !in nodeList) {
                                        valid = false
                                    }
                                }
                                if (!valid) {
                                    println("Error parsing line, all connecting nodes must be defined before the components that use them: \n$line")
                                    break
                                }

                                val aPin: State? = nodeList[connections[0]]
                                val bPin: State? = nodeList[connections[1]]

                                // === Handles the component itself ===
                                when (properties["label"]) {
                                    "Resistor" -> {
                                        component = Resistor(aPin, bPin)
                                        component.name = properties["name"]?: ""
                                        component.r = properties["ohms"]?.toDouble()?: 1e12
                                    }
                                    "Inductor" -> {
                                        component = Inductor(aPin, bPin)
                                        component.name = properties["name"]?: ""
                                        component.l = properties["henries"]?.toDouble()?: 0.0
                                    }
                                    "Capacitor" -> {
                                        component = Capacitor(aPin, bPin)
                                        component.name = properties["name"]?: ""
                                        component.c = properties["farads"]?.toDouble()?: 0.0
                                    }
                                    "VoltageSource" -> {
                                        component = VoltageSource(aPin, bPin)
                                        component.name = properties["name"]?: ""
                                        component.u = properties["volts"]?.toDouble()?: 0.0
                                    }
                                    else -> {
                                        component = null
                                    }
                                }
                                if (component != null) {
                                    println("Adding component $component")
                                    componentList.add(component)
                                }
                            }else{
                                // node
                                val name = parts[0].trim()
                                val type: State?

                                when(properties["label"]) {
                                    "null" -> {
                                        type = null
                                    }
                                    else -> {
                                        type = State(properties["name"]?: "")
                                        type.id = name.toInt()
                                    }
                                }
                                if (type == null) {
                                    println("Adding null node")
                                }else {
                                    println("Adding node $name, which is of type ${type::class.java.name}")
                                }
                                nodeList.putIfAbsent(name, type)
                            }
                        }
                    }
                }
            }
            return rootSystem
        }
    }
}