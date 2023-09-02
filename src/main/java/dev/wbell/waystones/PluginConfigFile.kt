package dev.wbell.waystones

import java.io.File

class PluginConfigFile {
    companion object {
        fun loadConfig(config: org.bukkit.configuration.file.FileConfiguration, file: File): org.bukkit.configuration.file.FileConfiguration {
            // load file if exists
            if (file.exists()) {
                config.load(file)
            }
            config.addDefault("holograms", false)
            config.addDefault("require-permissions", true)
            config.addDefault("lightning-on-travel", true)
            config.addDefault("lightning-at-travel-place", true)
            config.addDefault("lightning-on-creation", true)
            config.addDefault("lightning-on-destruction", true)
            config.addDefault("explosion-on-travel", true)
            config.addDefault("explosion-at-travel-place", true)
            config.addDefault("explosion-on-creation", true)
            config.addDefault("explosion-on-destruction", true)
            config.addDefault("ambiant-sound", "BLOCK_PORTAL_AMBIENT")
            config.addDefault("ambiant-particles", "PORTAL")

            config.options().copyDefaults(true)

            saveConfig(config, file)
            config.load(file)

            // add comments

            config.setComments("holograms", MutableList(1) { "enable holograms (requires DecentHolograms)" })
            config.setComments("require-permissions", MutableList(1) { "require permissions to use waystones" })
            config.setComments("lightning-on-travel", MutableList(1) { "strike lightning on teleported location" })
            config.setComments("lightning-at-travel-place", MutableList(1) { "strike lightning where the player teleported from" })
            config.setComments("lightning-on-creation", MutableList(1) { "strike lightning when a waystone is created" })
            config.setComments("lightning-on-destruction", MutableList(1) { "strike lightning when a waystone is destroyed" })
            config.setComments("explosion-on-travel", MutableList(1) { "create explosion on teleported location" })
            config.setComments("explosion-at-travel-place", MutableList(1) { "create explosion where the player teleported from" })
            config.setComments("explosion-on-creation", MutableList(1) { "create explosion when a waystone is created" })
            config.setComments("explosion-on-destruction", MutableList(1) { "create explosion when a waystone is destroyed" })
            config.setComments("ambiant-sound", MutableList(1) { "the ambiant sound that is played to a player when they are nearby" })
            config.setComments("ambiant-particles", MutableList(1) { "the ambiant particles that are made when a player is nearby" })

            saveConfig(config, file)
            return config
        }

        private fun saveConfig(config: org.bukkit.configuration.file.FileConfiguration, file: File) {
            config.save(file)
        }
    }
}