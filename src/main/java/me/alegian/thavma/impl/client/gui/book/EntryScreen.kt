package me.alegian.thavma.impl.client.gui.book

import me.alegian.thavma.impl.client.gui.layout.*
import me.alegian.thavma.impl.client.texture.Texture
import me.alegian.thavma.impl.common.book.PageFeature
import me.alegian.thavma.impl.common.research.ResearchEntry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component

class EntryScreen(entry: Holder<ResearchEntry>) : Screen(Component.literal("Book Entry")) {
  companion object {
    private val BG = Texture("gui/book/background", 510, 282, 512, 512)
  }

  private var currentPage = 0
  private val fontify = Minecraft.getInstance().font
  private val entry = entry.value()

  // Not using scale currently
  private val scale = 1f

  var pages = listOf<List<PageFeature>>()

  override fun init() {
    super.init()
    clearWidgets()

    LayoutExtensions.currScreen = this
    Row({
      width = fixed(this@EntryScreen.width)
      height = fixed(this@EntryScreen.height)
      align = Alignment.CENTER
    }) {
      TextureBox(BG) {
        Column({
          size = grow()
          gap = 8
        }) {
          Row({
            size = grow()
            paddingY = PageTurningWidget.LEFT_TEXTURE.height + 8
            paddingX = 32
            paddingBottom = 42
            gap = 48
          }) {
            Row({
              size = grow()
            }) {
              Column({
                size = grow()
                gap = 4
              }) {
                afterLayout {
                  if (pages.isEmpty()) pages = pagifyFeatures(
                    entry.pageFeatures,
                    this@afterLayout.size.y.toInt(),
                    this@afterLayout.size.x.toInt(),
                    fontify,
                    scale
                  )
                  val features = pages.getOrNull(currentPage)
                  if (features != null) {
                    for (feature in features) initPageFeature(feature, this@afterLayout.size.x.toInt())
                  }
                }
//                maxWidth = BG.width / 2 - maxWidthCorrection
//                if (pages.isEmpty()) pages = pagifyFeatures(
//                  entry.pageFeatures,
//                  BG.height - maxHeightCorrection,
//                  maxWidth,
//                  fontify,
//                  scale
//                )

              }
            }

            Row({
              size = grow()
            }) {
              Column({
                size = grow()
                gap = 4
              }) {
                afterLayout {
                  val features = pages.getOrNull(currentPage + 1)
                  if (features != null) {
                    for (feature in features) initPageFeature(feature, this@afterLayout.size.x.toInt())
                  }
                }
              }
            }
          }
          PageTurnerRow()
        }
      }
    }
  }

  private fun PageTurnerRow() {
    Row({
      width = grow()
      paddingX = 40
      paddingY = 20
    }) {
      if (currentPage != 0) {
        Box({
          width = fixed(PageTurningWidget.LEFT_TEXTURE.width)
          height = fixed(PageTurningWidget.LEFT_TEXTURE.height)
        }) {
          afterLayout {
            addRenderableWidget(PageTurningWidget(position, false) {
              // rerender the screen for the new page(s)
              turnPage(false)
            })
          }
        }
      }
      Box({ width = grow() }) {}
      if (pages.getOrNull(currentPage + 2) != null) {
        Box({
          width = fixed(PageTurningWidget.RIGHT_TEXTURE.width)
          height = fixed(PageTurningWidget.RIGHT_TEXTURE.height)
        }) {
          afterLayout {
            addRenderableWidget(PageTurningWidget(position, true) {
              // rerender the screen for the new page(s)
              turnPage(true)
            })
          }
        }
      }
    }
  }

  fun turnPage(right: Boolean) {
    if (right) currentPage += 2
    else currentPage -= 2
    init()
  }

  override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    renderTransparentBackground(guiGraphics)
  }

  // wrapper around... unchecked cast
  private fun <T : PageFeature?> initPageFeature(feature: T, maxWidth: Int) {
    if (feature != null) {
      val renderer = PAGE_FEATURE_RENDERERS[feature.type] as PageFeatureRenderer<T>
      // Not using scale currently
      renderer.initPageFeature(this, feature, maxWidth, fontify, scale)
    }
  }

  override fun isPauseScreen() = false
}