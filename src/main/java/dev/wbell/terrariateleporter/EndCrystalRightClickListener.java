package dev.wbell.terrariateleporter;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Array;
import java.util.List;


public class EndCrystalRightClickListener implements Listener {
    public static Material[] waystoneBlocks = new Material[]{
            Material.DEEPSLATE_BRICK_WALL,
            Material.LODESTONE,
            Material.CRYING_OBSIDIAN,
            Material.DEEPSLATE_BRICK_SLAB,
            Material.AIR
    };

    public static JavaPlugin owningPluginInstance  = null;

    private void blockBreak(Block block) {
        boolean pass = false;
        for (Material material : waystoneBlocks) {
            if (block.getType() == material) {
                pass = true;
                break;
            }
        }
        if (!pass)
            return;
        Location location = block.getLocation();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        PositionData position = waystonePosition.waystoneNear(new PositionData(x, y, z));
        if (position != null) {
            waystonePosition.removeWaystone(position);
            Location strikeLocation = new Location(location.getWorld(), position.getX() + 0.5, position.getY() + 2, position.getZ() + 0.5);
            strikeLocation.getWorld().strikeLightningEffect(strikeLocation);
            {
                for (Player selectPlayer : location.getWorld().getPlayers()) {
                    if (selectPlayer.getLocation().distance(location) <= 50) {
                        selectPlayer.playSound(location, Sound.ENTITY_WARDEN_DEATH, 1.0f, 1.0f);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        blockBreak(event.getBlock());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> blocks = event.blockList();
        for (Block block : blocks) {
            blockBreak(block);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        PositionData position = waystonePosition.waystoneNear(new PositionData(x, y, z));
        if (position != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamagebyEntityEvent(org.bukkit.event.entity.EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Firework) {
            Firework fw = (Firework) e.getDamager();
            if (fw.hasMetadata("nodamage")) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null)
            return;
        Location location = block.getLocation();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        if (!event.getAction().toString().contains("RIGHT_CLICK"))
            return;

        if (waystonePosition.waystoneExists(new PositionData(x, y, z))) {
            List<PositionData> positions = waystonePosition.getAllPositionNotIncluding(new PositionData(x, y, z));
            if (positions.isEmpty()) {
                player.sendMessage("No other waystones exist!");
                return;
            }
            PositionData position = positions.get((int) (Math.random() * positions.size()));
            Location TeleportLocation = new Location(location.getWorld(), position.getX() + 0.5, position.getY(), position.getZ() -0.5);
            Location EffectLocation = new Location(location.getWorld(), position.getX() + 0.5, position.getY() + 2, position.getZ() + 0.5);
            player.teleport(TeleportLocation);
            World world = EffectLocation.getWorld();
            world.strikeLightningEffect(EffectLocation);
            Firework firework = world.spawn(EffectLocation, Firework.class);

            // Create firework meta
            FireworkMeta fireworkMeta = firework.getFireworkMeta();

            // Create a firework effect with a purple color
            FireworkEffect effect = FireworkEffect.builder()
                    .flicker(true)
                    .trail(true)
                    .withColor(Color.PURPLE)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .build();

            // Add the effect to the firework
            fireworkMeta.addEffect(effect);

            // Set the firework meta and detonate it immediately
            firework.setFireworkMeta(fireworkMeta);
            firework.setMetadata("nodamage", new FixedMetadataValue(owningPluginInstance, true));

            firework.detonate();

            player.playSound(EffectLocation, Sound.ENTITY_WARDEN_ROAR, 1.0f, 1.0f);
            return;
        }
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.getType() != Material.END_CRYSTAL)
            return;
        if (block.getType() != Material.DEEPSLATE_BRICK_WALL)
            return;
        {
            Block blockAbove = block.getWorld().getBlockAt(x, y + 1, z);
            if (blockAbove.getType() != Material.LODESTONE)
                return;
        }
        {
            Block blockBelow = block.getWorld().getBlockAt(x, y - 1, z);
            if (blockBelow.getType() != Material.LODESTONE)
                return;
        }
        {
            // crying obsidian base
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 2 - i; j++) {
                    Block cryingObsidian = block.getWorld().getBlockAt(x - 1 + i, y - 2, z - 1 + j);
                    if (cryingObsidian.getType() != Material.CRYING_OBSIDIAN)
                        return;
                }
            }
        }
        {
            // deepslate brick slab bottom 4 around bottom lodestone
            Block block1 = block.getWorld().getBlockAt(x - 1, y - 1, z);
            if (block1.getType() != Material.DEEPSLATE_BRICK_SLAB) {
                return;
            }
            Slab slab1 = (Slab) block1.getBlockData();
            if (slab1.getType() != Slab.Type.BOTTOM)
                return;
            Block block2 = block.getWorld().getBlockAt(x + 1, y - 1, z);
            if (block2.getType() != Material.DEEPSLATE_BRICK_SLAB)
                return;
            Slab slab2 = (Slab) block2.getBlockData();
            if (slab2.getType() != Slab.Type.BOTTOM)
                return;
            Block block3 = block.getWorld().getBlockAt(x, y - 1, z - 1);
            if (block3.getType() != Material.DEEPSLATE_BRICK_SLAB)
                return;
            Slab slab3 = (Slab) block3.getBlockData();
            if (slab3.getType() != Slab.Type.BOTTOM)
                return;
            Block block4 = block.getWorld().getBlockAt(x, y - 1, z + 1);
            if (block4.getType() != Material.DEEPSLATE_BRICK_SLAB)
                return;
            Slab slab4 = (Slab) block4.getBlockData();
            if (slab4.getType() != Slab.Type.BOTTOM)
                return;
        }
        {
            // check to make sure gaps around are air gaps
            for (int height = -1; height < 3; height++) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (i == 1 && j == 1 && height >= 0 || (i == 0 && j == 1 || i == 1 && j == 0 || i == 1 && j == 2 || i == 2 && j == 1) && height == 2)
                            continue;
                        Block air = block.getWorld().getBlockAt(x - 1 + i, y + 1 - height, z - 1 + j);
                        if (air.getType() != Material.AIR)
                            return;
                    }
                }
            }

        }


        if (player.getGameMode() == GameMode.SURVIVAL) {
            if (heldItem.getAmount() > 1) {
                heldItem.setAmount(heldItem.getAmount() - 1);
            } else {
                player.getInventory().removeItem(heldItem);
            }
        }
        waystonePosition.addWaystone(new PositionData(x, y, z));
        World world = location.getWorld();
        Location EffectLocation = new Location(location.getWorld(), x + 0.5, y + 2, z + 0.5);
        world.strikeLightningEffect(EffectLocation);

        Firework firework = world.spawn(EffectLocation, Firework.class);

        // Create firework meta
        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        // Create a firework effect with a purple color
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .withColor(Color.PURPLE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .build();

        // Add the effect to the firework
        fireworkMeta.addEffect(effect);

        // Set the firework meta and detonate it immediately
        firework.setFireworkMeta(fireworkMeta);
        firework.setMetadata("nodamage", new FixedMetadataValue(owningPluginInstance, true));

        firework.detonate();
        {
            for (Player selectPlayer : location.getWorld().getPlayers()) {
                if (selectPlayer.getLocation().distance(location) <= 50) {
                    selectPlayer.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f);
                }
            }
        }
    }
}