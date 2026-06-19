package me.alegian.thavma.impl.client.gui.layer

import me.alegian.thavma.impl.client.gui.layer.PlayerNotifications.FONT_SIZE_PRIO
import me.alegian.thavma.impl.init.registries.deferred.T7Attachments
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor

// NotificationDispatcher.kt — server-side only, no @OnlyIn annotation needed
object NotificationDispatcher {

  fun send(
    serverPlayer: ServerPlayer,
    text: Component,
    color: Int = 0xFFFFFF,
    image: ResourceLocation? = null,
    scale: Float = FONT_SIZE_PRIO
  ) {
    // Record to history — saved automatically with player data
    serverPlayer.getData(T7Attachments.NOTIFICATION_HISTORY).addEntry(
      NotificationHistory.Entry(
        text = text.getString(),
        color = color,
        image = image?.toString(),
        gameTime = serverPlayer.level().gameTime,
        realTime = System.currentTimeMillis()
      )
    )

    // Trigger the client display
    PacketDistributor.sendToPlayer(
      serverPlayer, ShowNotificationPacket(
        text = text,
        color = color,
        image = image,
        scale = scale
      )
    )
  }
}