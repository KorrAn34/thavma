package me.alegian.thavma.impl.client.gui.layer

import io.netty.buffer.ByteBuf
import me.alegian.thavma.impl.Thavma
import me.alegian.thavma.impl.init.registries.deferred.T7Attachments
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.handling.IPayloadContext

// Client → Server request
object RequestHistoryPacket : CustomPacketPayload {
  val TYPE = CustomPacketPayload.Type<RequestHistoryPacket>(
    ResourceLocation.fromNamespaceAndPath(Thavma.MODID, "request_history")
  )
  val STREAM_CODEC: StreamCodec<ByteBuf, RequestHistoryPacket> =
    StreamCodec.unit(RequestHistoryPacket)

  override fun type() = TYPE

  // Server handler for RequestHistoryPacket
  fun handleHistoryRequest(packet: RequestHistoryPacket, context: IPayloadContext) {
    val serverPlayer = context.player() as? ServerPlayer ?: return
    context.enqueueWork {
      val history = serverPlayer.getData(T7Attachments.NOTIFICATION_HISTORY)
      PacketDistributor.sendToPlayer(serverPlayer, HistoryResponsePacket(history.entries.toList()))
    }
  }

  // Server → Client response
  data class HistoryResponsePacket(val entries: List<NotificationHistory.Entry>) : CustomPacketPayload {

    companion object {
      val TYPE = CustomPacketPayload.Type<HistoryResponsePacket>(
        ResourceLocation.fromNamespaceAndPath(Thavma.MODID, "history_response")
      )

      // Entry.STREAM_CODEC now exists, so .apply(ByteBufCodecs.list()) resolves,
      // the getter and factory types are unambiguous, and composite can infer B
      val STREAM_CODEC: StreamCodec<ByteBuf, HistoryResponsePacket> =
        StreamCodec.composite(
          NotificationHistory.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
          HistoryResponsePacket::entries,
          ::HistoryResponsePacket
        )

      fun handleHistoryResponse(packet: HistoryResponsePacket, context: IPayloadContext) {
        context.enqueueWork {
          ClientNotificationHistory.update(packet.entries)
        }
      }
    }

    override fun type(): CustomPacketPayload.Type<HistoryResponsePacket> = TYPE

  }


}