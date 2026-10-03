package me.alegian.thavma.impl.init.data.providers

import me.alegian.thavma.impl.Thavma
import me.alegian.thavma.impl.client.texture.Texture
import me.alegian.thavma.impl.common.book.*
import me.alegian.thavma.impl.common.research.ResearchEntry
import me.alegian.thavma.impl.init.registries.deferred.ResearchEntries
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceKey

internal object ResearchBookContent {
  val PLACEHOLDERS = "placeholders." + Thavma.MODID + "."
  private val contentByEntry = mapOf(
    ResearchEntries.Thavma.THAVMA to listOf(
      title("Thavma"),
      paragraph(
        """
          I was merely toying with that wand -if it can even be called that- when this tome
          flew into my hands! I can sense great power within it.
        """
      ),
      paragraph(
        """
          The cover reads "Elements", but a lot of its pages appear blank, sealed by some magic.
        """
      ),
      paragraph(
        """
          To read them, I will first need to break that seal. It won't be easy... but
          I have a feeling it will be worth my efforts.
        """
      ),
      pageBreak(),
      paragraph(
        """
          I will document all my findings inside the book, so that I can recall them later.
        """
      ),
    ),
    ResearchEntries.Thavma.ARCANE_LENS to listOf(
      title("The Arcane Lens"),
      paragraph(
        """
          The part of the book I can read describes an arcane tool that "allows the user
          to see", whatever that might mean. I have a feeling that crafting it could assist
          my work in unsealing the other pages.
        """
      ),
      paragraph(
        """
          The blueprint describes a hexagonal device, much like a prism,
          made with those colorful crystals I found lying in a cave.
        """
      ),
      paragraph(
        """
          I should look at the world through its lens, maybe it will uncover something useful.
        """
      ),
    ),
    ResearchEntries.Thavma.INFUSION to listOf(
      figure(
        Texture("gui/images/infusion", 1916, 1036, 1916, 1036),
        180,
        101,
        "An image of the infusion altar",
      ),
    ),
    ResearchEntries.Lore.MYTH to listOf(
      title("From the Heart's Eclipsed Depths"),
      paragraph(
        """
        Once the symposium partakers have each had their fill of wine and nestle in their seats of the andron, one of them crieth: “O 
        didaskale
        , day and night do we ponder the quintessential mysteries of the world, pay thorough mind to man’s doom and mind no scrutiny of our own judgement, yet the unhiddenness most simple eludeth us. What is, in truth, love? In waking and dreaming we see the truth of love all around and follow the path of love unto true knowledge. Thus, why may we not capture the essence of the thing, or more-than-thing?”
      """, Style.EMPTY.withItalic(true)
      ),
      paragraph(
        """
        The master casteth his gaze over the sea into the distance. He answereth that love is a thing most simple indeed, though her faces hardly can enumerate he who sifteth gold out of sand grain by grain.
      """, Style.EMPTY.withItalic(true)
      ),
      paragraph(
        """
        “But is there no saving grace for him who seeketh sense therein? Dost thou know of a fable that in the blink of an eye illuminateth the shadow of doubt?” asketh another.
      """, Style.EMPTY.withItalic(true)
      ),
      paragraph(
        """
        The sage confesseth that such might outnumber the night velvet’s pearls, and still one resoundeth stubbornly in his mind’s ear.
      """, Style.EMPTY.withItalic(true)
      ),
      paragraph("“Tarry no longer, speak!”", Style.EMPTY.withItalic(true))
    )
  )

  fun featuresFor(entryKey: ResourceKey<ResearchEntry>): List<PageFeature> =
    contentByEntry[entryKey].orEmpty().mapIndexed { index, feature ->
      feature.create(PageFeature.translationId(ResearchEntry.translationId(entryKey), index))
    }

  fun translations(): Map<String, String> = buildMap {
    for ((entryKey, features) in contentByEntry) {
      val baseId = ResearchEntry.translationId(entryKey)
      features.forEachIndexed { index, feature ->
        feature.text?.let { put(PageFeature.translationId(baseId, index), it) }
      }
    }
  }

  fun placeholders():
}

private class FeatureDefinition(
  val text: String?,
  val factory: (String) -> PageFeature,
) {
  fun create(translationId: String) = factory(translationId)
}

private fun title(text: String) = FeatureDefinition(text.normalize()) { translationId ->
  TitleFeature(Component.translatable(translationId).withStyle(ChatFormatting.BOLD))
}

private fun paragraph(text: String, styles: Style = Style.EMPTY, vararg placeholders: MutableComponent = emptyArray()) =
  FeatureDefinition(text.normalize()) { translationId ->
    ParagraphFeature(separateComponentStyles(translationId, styles, *placeholders))
}

private fun pageBreak() = FeatureDefinition(null) { PageBreakFeature() }

private fun figure(image: Texture, width: Int, height: Int, caption: String? = null) =
  FeatureDefinition(caption?.normalize()) { translationId ->
    FigureFeature(image, width, height, caption?.let { Component.translatable(translationId) })
  }

private fun String.normalize() = trimIndent().replace("\n", " ")

private val NO_FORMAT = Style.EMPTY
  .withBold(false)
  .withItalic(false)
  .withUnderlined(false)
  .withStrikethrough(false)
  .withObfuscated(false)
  .withColor(0x000000)

/** Creates a copy of the component that ignores any text formatting inherited from its parent. */
private fun Component.unformatted(): MutableComponent =
  copy().setStyle(style.applyTo(NO_FORMAT))

fun separateComponentStyles(key: String, baseStyle: Style, vararg placeholders: Component) =
  Component.translatable(key, *placeholders.map { it.unformatted() }.toTypedArray()).setStyle(baseStyle)
