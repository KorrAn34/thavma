package me.alegian.thavma.impl.client.gui.layer

import me.alegian.thavma.impl.Thavma
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.*


data class ShowNotificationPacket(
  val text: Component,
  val color: Int,
  val image: ResourceLocation?,
  val scale: Float
) : CustomPacketPayload {

  companion object {
    val TYPE = CustomPacketPayload.Type<ShowNotificationPacket>(
      ResourceLocation.fromNamespaceAndPath(Thavma.MODID, "show_notification")
    )

    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ShowNotificationPacket> =
      StreamCodec.composite(
        ComponentSerialization.TRUSTED_STREAM_CODEC, ShowNotificationPacket::text,
        ByteBufCodecs.INT, ShowNotificationPacket::color,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC)
          .map({ it.orElse(null) }, { Optional.ofNullable(it) }),
        ShowNotificationPacket::image,
        ByteBufCodecs.FLOAT, ShowNotificationPacket::scale,
        ::ShowNotificationPacket
      )
  }

  override fun type() = TYPE
}