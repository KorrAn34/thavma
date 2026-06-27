package me.alegian.thavma.impl.client.gui.book

import me.alegian.thavma.impl.client.gui.layout.*
import me.alegian.thavma.impl.client.texture.Texture
import me.alegian.thavma.impl.client.util.drawCenteredString
import me.alegian.thavma.impl.common.book.RecipeFeature
import me.alegian.thavma.impl.common.recipe.translationId
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.Renderable
import net.minecraft.network.chat.Component
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.RecipeType
import kotlin.jvm.optionals.getOrNull

object RecipeFeatureRenderer : PageFeatureRenderer<RecipeFeature> {
  private val GRID = Texture("gui/book/crafting", 96, 96, 96, 96)
  private val RESULT = Texture("gui/book/result", 32, 32, 32, 32)
  private val TITLE = Component.translatable(RecipeType.CRAFTING.translationId)
  private const val GAP = 12

  // Not using scale currently
  override fun initPageFeature(screen: EntryScreen, feature: RecipeFeature, maxWidth: Int, font: Font, scale: Float) {
    val recipe = Minecraft.getInstance().level?.recipeManager?.byKey(feature.recipeRL)?.getOrNull()?.value
    if (recipe !is CraftingRecipe) return // TODO: support other recipe types


    Column({
      alignCross = Alignment.CENTER
      size = grow()
      gap = GAP
    }) {
      Title(scale)

      TextureBox(RESULT) {}

      TextureBox(GRID) {}
    }
  }

  private fun Title(scale: Float) {
    val font = Minecraft.getInstance().font

    Row({
      height = fixed(font.lineHeight)
    }) {
      relativeRenderable {
        Renderable { guiGraphics, _, _, _ ->
          val lines = font.split(TITLE, size.x.toInt())
          // Not using scale currently
          //guiGraphics.pose().scale(scale, scale, 1.0f)
          for (line in lines) {
            guiGraphics.drawCenteredString(
              //font, line, size.x / scale / 2
              font, line, size.x / 2
            )
          }
        }
      }
    }
  }
}
