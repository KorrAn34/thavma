package me.alegian.thavma.impl.common.book

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.alegian.thavma.impl.common.research.ResearchEntry
import me.alegian.thavma.impl.init.registries.T7DatapackRegistries
import net.minecraft.Util
import net.minecraft.resources.ResourceKey

class TranslationPlaceholder(val text: String) {
  companion object {
    val CODEC = RecordCodecBuilder.create {
      it.group(
        Codec.STRING.fieldOf("text").forGetter(TranslationPlaceholder::text)
      ).apply(it, ::TranslationPlaceholder)
    }

    fun placeholderId(key: ResourceKey<ResearchEntry>) =
      Util.makeDescriptionId(T7DatapackRegistries.TRANSLATION_PLACEHOLDER.location().path, key.location())
  }
}