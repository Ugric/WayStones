package main.java.dev.wbell.waystones

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin


object Scheduler {
    private var IS_FOLIA: Boolean? = null
    private fun tryFolia(): Boolean {
        try {
            Bukkit.getAsyncScheduler()
            return true
        } catch (ignored: Throwable) {
        }
        return false
    }

    private val isFolia: Boolean?
        get() {
            if (IS_FOLIA == null) IS_FOLIA = tryFolia()
            return IS_FOLIA
        }
    fun runTaskForEntity(entity: Entity, plugin: Plugin?, entityTask: Runnable, initialDelayTicks: Long) {
        if (isFolia!!) {
            entity.scheduler.runDelayed(
                plugin!!,
                { _: ScheduledTask? -> entityTask.run() }, null, initialDelayTicks
            )
        } else {
            Bukkit.getScheduler().runTaskLater(plugin!!, entityTask, initialDelayTicks)
        }
    }
}