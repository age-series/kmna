package net.eln.mna.misc

interface IProfiler {
    var time: Long

    fun init()

    fun reset()

    fun add(name: String)
    fun start()
    fun stop()
}