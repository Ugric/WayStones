package dev.wbell.terrariateleporter

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.io.File


class TerrariaTeleporter : JavaPlugin() {
    override fun onEnable() {
        instance = this
        // Plugin startup logic
        val endCrystalRightClickListener = EndCrystalRightClickListener()
        endCrystalRightClickListener.playerNearbyHandler()
        EndCrystalRightClickListener.owningPluginInstance = this
        server.pluginManager.registerEvents(endCrystalRightClickListener, this)
        getCommand("renamewaystone")!!.setExecutor(RenameWaystoneCommand())
        // make all the folders
        File(dataFolder.absolutePath).mkdirs()
        dev.wbell.terrariateleporter.waystonePosition.loadPositions(File(dataFolder.absolutePath, "waystones.json"))
    }

    override fun onDisable() {
        dev.wbell.terrariateleporter.waystonePosition.savePositions()
        // Plugin shutdown logic
        logger.info("TerrariaTeleporter has been disabled")
        running = false
    }

    fun startMovingBlock(player: Player) {
        object : BukkitRunnable() {
            override fun run() {
                object : BukkitRunnable() {
                    override fun run() {
                        val location = player.location
                        val world = location.world!!
                        player.sendMessage("Starting to move block")
                        val ice: FallingBlock = player.world.spawnFallingBlock(location, Material.LODESTONE.createBlockData())
                        player.sendMessage("Spawned falling block")
                        ice.dropItem = false
                        ice.setGravity(false)
                        ice.isInvulnerable = true
                        ice.isSilent = true
                        ice.isPersistent = true
                        ice.setHurtEntities(false)
                        ice.velocity = Vector(0f, 0.1f, 0f)
                        ice.velocity = ice.velocity.multiply(1)
                        object : BukkitRunnable() {
                            override fun run() {
                                object : BukkitRunnable() {
                                    override fun run() {
                                        ice.velocity = Vector(0f, 0f, 0f)
                                        val newLocation = ice.location
                                        newLocation.y = newLocation.y + 1
                                        world.getBlockAt(newLocation).type = Material.LODESTONE
                                        ice.remove()
                                    }
                                }.runTask(TerrariaTeleporter.instance);
                            }
                        }.runTaskLaterAsynchronously(TerrariaTeleporter.instance, 10)
                    }
                }.runTask(TerrariaTeleporter.instance);

            }
        }.runTaskAsynchronously(this)
    }


    companion object {
        var running = true
        var waystonePosition = waystonePosition()
        lateinit var instance: TerrariaTeleporter
            private set

        fun spawnFloatingSand(location: Location) {
            val world: World = location.world ?: return
            val fallingBlock = world.spawnFallingBlock(location, Material.LAVA.createBlockData())
            fallingBlock.setGravity(false)
            fallingBlock.shouldAutoExpire(false)
            fallingBlock.dropItem = false
            fallingBlock.setHurtEntities(false)
            fallingBlock.isInvulnerable = true
            fallingBlock.isSilent = true
            fallingBlock.isPersistent = true
            fallingBlock.velocity = fallingBlock.velocity.multiply(1)
            fallingBlock.isVisibleByDefault = false
        }
    }
}