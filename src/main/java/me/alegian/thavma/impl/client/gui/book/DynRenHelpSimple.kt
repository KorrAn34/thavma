package me.alegian.thavma.impl.client.gui.book

import me.alegian.thavma.impl.common.book.*
import net.minecraft.client.gui.Font
import net.minecraft.util.Mth.floor

const val lineOffset: Int = 2

// Not using scale currently
private fun PageFeature.renderedHeight(pageWidth: Int, font: Font): Int {
  val lineHeight = font.lineHeight + lineOffset
  return when (this) {
    is ParagraphFeature -> font.split(this.text, floor(pageWidth.toDouble())).size * lineHeight
    is TitleFeature -> font.split(this.text, (pageWidth)).size * lineHeight + 16
    is FigureFeature -> if (caption != null) font.split(
      this.caption,
      pageWidth
    ).size * lineHeight + this.textureHeight else this.textureHeight

    is RecipeFeature -> 96
    is FormattedTextFeature -> text.size * lineHeight
    else -> throw IllegalArgumentException("This PageFeature $this does not have renderedHeight() implemented yet")
  }
}

/**
 *  Returns a list of lists of features where every index represents a page.
 *  Features in the same list belong together on one page.
 */

// Not using scale currently
fun pagifyFeatures(
  features: List<PageFeature>,
  maxHeight: Int,
  pageWidth: Int,
  font: Font,
  scale: Float
): List<List<PageFeature>> {
  val pages = mutableListOf<List<PageFeature>>()
  val buffer = mutableListOf<PageFeature>()
  fun currentHeight() = buffer.sumOf { it.renderedHeight(pageWidth, font) }
  fun submitBufferAndClear() {
    pages.add(buffer.toList())
    buffer.clear()
  }

  // deal with elements without predetermined order (bulk of the logic)
  for (feature in features) {
    with(feature) {
      when {
        (this !is ParagraphFeature && this !is FigureFeature) && renderedHeight(
          pageWidth,
          font
        ) > maxHeight -> throw IllegalArgumentException(
          "The size of the element ${this::class.simpleName} is too large at ${
            renderedHeight(
              pageWidth,
              font
            )
          } while allowed $maxHeight."
        )

        coversOneWholePage -> {
          if (buffer.isNotEmpty()) submitBufferAndClear()
          pages += listOf(this)
        }

        mustStartPage -> {
          if (buffer.isNotEmpty()) submitBufferAndClear()
          buffer += this
        }

        currentHeight() + renderedHeight(pageWidth, font) <= maxHeight -> buffer += this
        // the next check might be redundant:
        buffer.isEmpty() -> throw IllegalArgumentException(
          "The size of the element ${this::class.simpleName} is too large at ${
            renderedHeight(
              pageWidth,
              font
            )
          } while allowed $maxHeight."
        )

        else -> {
          submitBufferAndClear()
          buffer += this
        }
      }
    }
  }

  // add anything left over in the buffer
  if (buffer.isNotEmpty()) submitBufferAndClear()

  return pages
}