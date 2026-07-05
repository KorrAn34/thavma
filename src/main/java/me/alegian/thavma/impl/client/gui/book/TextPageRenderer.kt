package me.alegian.thavma.impl.client.gui.book

import me.alegian.thavma.impl.client.gui.layout.*
import me.alegian.thavma.impl.client.texture.Texture
import me.alegian.thavma.impl.client.util.drawCenteredString
import me.alegian.thavma.impl.client.util.drawString
import me.alegian.thavma.impl.client.util.translateXY
import me.alegian.thavma.impl.client.util.usePose
import me.alegian.thavma.impl.common.book.TextPage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Renderable
import net.minecraft.network.chat.Component

object TextPageRenderer : PageRenderer<TextPage> {
  private val SEPARATOR = Texture("gui/book/separator", 128, 16, 128, 16)

  override fun initPage(screen: EntryScreen, page: TextPage) {
    val font = Minecraft.getInstance().font
    val LINE_HEIGHT = font.lineHeight + lineOffset

    Column({
      size = grow()
      gap = 4
    }) {
      if (page.title != null) {
        Title(page.title)
        Separator()
      }
      Row({
        size = grow()
      }) {
        draw {
          Renderable { guiGraphics, _, _, _ ->
            guiGraphics.usePose {
              for (paragraph in page.paragraphs) {
                for (line in font.split(paragraph, size.x.toInt())) {
                  guiGraphics.drawString(font, line)
                  translateXY(0, LINE_HEIGHT)
                }
                translateXY(0, LINE_HEIGHT * 2 / 3)
              }
            }
          }
        }
      }
    }
  }

  private fun Separator() {
    Row({
      width = grow()
      alignMain = Alignment.CENTER
    }) {
      TextureBox(SEPARATOR) {}
    }
  }

  private fun Title(text: Component) {
    val font = Minecraft.getInstance().font

    Row({
      width = grow()
      height = fixed(font.lineHeight)
    }) {
      draw {
        Renderable { guiGraphics, _, _, _ ->
          val lines = font.split(text, size.x.toInt())
          // Not using scale currently
          //guiGraphics.pose().scale(scale, scale, 1.0f)
          guiGraphics.usePose {
            for ((index, line) in lines.withIndex()) {
              guiGraphics.drawCenteredString(
                //font, line, size.x / scale / 2
                font, line, size.x / 2
              )
              if (index != lines.lastIndex) translateXY(0, font.lineHeight + lineOffset)
            }
          }
        }
      }
    }
  }
}
