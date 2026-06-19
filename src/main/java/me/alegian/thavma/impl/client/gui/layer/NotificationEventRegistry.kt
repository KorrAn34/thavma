package me.alegian.thavma.impl.client.gui.layer

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

// Server-side registry mapping event IDs to conditional notification producers.
// Register entries during mod init before any client can connect.
object NotificationEventRegistry {

  data class NotifySpec(
    val text: Component,
    val color: Int,
    val image: ResourceLocation? = null,
    val scale: Float = PlayerNotifications.FONT_SIZE_PRIO
  )

  // Producer returns null to silently suppress the notification (e.g. player
  // hasn't unlocked the relevant content yet — no error sent back to client)
  private val registry = mutableMapOf<ResourceLocation, (ServerPlayer) -> NotifySpec?>()

  fun register(id: ResourceLocation, producer: (ServerPlayer) -> NotifySpec?) {
    check(id !in registry) { "Duplicate notification event ID: $id" }
    registry[id] = producer
  }

  // Internal — called only from the server packet handler
  internal fun resolve(id: ResourceLocation, player: ServerPlayer): NotifySpec? =
    registry[id]?.invoke(player)
}