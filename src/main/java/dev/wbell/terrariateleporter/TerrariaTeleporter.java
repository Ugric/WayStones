package dev.wbell.terrariateleporter;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class TerrariaTeleporter extends JavaPlugin {
    public static waystonePosition waystonePosition = new waystonePosition();

    @Override
    public void onEnable() {
        // Plugin startup logic
        EndCrystalRightClickListener endCrystalRightClickListener = new EndCrystalRightClickListener();
        EndCrystalRightClickListener.owningPluginInstance = this;
        getServer().getPluginManager().registerEvents(endCrystalRightClickListener, this);
        String folderPath = getDataFolder().getAbsolutePath();
        // make all the folders
        boolean wasSuccessful = new File(folderPath).mkdirs();
        if (!wasSuccessful) {
            getLogger().warning("Failed to create plugin folder");
        }
        dev.wbell.terrariateleporter.waystonePosition.loadPositions(new File(folderPath, "waystones.json"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}