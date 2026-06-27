package me.alegian.thavma.impl.common.book

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.alegian.thavma.impl.init.registries.deferred.PageFeatureTypes
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization

class TitleFeature(
  val text: Component, override val mustStartPage: Boolean = false,
  override val forceIndex: Int? = null
) : PageFeature {

  override val type: PageFeatureType<*>
    get() = PageFeatureTypes.TITLE.get()

  override val coversOneWholePage = false

  override fun toString(): String {
    return "TitleFeature with text $text"
  }

  companion object {
    val CODEC = RecordCodecBuilder.mapCodec { builder ->
      builder.group(
        ComponentSerialization.CODEC.fieldOf("text").forGetter(TitleFeature::text),
        Codec.BOOL.optionalFieldOf("must_start_page", false).forGetter(TitleFeature::mustStartPage),
        Codec.INT.optionalFieldOf("force_index", null).forGetter(TitleFeature::forceIndex)
      ).apply(builder, ::TitleFeature)
    }

    fun translationId(baseId: String, featureIndex: Int) = "$baseId.title_feature$featureIndex"
  }
}