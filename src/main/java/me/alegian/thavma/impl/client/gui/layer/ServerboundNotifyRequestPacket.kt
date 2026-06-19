package me.alegian.thavma.impl.client.gui.layer

import io.netty.buffer.ByteBuf
import me.alegian.thavma.impl.Thavma
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class ServerboundNotifyRequestPacket(
  val eventId: ResourceLocation
) : CustomPacketPayload {

  companion object {
    val TYPE = CustomPacketPayload.Type<ServerboundNotifyRequestPacket>(
      ResourceLocation.fromNamespaceAndPath(Thavma.MODID, "notify_request")
    )
    val STREAM_CODEC: StreamCodec<ByteBuf, ServerboundNotifyRequestPacket> =
      StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        ServerboundNotifyRequestPacket::eventId,
        ::ServerboundNotifyRequestPacket
      )
  }

  override fun type() = TYPE
}