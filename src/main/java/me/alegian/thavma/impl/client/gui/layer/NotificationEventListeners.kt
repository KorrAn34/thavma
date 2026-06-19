package me.alegian.thavma.impl.client.gui.layer

import me.alegian.thavma.impl.Thavma
import me.alegian.thavma.impl.init.registries.deferred.T7Items
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import java.util.*

@EventBusSubscriber(modid = Thavma.MODID, bus = EventBusSubscriber.Bus.GAME)
object NotificationEventListeners {

  // ── XP level ───────────────────────────────────────────────────────────
  @SubscribeEvent
  fun onLevelUp(event: PlayerXpEvent.LevelChange) {
    val player = event.entity as? ServerPlayer ?: return
    val newLevel = player.experienceLevel + event.levels
    if (player.experienceLevel < 5 && newLevel >= 5) {
      NotificationDispatcher.send(
        player,
        Component.translatable("thavma.notif.level_5"),
        color = 0xFFD700
      )
    }
  }

  // ── Item pickup ────────────────────────────────────────────────────────
  @SubscribeEvent
  fun onItemPickup(event: ItemEntityPickupEvent) {
    val player = event.player as? ServerPlayer ?: return
    if (event.itemEntity == T7Items.BOOK.get()) {
      NotificationDispatcher.send(
        player,
        Component.translatable("thavma.notif.found_crystal"),
        color = 0x88CCFF,
        image = ResourceLocation.fromNamespaceAndPath(Thavma.MODID, "textures/item/aura_crystal.png")
      )
    }
  }

  // ── Coordinates and biome (polled) ─────────────────────────────────────
  // Track last known biome per player to detect transitions, not just presence
  private val lastBiome = mutableMapOf<UUID, ResourceKey<Biome>>()

  @SubscribeEvent
  fun onPlayerTick(event: PlayerTickEvent.Post) {
    val player = event.entity as? ServerPlayer ?: return

    // Coordinate check — every tick is fine for a precise location
    if (player.blockPosition() == BlockPos(0, 64, 0)) {
      NotificationDispatcher.send(
        player,
        Component.translatable("thavma.notif.origin"), 0xFFFFFF
      )
    }

    // Biome transition — poll once per second to avoid spam
    if (player.tickCount % 100 == 0) {
      val biomeKey = player.level()
        .getBiome(player.blockPosition())
        .unwrapKey().orElse(null) ?: return

      if (biomeKey != lastBiome[player.uuid]) {
        lastBiome[player.uuid] = biomeKey
        if (biomeKey == Biomes.SOUL_SAND_VALLEY) {
          NotificationDispatcher.send(
            player,
            Component.translatable("thavma.notif.soul_sand_valley"), 0xFF6633
          )
        }
      }
    }

    // Water contact — guard with persistent data to fire only on transition
    val wasInWater = player.persistentData.getBoolean("thavma.was_in_water")
    //player.isEyeInFluidType()
    if (player.isInWater && !wasInWater) {
      NotificationDispatcher.send(
        player,
        Component.translatable("thavma.notif.entered_water"), 0x4466CC
      )
    }
    player.persistentData.putBoolean("thavma.was_in_water", player.isInWater)
  }

  // ── Mob kill ───────────────────────────────────────────────────────────
  @SubscribeEvent
  fun onMobKill(event: LivingDeathEvent) {
    // Event fires on both sides — ServerPlayer cast is the server-side guard
    val killer = event.source.entity as? ServerPlayer ?: return
    if (event.entity is Zombie) {
      NotificationDispatcher.send(
        killer,
        Component.translatable("thavma.notif.zombie_slain"), 0x44AA44
      )
    }
  }
}