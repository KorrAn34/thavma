package me.alegian.thavma.impl.common.book

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.alegian.thavma.impl.client.texture.Texture
import me.alegian.thavma.impl.init.registries.deferred.PageFeatureTypes
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import java.util.*

class FigureFeature(
  val image: Texture,
  val caption: Component?,
  override val mustStartPage: Boolean = false,
  override val forceIndex: Int? = null
) : PageFeature {
  override val type: PageFeatureType<*>
    get() = PageFeatureTypes.FIGURE.get()

  override val coversOneWholePage = false

  override fun toString(): String {
    return "FigureFeature with caption $caption and preferred page index $forceIndex"
  }

  val textureHeight = image.height

  companion object {
    val CODEC = RecordCodecBuilder.mapCodec { builder ->
      builder.group(
        Texture.CODEC.fieldOf("image").forGetter(FigureFeature::image),
        ComponentSerialization.CODEC.optionalFieldOf("caption").forGetter { p -> Optional.ofNullable(p.caption) },
        Codec.BOOL.optionalFieldOf("must_start_page", false).forGetter(FigureFeature::mustStartPage),
        Codec.INT.optionalFieldOf("force_index", null).forGetter(FigureFeature::forceIndex)
      ).apply(builder) { img, cap, start, index ->
        FigureFeature(img, cap.orElse(null), start, index)
      }
    }

    fun translationId(baseId: String, featureIndex: Int) = "$baseId.figure_feature$featureIndex"
  }

}