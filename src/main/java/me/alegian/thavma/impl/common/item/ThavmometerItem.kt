package me.alegian.thavma.impl.common.item

import me.alegian.thavma.impl.init.registries.T7AttributeModifiers.Revealing.ARCANE_LENS
import me.alegian.thavma.impl.init.registries.deferred.T7Attributes.REVEALING
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.item.Item
import net.minecraft.world.item.component.ItemAttributeModifiers

class ThavmometerItem(props: Properties) : Item(
  props.attributes(
    ItemAttributeModifiers.builder().add(
      REVEALING,
      ARCANE_LENS,
      EquipmentSlotGroup.MAINHAND
    ).build()
  ).stacksTo(1)
) {


}