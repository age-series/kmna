package net.eln.common

import net.eln.thermal.PhysicalConstant

class PrintValue {
    companion object {
        fun plotValue(value: Double): String {
            val valueAbs = Math.abs(value)
            return if (valueAbs < 0.0001) {
                // if one cared? I'd suggest just converting the double completely, and skip this whole "switch" thing.
                // plus, FPU's almost never give you 0.0 or -0.0 (yup, negative zero is a thing)
                //return String.format("%1.3fµ",value * 10000);
                "0.0"
            } else if (valueAbs < 0.000999) {
                String.format("%1.2fµ", value * 10000)
            } else if (valueAbs < 0.00999) {
                String.format("%1.2fm", value * 1000)
            } else if (valueAbs < 0.0999) {
                String.format("%2.1fm", value * 1000)
            } else if (valueAbs < 0.999) {
                String.format("%3.0fm", value * 1000)
            } else if (valueAbs < 9.99) {
                String.format("%1.2f", value)
            } else if (valueAbs < 99.9) {
                String.format("%2.1f", value)
            } else if (valueAbs < 999) {
                String.format("%3.0f", value)
            } else if (valueAbs < 9999) {
                String.format("%1.2fk", value / 1000.0)
            } else if (valueAbs < 99999) {
                String.format("%2.1fk", value / 1000.0)
            } else if (valueAbs < 999999) {
                String.format("%3.0fk", value / 1000.0)
            } else if (valueAbs < 9999999) {
                String.format("%1.2fM", value / 1000000.0)
            } else if (valueAbs < 99999999) {
                String.format("%2.1fM", value / 1000000.0)
            } else {
                String.format("%3.0fM", value / 1000000.0)
                // and bigger, and bigger, and bigger... I think if you're going over 1MA or 1MV, you're probably done.
            }
        }

        fun plotValue(value: Double, unit: String): String {
            return plotValue(value) + unit
        }

        fun plotVolt(value: Double): String {
            return plotValue(value, "V  ")
        }

        fun plotAmpere(value: Double): String {
            return plotValue(value, "A  ")
        }

        fun plotCelsius(value: Double): String {
            var value2 = value + PhysicalConstant.Tref - PhysicalConstant.TCelsius
            return plotValue(value2, "\u00B0C ")
        }

        fun plotPercent(value: Double): String {
            return if (value >= 1.0)
                String.format("%3.0f", value * 100.0) + "%   "
            else
                String.format("%3.1f", value * 100.0) + "%   "
        }

        fun plotEnergy(value: Double): String {
            return plotValue(value, "J")
        }

        fun plotRads(value: Double): String {
            return plotValue(value, "rad/s ")
        }

        fun plotPower(value: Double): String {
            return plotValue(value, "W  ")
        }

        fun plotOhm(value: Double): String {
            return plotValue(value, "\u2126 ")
        }

        fun plotUIP(U: Double, I: Double): String {
            return plotVolt(U) + plotAmpere(I) + plotPower(U * I)
        }

        fun plotTime(value: Double): String {
            var value = value
            var str = ""
            val h: Int
            val mn: Int
            val s: Int

            if (value == 0.0)
                return str + "0''"

            h = (value / 3600).toInt()
            value = value % 3600
            mn = (value / 60).toInt()
            value = value % 60
            s = (value / 1).toInt()

            if (h != 0)
                str += h.toString() + "h"
            if (mn != 0)
                str += "$mn'"
            if (s != 0)
                str += "$s''"
            return str
        }
    }
}