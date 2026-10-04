package me.alegian.thavma.impl.init.registries

import me.alegian.thavma.impl.common.book.TranslationPlaceholder
import me.alegian.thavma.impl.common.research.ResearchCategory
import me.alegian.thavma.impl.common.research.ResearchEntry
import me.alegian.thavma.impl.rl
import net.minecraft.resources.ResourceKey

object T7DatapackRegistries {
  val RESEARCH_CATEGORY = ResourceKey.createRegistryKey<ResearchCategory>(rl("research/category"))
  val RESEARCH_ENTRY = ResourceKey.createRegistryKey<ResearchEntry>(rl("research/entry"))
  val TRANSLATION_PLACEHOLDER = ResourceKey.createRegistryKey<TranslationPlaceholder>(rl("translation_placeholders"))
}