    package main.java.dev.wbell.waystones

    import dev.wbell.waystones.*
    import net.kyori.adventure.text.Component
    import net.kyori.adventure.text.format.NamedTextColor
    import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
    import org.bukkit.*
    import org.bukkit.block.Block
    import org.bukkit.block.data.type.Slab
    import org.bukkit.entity.Firework
    import org.bukkit.entity.Player
    import org.bukkit.event.EventHandler
    import org.bukkit.event.Listener
    import org.bukkit.event.block.BlockBreakEvent
    import org.bukkit.event.block.BlockPlaceEvent
    import org.bukkit.event.entity.EntityDamageByEntityEvent
    import org.bukkit.event.entity.EntityExplodeEvent
    import org.bukkit.event.inventory.InventoryClickEvent
    import org.bukkit.event.inventory.InventoryDragEvent
    import org.bukkit.event.player.PlayerInteractEvent
    import org.bukkit.event.server.PluginEnableEvent
    import org.bukkit.inventory.Inventory
    import org.bukkit.inventory.ItemStack
    import org.bukkit.metadata.FixedMetadataValue
    import org.bukkit.potion.PotionEffect
    import org.bukkit.potion.PotionEffectType
    import java.util.*
    import kotlin.concurrent.thread
    import kotlin.math.pow
    import kotlin.math.sqrt


    class EndCrystalRightClickListener : Listener {
        private val teleportingPlayers = HashSet<UUID>()


        @EventHandler
        fun onPluginEnable(event: PluginEnableEvent) {
            if (!WayStones.holograms) return
            val enabledPlugin = event.plugin
            if (enabledPlugin.name == "FancyHolograms") {
                FancyHolograms.runEnable()
            }
        }

        fun playerNearbyHandler() {
            if (WayStones.ambiantSound != null) {
                thread {
                    while (WayStones.running) {
                        for (waystone in WaystonePosition.positions) {
                            val world = Bukkit.getWorld(waystone.pos.world)
                            if (world == null) {
                                WaystonePosition.removeWaystone(waystone.pos)
                                continue
                            }
                            val location = Location(world, waystone.pos.x + 0.5, waystone.pos.y + 0.5, waystone.pos.z + 0.5)
                            for (player in world.players) {
                                val distance = player.location.distance(location)
                                if (distance <= 10) {
                                    val pitch = (Math.random() / 2).toFloat()
                                    val speed = (Math.random() / 2).toFloat()
                                    player.playSound(location, WayStones.ambiantSound!!, speed, pitch)
                                }
                            }
                        }
                        Thread.sleep(((Math.random() * 5000) + 5000).toLong())
                    }
                }
            }
            if (WayStones.ambiantParticles != null) {
                thread {
                    while (WayStones.running) {
                        for (waystone in WaystonePosition.positions) {
                            val world = Bukkit.getWorld(waystone.pos.world)
                            if (world == null) {
                                WaystonePosition.removeWaystone(waystone.pos)
                                continue
                            }
                            val location = Location(world, waystone.pos.x + 0.5, waystone.pos.y + 0.5, waystone.pos.z + 0.5)
                            var playerNearby = false
                            for (player in world.players) {
                                val distance = player.location.distance(location)
                                if (distance <= 15) {
                                    playerNearby = true
                                    break
                                }
                            }
                            if (!playerNearby) continue

                            world.spawnParticle(WayStones.ambiantParticles!!, location, 100, 0.5, 0.5, 0.5)
                        }
                        Thread.sleep(((Math.random() * 500) + 1000).toLong())
                    }
                }
            }
        }

        private fun blockBreak(block: Block) {
            var pass = false
            for (material in waystoneBlocks) {
                if (block.type == material) {
                    pass = true
                    break
                }
            }
            if (!pass) return
            val location = block.location
            val x = block.x
            val y = block.y
            val z = block.z
            val position =
                WaystonePosition.waystoneNear(PositionData(x.toDouble(), y.toDouble(), z.toDouble(), location.world.name))
            if (position != null) {
                WaystonePosition.removeWaystone(position.pos)
                val strikeLocation = Location(location.world, position.pos.x + 0.5, position.pos.y + 2, position.pos.z + 0.5)
                if (WayStones.instance.config.getBoolean("lightning-on-destruction")) strikeLocation.world.strikeLightningEffect(strikeLocation)
                if (WayStones.instance.config.getBoolean("explosion-on-destruction")) {
                    val firework = strikeLocation.world.spawn(strikeLocation, Firework::class.java)

                    // Create firework meta
                    val fireworkMeta = firework.fireworkMeta

                    // Create a firework effect with a purple color
                    val effect = FireworkEffect.builder().flicker(true).trail(true).withColor(Color.PURPLE).with(FireworkEffect.Type.BALL_LARGE).build()

                    // Add the effect to the firework
                    fireworkMeta.addEffect(effect)

                    // Set the firework meta and detonate it immediately
                    firework.fireworkMeta = fireworkMeta
                    firework.setMetadata("nodamage", FixedMetadataValue(WayStones.instance, true))
                    firework.detonate()
                }
                for (selectPlayer in location.world.players) {
                    if (selectPlayer.location.distance(location) <= 50) {
                        selectPlayer.playSound(location, Sound.ENTITY_WARDEN_DEATH, 1.0f, 1.0f)
                    }
                }
            }
        }
        @EventHandler
        fun onInventoryDrag(event: InventoryDragEvent) {
            val clickedInventory = event.inventory
            if (clickedInventory.holder !is ChestGUIHolder) return
            val player = event.whoClicked as Player

            if (teleportingPlayers.contains(player.uniqueId)) {
                // Cancel the inventory drag event if the player is teleporting
                event.isCancelled = true
            }
        }

        @EventHandler
        fun onInventoryClick(event: InventoryClickEvent) {
            val clickedInventory = event.clickedInventory ?: return
            if (clickedInventory.holder !is ChestGUIHolder) return
            event.isCancelled = true
            val player = event.whoClicked as Player
            val holder = clickedInventory.holder as ChestGUIHolder
            val clickedSlot = event.slot

            if (teleportingPlayers.contains(player.uniqueId)) {
                player.closeInventory()
                player.sendMessage("${ChatColor.RED}คุณกำลังอยู่ในการวาร์ป")
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f)
                return
            }
            // Check if the clicked slot is empty
            if (clickedSlot >= clickedInventory.size || clickedInventory.getItem(clickedSlot) == null) {
                return
            }

            if (event.slot == 45 && holder.page > 0) {
                // Previous page button clicked
                openChestGUI(player, holder.positions, holder.positions[0], holder.page - 1)
                return
            }

            if (event.slot == 53 && holder.page < holder.positions.size / 45) {
                // Next page button clicked
                openChestGUI(player, holder.positions, holder.positions[0], holder.page + 1)
                return
            }

    // Check if the player has the item
            if (!player.inventory.contains(Material.ENDER_EYE)) {
                player.closeInventory()
                player.sendMessage("${ChatColor.YELLOW}คุณต้องมี ENDER_EYE เพื่อใช้งานเสาวาร์ป")
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f)
                return
            }

    // Consume the item
            player.inventory.removeItem(ItemStack(Material.ENDER_EYE, 1))
            player.sendMessage("${ChatColor.GREEN}ใช้ 1 ENDER_EYE แล้วในการวาร์ป")
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_FLUTE, 1.0f, 1.0f)
            player.closeInventory()


            val positions = holder.positions
            if (clickedSlot >= positions.size) return
            val position = positions[holder.page * 45 + clickedSlot] // Calculate the correct position based on the page
            val teleportLocation = Location(Bukkit.getWorld(position.pos.world), position.pos.x + 0.5, position.pos.y, position.pos.z - 0.5)
            val effectLocation = Location(Bukkit.getWorld(position.pos.world), position.pos.x + 0.5, position.pos.y + 2, position.pos.z + 0.5)


            // Add the player to the teleporting set
            teleportingPlayers.add(player.uniqueId)

            // Delay the teleport
            Scheduler.runTaskForEntity(player, WayStones.instance, Runnable {
                if (WayStones.instance.config.getBoolean("lightning-at-travelled-from-place")) player.world.strikeLightningEffect(player.location)
                player.teleportAsync(teleportLocation).thenRun {
                    val world = effectLocation.world
                    if (WayStones.instance.config.getBoolean("lightning-on-travel")) world.strikeLightningEffect(effectLocation)
                    if (WayStones.instance.config.getBoolean("explosion-on-travel")) {
                        val firework = world.spawn(effectLocation, Firework::class.java)

                        // Create firework meta
                        val fireworkMeta = firework.fireworkMeta

                        // Create a firework effect with a purple color
                        val effect = FireworkEffect.builder().flicker(true).trail(true).withColor(Color.PURPLE).with(FireworkEffect.Type.BALL_LARGE).build()

                        // Add the effect to the firework
                        fireworkMeta.addEffect(effect)

                        // Set the firework meta and detonate it immediately
                        firework.fireworkMeta = fireworkMeta
                        firework.setMetadata("nodamage", FixedMetadataValue(WayStones.instance, true))
                        firework.detonate()

                        //effect to player
                        val glowing = PotionEffectType.GLOWING
                        val effectDarkness = PotionEffectType.DARKNESS
                        player.addPotionEffect(PotionEffect(glowing, 60, 0, true))
                        player.addPotionEffect(PotionEffect(effectDarkness, 60, 0, true))
                    }
                    player.playSound(effectLocation, Sound.ENTITY_WARDEN_ROAR, 1.0f, 1.0f)

                    // Remove the player from the teleporting set after teleportation is done
                    teleportingPlayers.remove(player.uniqueId)
                }
            }, 60L) // Change 60L to adjust the delay in ticks. 20 ticks = 1 second
        }

        private fun openChestGUI(player: Player, positions: List<WayStoneData>, position: WayStoneData, page: Int) {
            val holder = ChestGUIHolder()
            holder.positions.addAll(positions)
            holder.page = page // Set the current page

            val startIndex = page * 45
            val endIndex = Math.min(startIndex + 45, positions.size)
            val inventory = Bukkit.createInventory(holder, 54, LegacyComponentSerializer.legacyAmpersand().deserialize("WS - หน้าที่ $page").color(NamedTextColor.AQUA))

            for (i in startIndex until endIndex) {
                val indexInInventory = i - startIndex
                val item = ItemStack(positions[i].rngBlock ?: Material.END_CRYSTAL) // Use the rngBlock, default to END_CRYSTAL
                val meta = item.itemMeta
                var lore: MutableList<Component>? = meta?.lore() ?: ArrayList()
                if (lore == null) {
                    lore = ArrayList()
                }
                lore.add(Component.text(" "))
                if (positions[i].owner != null) {
                    lore.add(Component.text("สร้างโดย: ${Bukkit.getOfflinePlayer(UUID.fromString(positions[i].owner)).name}").color(NamedTextColor.GRAY))
                }
                if (positions[i].pos.world != position.pos.world) {
                    val worldColor = when(positions[i].pos.world) {
                        "world" -> NamedTextColor.GREEN
                        "world_nether" -> NamedTextColor.RED
                        "world_the_end" -> NamedTextColor.LIGHT_PURPLE
                        else -> NamedTextColor.GRAY // default color if neither of the above
                    }
                    lore.add(Component.text("โลก: ${positions[i].pos.world} (${positions[i].pos.x.toInt()}, ${positions[i].pos.y.toInt()}, ${positions[i].pos.z.toInt()})").color(worldColor))
                } else {
                    // Calculate distance
                    val distance = distance(position.pos, positions[i].pos).toInt()
                    lore.add(Component.text("ระยะ: $distance บล็อก (${positions[i].pos.x.toInt()}, ${positions[i].pos.y.toInt()}, ${positions[i].pos.z.toInt()})").color(NamedTextColor.AQUA))
                }
                meta.lore(lore)
                meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("${positions[i].name}").color(NamedTextColor.AQUA))
                item.itemMeta = meta
                inventory.setItem(indexInInventory, item)
            }

            // Add pagination buttons if needed
            if (page > 0) {
                // Add a previous page button
                val prevPageItem = ItemStack(Material.ARROW)
                val prevPageMeta = prevPageItem.itemMeta
                prevPageMeta.displayName(Component.text("หน้าก่อนหน้านี้").color(NamedTextColor.RED))
                prevPageItem.itemMeta = prevPageMeta
                inventory.setItem(45, prevPageItem)
            }

            if (endIndex < positions.size) {
                // Add a next page button
                val nextPageItem = ItemStack(Material.ARROW)
                val nextPageMeta = nextPageItem.itemMeta
                nextPageMeta.displayName(Component.text("หน้าุถัดไป").color(NamedTextColor.GREEN))
                nextPageItem.itemMeta = nextPageMeta
                inventory.setItem(53, nextPageItem)
            }

            player.openInventory(inventory)
        }


        private class ChestGUIHolder : org.bukkit.inventory.InventoryHolder {
            public val positions = ArrayList<WayStoneData>()
            var page = 0 // Add a variable to track the current page
            override fun getInventory(): Inventory {
                return inventory
            }
        }


        @EventHandler
        fun onBlockBreak(event: BlockBreakEvent) {
            blockBreak(event.block)
        }

        @EventHandler
        fun onEntityExplode(event: EntityExplodeEvent) {
            val blocks = event.blockList()
            for (block in blocks) {
                blockBreak(block)
            }
        }

        @EventHandler
        fun onBlockPlace(e: BlockPlaceEvent) {
            val block = e.block
            val x = block.x
            val y = block.y
            val z = block.z
            val position =
                WaystonePosition.waystoneNear(PositionData(x.toDouble(), y.toDouble(), z.toDouble(), block.world.name))
            if (position != null) e.isCancelled = true
        }

        @EventHandler
        fun onEntityDamagebyEntityEvent(e: EntityDamageByEntityEvent) {
            if (e.damager is Firework) {
                val fw = e.damager as Firework
                if (fw.hasMetadata("nodamage")) {
                    e.isCancelled = true
                }
            }
        }

        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            val player = event.player
            val block = event.clickedBlock ?: return
            val location = block.location
            val x = block.x
            val y = block.y
            val z = block.z
            if (!event.action.toString().contains("RIGHT_CLICK")) return

            val currentWaystone =
                WaystonePosition.waystoneExists(PositionData(x.toDouble(), y.toDouble(), z.toDouble(), location.world.name))
            if (currentWaystone != null) {
                val positions = WaystonePosition.getAllPositionNotIncluding(
                    PositionData(
                        x.toDouble(),
                        y.toDouble(),
                        z.toDouble(),
                        location.world.name
                    )
                )

                if (WayStones.instance.config.getBoolean("usage-permissions") && !player.hasPermission("waystones.use")) return // Simple permissions check
                openChestGUI(player, positions, currentWaystone,0)
                return
            }
            if (WayStones.instance.config.getBoolean("creation-permissions") && !player.hasPermission("waystones.create")) return // Simple permissions check

            val heldItem = player.inventory.itemInMainHand
            if (heldItem.type != Material.END_CRYSTAL) return
            if (block.type != Material.DEEPSLATE_BRICK_WALL) return
            run {
                val blockAbove = block.world.getBlockAt(x, y + 1, z)
                if (blockAbove.type != Material.LODESTONE) return
            }
            run {
                val blockBelow = block.world.getBlockAt(x, y - 1, z)
                if (blockBelow.type != Material.LODESTONE) return
            }
            run {
                // crying obsidian base
                for (i in 0..2) {
                    for (j in 0..2) {
                        val cryingObsidian = block.world.getBlockAt(x - 1 + i, y - 2, z - 1 + j)
                        if (cryingObsidian.type != Material.CRYING_OBSIDIAN) return
                    }
                }
            }
            run {

                // deepslate brick slab bottom 4 around bottom lodestone
                val block1 = block.world.getBlockAt(x - 1, y - 1, z)
                if (block1.type != Material.DEEPSLATE_BRICK_SLAB) return
                val slab1 = block1.blockData as Slab
                if (slab1.type != Slab.Type.BOTTOM) return
                val block2 = block.world.getBlockAt(x + 1, y - 1, z)
                if (block2.type != Material.DEEPSLATE_BRICK_SLAB) return
                val slab2 = block2.blockData as Slab
                if (slab2.type != Slab.Type.BOTTOM) return
                val block3 = block.world.getBlockAt(x, y - 1, z - 1)
                if (block3.type != Material.DEEPSLATE_BRICK_SLAB) return
                val slab3 = block3.blockData as Slab
                if (slab3.type != Slab.Type.BOTTOM) return
                val block4 = block.world.getBlockAt(x, y - 1, z + 1)
                if (block4.type != Material.DEEPSLATE_BRICK_SLAB) return
                val slab4 = block4.blockData as Slab
                if (slab4.type != Slab.Type.BOTTOM) return
            }
            // check to make sure gaps around are air gaps
            for (height in 0..2) {
                for (i in 0..2) {
                    for (j in 0..2) {
                        if (i == 1 && j == 1 || (i == 0 && j == 1 || i == 1 && j == 0 || i == 1 || i == 2 && j == 1) && height == 2) continue
                        val air = block.world.getBlockAt(x - 1 + i, y + 1 - height, z - 1 + j)
                        if (air.type != Material.AIR && air.type != Material.CAVE_AIR) return
                    }
                }
            }

            val heldItemMeta = heldItem.itemMeta

            var waystoneName = "จุด (${x}, ${y}, ${z})"
            if (heldItemMeta.hasDisplayName()) {
                val displayNameComponent: Component? = heldItemMeta.displayName()
                if (displayNameComponent != null) {
                    waystoneName = LegacyComponentSerializer.legacyAmpersand().serialize(displayNameComponent)
                }
            }

            if (player.gameMode == GameMode.SURVIVAL) {
                if (heldItem.amount > 1) {
                    heldItem.amount -= 1
                } else {
                    player.inventory.removeItem(heldItem)
                }
            }
            val randomBlocks = listOf(Material.DIAMOND, Material.GOLD_INGOT, Material.IRON_INGOT, Material.REDSTONE, Material.COAL, Material.COPPER_INGOT, Material.LAPIS_LAZULI, Material.EMERALD, Material.NETHERITE_INGOT, Material.QUARTZ)
            val randomBlock = randomBlocks[Random().nextInt(randomBlocks.size)]

            WaystonePosition.addWaystone(
                PositionData(x.toDouble(), y.toDouble(), z.toDouble(), location.world.name),
                waystoneName,
                player.uniqueId.toString(),
                rngBlock = randomBlock,
            )
            val world = location.world
            val effectLocation = Location(location.world, x + 0.5, (y + 2).toDouble(), z + 0.5)
            if (WayStones.instance.config.getBoolean("lightning-on-creation")) world.strikeLightningEffect(effectLocation)
            if (WayStones.instance.config.getBoolean("explosion-on-creation")) {
                val firework = world.spawn(effectLocation, Firework::class.java)

                // Create firework meta
                val fireworkMeta = firework.fireworkMeta

                // Create a firework effect with a purple color
                val effect = FireworkEffect.builder().flicker(true).trail(true).withColor(Color.PURPLE).with(FireworkEffect.Type.BALL_LARGE).build()

                // Add the effect to the firework
                fireworkMeta.addEffect(effect)

                // Set the firework meta and detonate it immediately
                firework.fireworkMeta = fireworkMeta
                firework.setMetadata("nodamage", FixedMetadataValue(WayStones.instance, true))
                firework.detonate()
            }
            for (selectPlayer in location.world.players) {
                if (selectPlayer.location.distance(location) <= 50) {
                    selectPlayer.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f)
                }
            }
        }

        companion object {
            var waystoneBlocks = arrayOf(Material.DEEPSLATE_BRICK_WALL, Material.LODESTONE, Material.CRYING_OBSIDIAN, Material.DEEPSLATE_BRICK_SLAB, Material.AIR, Material.CAVE_AIR)
            fun distance(pos1: PositionData, pos2: PositionData): Double {
                return sqrt((pos1.x - pos2.x).pow(2.0) + (pos1.y - pos2.y).pow(2.0) + (pos1.z - pos2.z).pow(2.0))
            }
        }
    }