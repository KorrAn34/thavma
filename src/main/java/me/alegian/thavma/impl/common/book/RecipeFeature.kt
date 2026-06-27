package me.alegian.thavma.impl.common.book

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.alegian.thavma.impl.init.registries.deferred.PageFeatureTypes
import net.minecraft.resources.ResourceLocation

class RecipeFeature(
  val recipeRL: ResourceLocation,
  override val coversOneWholePage: Boolean = true,
  override val mustStartPage: Boolean = true,
  override val forceIndex: Int? = null
) : PageFeature {
  override val type: PageFeatureType<*>
    get() = PageFeatureTypes.RECIPE.get()

  companion object {
    val CODEC = RecordCodecBuilder.mapCodec { builder ->
      builder.group(
        ResourceLocation.CODEC.fieldOf("recipeRL").forGetter(RecipeFeature::recipeRL),
        Codec.BOOL.optionalFieldOf("covers_one_whole_page", true).forGetter(RecipeFeature::coversOneWholePage),
        Codec.BOOL.optionalFieldOf("must_start_page", true).forGetter(RecipeFeature::mustStartPage),
        Codec.INT.optionalFieldOf("force_index", null).forGetter(RecipeFeature::forceIndex)
      ).apply(builder, ::RecipeFeature)
    }

    fun translationId(baseId: String, featureIndex: Int) = "$baseId.figure_feature$featureIndex"
  }
}