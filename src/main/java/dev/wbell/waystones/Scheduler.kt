package main.java.dev.wbell.waystones

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit
import java.util.function.Consumer


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

    val isFolia: Boolean?
        get() {
            if (IS_FOLIA == null) IS_FOLIA = tryFolia()
            return IS_FOLIA
        }

    fun runAsyncSchedulerDelay(plugin: Plugin?, playerTask: Consumer<Player?>, initialDelayTicks: Int) {
        if (isFolia!!) {
            Bukkit.getAsyncScheduler().runDelayed(
                plugin!!,
                { task: ScheduledTask? ->
                    for (player in Bukkit.getOnlinePlayers()) {
                        playerTask.accept(player)
                    }
                }, initialDelayTicks.toLong(), TimeUnit.SECONDS
            )
        } else {
            Bukkit.getScheduler().runTaskLater(plugin!!, Runnable {
                for (player in Bukkit.getOnlinePlayers()) {
                    playerTask.accept(player)
                }
            }, initialDelayTicks * 20L)
        }
    }

    fun runTaskForEntity(entity: Entity, plugin: Plugin?, entityTask: Runnable, initialDelayTicks: Long) {
        if (isFolia!!) {
            entity.scheduler.runDelayed(
                plugin!!,
                { task: ScheduledTask? -> entityTask.run() }, null, initialDelayTicks
            )
        } else {
            Bukkit.getScheduler().runTaskLater(plugin!!, entityTask, initialDelayTicks)
        }
    }
}