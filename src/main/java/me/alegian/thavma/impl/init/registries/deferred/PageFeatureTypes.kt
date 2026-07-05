package me.alegian.thavma.impl.init.registries.deferred

import me.alegian.thavma.impl.Thavma
import me.alegian.thavma.impl.common.book.*
import me.alegian.thavma.impl.init.registries.T7Registries
import me.alegian.thavma.impl.rl
import net.neoforged.neoforge.registries.DeferredRegister

object PageFeatureTypes {
  val REGISTRAR = DeferredRegister.create(T7Registries.PAGE_FEATURE_TYPE.key(), Thavma.MODID)

  val PARAGRAPH =
    REGISTRAR.register("paragraph_feature") { ->
      PageFeatureType<ParagraphFeature>(
        rl("paragraph_feature"),
        ParagraphFeature.CODEC
      )
    }
  val FORMATTED = REGISTRAR.register("formatted_text_feature") { ->
    PageFeatureType(
      rl("formatted_text_feature"),
      FormattedTextFeature.CODEC
    )
  }
  val TITLE =
    REGISTRAR.register("title_feature") { -> PageFeatureType<TitleFeature>(rl("title_feature"), TitleFeature.CODEC) }
  val FIGURE = REGISTRAR.register("figure_feature") { ->
    PageFeatureType<FigureFeature>(
      rl("figure_feature"),
      FigureFeature.CODEC
    )
  }
  val RECIPE = REGISTRAR.register("recipe_feature") { ->
    PageFeatureType<RecipeFeature>(
      rl("recipe_feature"),
      RecipeFeature.CODEC
    )
  }
}