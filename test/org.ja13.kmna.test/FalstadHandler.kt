package org.ja13.kmna.test

import net.eln.mna.passive.*
import net.eln.mna.state.State

class FalstadHandler {
    companion object {
        /**
         * parseFalstad - transpiled the Falstad language into the Dot language
         *
         * NOTE: will remove state information (such as the prepared voltage of a device or the current across a device)
         *
         * @param str The input string using the Falstad language
         * @return The output string using the dot language (feed this into parseDot)
         */
        fun parseFalstad(str: String): String {
            val nodes = mutableListOf<String>()
            val components = mutableListOf<String>()

            val commands = str.split("\n")

            for (c in commands) {

                var cs = c.split(" ")
                cs = cs.map { it.trim() }

                when (cs[0]) {
                    // ==== NON COMPONENTS
                    "$" -> {
                        // === Initializer Statement ===
                        // flags = cs[1]
                        // timeStep = cs[2].toInt()
                        // rest of the arguments are unimportant (probably) for our use case
                    }
                    "o" -> {
                        // === Scope Statement ===
                    }
                    "38" -> {
                        // === Adjustable Statement ===
                    }
                    "h" -> {
                        // === Hint Statement ===
                    }
                    "%", "/" -> {
                        // === While not official, I'm going to use % symbols and / (//) as comment lines.
                        // do nothing
                    }
                    else -> if (cs.size >= 6) {
                        componentBuilder(
                            cs[0],
                            cs[1].toInt(), cs[2].toInt(),
                            cs[3].toInt(), cs[4].toInt(),
                            cs[5].toInt(),
                            if (cs.size > 5) {cs.subList(6, cs.size )} else listOf(),
                            nodes,
                            components
                        )
                    }
                }
            }

            val output = mutableListOf<String>()
            nodes.distinct().forEach {output.add("$it [label=Nod3];" ) }
            output.addAll(components)

            return "graph subsystem0 {\n${output.joinToString("\n")}\n}\n"
        }

        fun componentBuilder(
            type: String,
            x1: Int, y1: Int,
            x2: Int, y2: Int,
            flags: Int,
            parameters: List<String>,
            nodes: MutableList<String>,
            components: MutableList<String>
        ): Component? {

            val aPin = "$x1$y1"
            val bPin = "$x2$y2"

            val component: String
            when(type) {
                "g" -> {
                    // ground (one pin)
                    component = "$aPin -- null [label=VoltageSource volts=0.0];"
                    components.add(component)
                    nodes.add(aPin)
                }
                "r" -> {
                    // resistor
                    component = "$aPin -- $bPin [label=Resistor ohms=${parameters[0]}];"
                    components.add(component)
                    nodes.add(aPin)
                    nodes.add(bPin)
                }
                "R" -> {
                    // voltage rail (one pin)
                    component = "$aPin -- null [label=VoltageSource volts=${parameters[2]}];"
                    components.add(component)
                    nodes.add(aPin)
                }
                "s" -> {
                    // switch
                    component = "$aPin -- $bPin [label=ResistorSwitch];"
                    components.add(component)
                    nodes.add(aPin)
                    nodes.add(bPin)
                }
                "S" -> {
                    // switch
                    component = "$aPin -- $bPin [label=ResistorSwitch];"
                    components.add(component)
                    nodes.add(aPin)
                    nodes.add(bPin)
                }
                "t" -> {
                    // transistor
                }
                "w" -> {
                    // wire
                    component = "$aPin -- $bPin [label=Wire ohms=0.0];"
                    components.add(component)
                    nodes.add(aPin)
                    nodes.add(bPin)
                }
                "c" -> {
                    // capacitor
                    component = "$aPin -- $bPin [label=Capacitor farads=${parameters[0]}];"
                    components.add(component)
                    nodes.add(aPin)
                    nodes.add(bPin)
                }
                "209" -> {
                    // polar capacitor?
                }
                "l" -> {
                    // inductor
                    component = "$aPin -- $bPin [label=Inductor henries=${parameters[0]}];"
                    components.add(component)
                    nodes.add(aPin)
                    nodes.add(bPin)
                }
                "v" -> {
                    // voltage source (two pin)
                    component = "$bPin -- $aPin [label=VoltageSource volts=${parameters[2]}];"
                    components.add(component)
                    nodes.add(aPin)
                    nodes.add(bPin)
                }
                "172" -> {
                    // "var" "rail" ??
                }
                "174" -> {
                    // "pot"
                }
                "O" -> {
                    // "output"
                }
                "i" -> {
                    // current source?
                }
                "p" -> {
                    // probe?
                }
                "d" -> {
                    // diode
                }
                "z" -> {
                    // zenner diode
                }
                "170" -> {
                    // sweep
                }
                "162" -> {
                    // LED
                }
                "A" -> {
                    // "antenna"
                }
                "L" -> {
                    // Logic input
                }
                "M" -> {
                    // logic output
                }
                "T" -> {
                    // transformer
                }
                "169" -> {
                    // tapped transformer
                }
                "171" -> {
                    // "trams line
                }
                "178" -> {
                    // relay
                }
                "m" -> {
                    // memristor
                }
                "187" -> {
                    // spark gap
                }
                "200" -> {
                    // AM source?
                }
                "201" -> {
                    // FM source?
                }
                "n" -> {
                    // noise source?
                }
                "181" -> {
                    // Lamp
                }
                "a" -> {
                    // Op Amp
                }
                "f" -> {
                    // Mosfet
                }
                "j" -> {
                    // Jfet
                }
                "159" -> {
                    // "Analog Switch?"
                }
                "160" -> {
                    // "analog switch"
                }
                "180" -> {
                    // " Tri state"
                }
                "182" -> {
                    // Schmitt
                }
                "183" -> {
                    // Inverting Schmitt
                }
                "177" -> {
                    // SCR
                }
                "203" -> {
                    // "Diac"
                }
                "206" -> {
                    // Triac
                }
                "173" -> {
                    // Triode
                }
                "175" -> {
                    // "Tunnel Diode"
                }
                "176" -> {
                    // Varactor
                }
                "179" -> {
                    // ???? CC2
                }
                "I" -> {
                    // Inverter
                }
                "151" -> {
                    // NAND
                }
                "153" -> {
                    // NOR
                }
                "150" -> {
                    // AND
                }
                "152" -> {
                    // OR
                }
                "154" -> {
                    // XOR
                }
                "155" -> {
                    // D-Flip Flop
                }
                "156" -> {
                    // JK-Flip Flop
                }
                "157" -> {
                    // 7 seg
                }
                "184" -> {
                    // Multiplexor
                }
                "185" -> {
                    // demultiplexor
                }
                "189" -> {
                    // "SipoShift
                }
                "186" -> {
                    // "PisoShift
                }
                "161" -> {
                    // phase comp
                }
                "164" -> {
                    // counter
                }
                "163" -> {
                    // RingCounter
                }
                "165" -> {
                    // Timer
                }
                "166" -> {
                    // DAC
                }
                "167" -> {
                    // ADC
                }
                "168" -> {
                    // Latch
                }
                "188" -> {
                    // Sequence Generator?
                }
                "158" -> {
                    // VCO
                }
                "b" -> {
                    // Box?
                }
                "x" -> {
                    // Text?
                }
                "193" -> {
                    // T Flip Flop
                }
                "197" -> {
                    // Seven Segment Decoder
                }
                "196" -> {
                    // Full Adder
                }
                "195" -> {
                    // Half Adder
                }
                "194" -> {
                    // Monostable
                }
                "207" -> {
                    // "labeled node"?
                }
                "208" -> {
                    // Custom Logic Element
                }
                "210" -> {
                    // data recorder?
                }
                "211" -> {
                    // audio output
                }
                "212" -> {
                    // VCVS?
                }
                "213" -> {
                    // VCCS?
                }
                "214" -> {
                    // CCVS?
                }
                "215" -> {
                    // CCCS?
                }
                "216" -> {
                    // Ohm Meter
                }
                "368" -> {
                    // Test Point
                }
                "370" -> {
                    // Ammeter
                }
                "400" -> {
                    // Darlington?
                }
                "401" -> {
                    // comparator?
                }
                "402" -> {
                    // OTAE?
                }
                "403" -> {
                    // scope?
                }
                "404" -> {
                    // fuse
                }
                "405" -> {
                    // LED Array
                }
                "406" -> {
                    // custom transformer
                }
                "407" -> {
                    // optocoupler
                }
                "408" -> {
                    // Stop Trigger?
                }
                "409" -> {
                    // op amp (real?)
                }
                "410" -> {
                    // Custom Composite?
                }
                "411" -> {
                    // Audio Input
                }
            }
            return null
        }
    }
}