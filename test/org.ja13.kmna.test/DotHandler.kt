package org.ja13.kmna.test

import net.eln.mna.RootSystem
import net.eln.mna.SubSystem
import net.eln.mna.passive.*
import net.eln.mna.state.Node
import net.eln.mna.state.VoltageNode

class DotHandler {
    companion object {
        /**
         * parseDot - parses a dot file with a particular language to a RootSystem
         *
         * @param str The input string using the dot language
         * @return RootSystem ready for simulation
         */
        fun parseDot(str: String): RootSystem {
            val rootSystem = RootSystem(KmnaParseTester.dt, 10)
            val lines = str.split("\n")
            val lineItr = lines.iterator()
            while(lineItr.hasNext()) {
                val sline = lineItr.next()
                if ("graph" in sline) {
                    val lsplit = sline.split(" ","\t")
                    if (lsplit.size > 2) {
                        val subsystemName = lsplit[1]
                        println("parsing $subsystemName")
                        val subSystem = SubSystem(null, KmnaParseTester.dt)
                        val componentList = mutableListOf<Component>()
                        val nodeList = mutableMapOf<String, Node?>()
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

                                if ((componentList.size > 1) and (nodeList.size > 1)) {
                                    rootSystem.systems.add(subSystem)
                                    subSystem.root = rootSystem
                                } else {
                                    println("Error, subsystem is trash.")
                                }
                                println("finished parsing $subsystemName")
                                break
                            }
                            val parts = line.replace("]","").replace(";","").trim().split("[")
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

                                val aPin: Node? = nodeList[connections[0]]
                                val bPin: Node? = nodeList[connections[1]]

                                component = componentBuilder(properties, aPin, bPin)

                                if (component != null) {
                                    println("Adding component $component")
                                    componentList.add(component)
                                }
                            }else{
                                // node
                                val name = parts[0].trim()
                                val type: Node?

                                when(properties["label"]) {
                                    "null" -> {
                                        type = null
                                    }
                                    "VoltageState" -> {
                                        type = VoltageNode(properties["name"]?: "")
                                        type.id = name.toInt()
                                        type.u = properties["volts"]?.toDouble()?: 0.0
                                    }
                                    else -> {
                                        type = null
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

        fun componentBuilder(properties: MutableMap<String, String>, aPin: Node?, bPin: Node?): Component? {
            val component: Component?

            // === Handles the component itself ===
            when (properties["label"]) {
                "Resistor", "Wire" -> {
                    component = Resistor(aPin, bPin)
                    component.name = properties["name"]?: ""
                    component.r = properties["ohms"]?.toDouble()?: 1e9
                    if (component.r < 0.001) component.r = 0.001
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
                "ResistorSwitch" -> {
                    component = ResistorSwitch(properties["name"]?: "", aPin, bPin)
                    component.setState(false)
                    component.r = 1e-1
                }
                else -> {
                    component = null
                }
            }
            return component;
        }
    }
}