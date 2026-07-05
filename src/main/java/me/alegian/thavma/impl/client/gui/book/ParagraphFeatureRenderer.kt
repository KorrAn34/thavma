package me.alegian.thavma.impl.client.gui.book

import me.alegian.thavma.impl.client.gui.layout.Row
import me.alegian.thavma.impl.client.gui.layout.draw
import me.alegian.thavma.impl.client.gui.layout.fixed
import me.alegian.thavma.impl.client.gui.layout.grow
import me.alegian.thavma.impl.client.util.drawString
import me.alegian.thavma.impl.client.util.translateXY
import me.alegian.thavma.impl.client.util.usePose
import me.alegian.thavma.impl.common.book.ParagraphFeature
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.Renderable

object ParagraphFeatureRenderer : PageFeatureRenderer<ParagraphFeature> {

  // Not using scale currently
  override fun initPageFeature(
    screen: EntryScreen,
    feature: ParagraphFeature,
    maxWidth: Int,
    font: Font,
    scale: Float
  ) {
    val LINE_HEIGHT = font.lineHeight + lineOffset

    Row({
      val lines = font.split(feature.text, maxWidth)
      width = grow()
      height = fixed(LINE_HEIGHT * (lines.size + 0.5f))
    }) {
      draw {
        Renderable { guiGraphics, _, _, _ ->
          // Not using scale currently
          // guiGraphics.pose().scale(scale, scale, 1.0f)
          guiGraphics.usePose {
            for (line in font.split(feature.text, maxWidth)) {
              //for (line in font.split(feature.text, (maxWidth / scale).toInt())) {
              guiGraphics.drawString(font, line)
              //translateXY(0, LINE_HEIGHT / scale)
              translateXY(0, LINE_HEIGHT)
            }
            translateXY(0, LINE_HEIGHT * 2 / 3)
          }
        }
      }
    }
  }
}