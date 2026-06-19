package me.alegian.thavma.impl.client.gui.layer

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.util.*

// NotificationHistory.kt
class NotificationHistory {

  val entries: MutableList<Entry> = mutableListOf()

  data class Entry(
    val text: String,       // component.getString() — plain text
    val color: Int,
    val image: String?,     // ResourceLocation.toString() or null
    val gameTime: Long,
    val realTime: Long
  ) {
    companion object {
      val STREAM_CODEC: StreamCodec<ByteBuf, Entry> = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        Entry::text,
        ByteBufCodecs.INT,
        Entry::color,
        ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8)
          .map({ it.orElse(null) }, { Optional.ofNullable(it) }),
        Entry::image,
        ByteBufCodecs.VAR_LONG,
        Entry::gameTime,
        ByteBufCodecs.VAR_LONG,
        Entry::realTime
      ) { text, color, image, gameTime, realTime ->
        Entry(text, color, image, gameTime, realTime)
      }
    }
  }

  fun addEntry(entry: Entry) {
    entries.add(0, entry)   // newest first
    if (entries.size > MAX_ENTRIES) entries.removeAt(entries.lastIndex)
  }

  companion object {
    const val MAX_ENTRIES = Int.MAX_VALUE - 2

    val ENTRY_CODEC: Codec<Entry> = RecordCodecBuilder.create { i ->
      i.group(
        Codec.STRING.fieldOf("text").forGetter(Entry::text),
        Codec.INT.fieldOf("color").forGetter(Entry::color),
        Codec.STRING.optionalFieldOf("image")
          .forGetter { Optional.ofNullable(it.image) },
        Codec.LONG.fieldOf("game_time").forGetter(Entry::gameTime),
        Codec.LONG.fieldOf("real_time").forGetter(Entry::realTime)
      ).apply(i) { text, color, image, gameTime, realTime ->
        Entry(text, color, image.orElse(null), gameTime, realTime)
      }
    }

    // Used by the attachment type — NeoForge applies RegistryOps automatically
    // when serializing player data, so this is safe server-side
    val CODEC: Codec<NotificationHistory> = ENTRY_CODEC.listOf().xmap(
      { list -> NotificationHistory().also { h -> list.forEach(h::addEntry) } },
      { history -> history.entries.toList() }
    )


  }
}