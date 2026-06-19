package me.alegian.thavma.impl.common.event

import me.alegian.thavma.impl.Thavma
import me.alegian.thavma.impl.client.gui.layer.*
import me.alegian.thavma.impl.client.gui.layer.RequestHistoryPacket.HistoryResponsePacket.Companion.handleHistoryResponse
import me.alegian.thavma.impl.client.gui.layer.RequestHistoryPacket.handleHistoryRequest
import me.alegian.thavma.impl.common.entity.AngryZombieEntity
import me.alegian.thavma.impl.common.payload.*
import me.alegian.thavma.impl.common.research.ResearchCategory
import me.alegian.thavma.impl.common.research.ResearchEntry
import me.alegian.thavma.impl.init.data.providers.*
import me.alegian.thavma.impl.init.registries.T7DataMaps
import me.alegian.thavma.impl.init.registries.T7DatapackRegistries
import me.alegian.thavma.impl.init.registries.T7Registries
import me.alegian.thavma.impl.init.registries.deferred.*
import me.alegian.thavma.impl.init.registries.deferred.callback.WandCallbacks
import me.alegian.thavma.impl.integration.curios.CuriosIntegration
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.SpawnPlacementTypes
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.level.block.ChestBlock
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent
import net.neoforged.neoforge.items.wrapper.InvWrapper
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadContext
import net.neoforged.neoforge.registries.DataPackRegistryEvent
import net.neoforged.neoforge.registries.ModifyRegistriesEvent
import net.neoforged.neoforge.registries.NewRegistryEvent
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS as KFF_MOD_BUS

private fun registerRegistries(event: NewRegistryEvent) {
  event.register(T7Registries.WAND_PLATING)
  event.register(T7Registries.WAND_CORE)
  event.register(T7Registries.ASPECT)
  event.register(T7Registries.PAGE_TYPE)
}

private fun registerDatapackRegistries(event: DataPackRegistryEvent.NewRegistry) {
  event.dataPackRegistry(T7DatapackRegistries.RESEARCH_CATEGORY, ResearchCategory.CODEC, ResearchCategory.CODEC)
  event.dataPackRegistry(T7DatapackRegistries.RESEARCH_ENTRY, ResearchEntry.CODEC, ResearchEntry.CODEC)
}

private fun modifyRegistries(event: ModifyRegistriesEvent) {
  val itemRegistry = event.getRegistry(Registries.ITEM)
  val coreRegistry = event.getRegistry(T7Registries.WAND_CORE.key())
  val platingRegistry = event.getRegistry(T7Registries.WAND_PLATING.key())
  val callbacks = WandCallbacks(itemRegistry, platingRegistry, coreRegistry)

  coreRegistry.addCallback(callbacks.coreCallback)
  platingRegistry.addCallback(callbacks.platingCallback)
}

private fun registerCapabilities(event: RegisterCapabilitiesEvent) {
  T7Items.registerCapabilities(event)
  T7BlockEntities.registerCapabilities(event)
  event.registerBlock(
    Capabilities.ItemHandler.BLOCK,
    { level, pos, state, _, _ ->
      InvWrapper(ChestBlock.getContainer(state.block as ChestBlock, state, level, pos, true)!!)
    },
    T7Blocks.HUNGRY_CHEST.get()
  )

  CuriosIntegration.get().registerCapabilities(event)
}

private fun registerDataMapTypes(event: RegisterDataMapTypesEvent) {
  event.register(T7DataMaps.AspectContent.ITEM)
  event.register(T7DataMaps.AspectContent.ENTITY)
  event.register(T7DataMaps.ASPECT_RELATIONS)
}

private fun gatherData(event: GatherDataEvent) {
  val generator = event.generator
  val lookupProvider = event.lookupProvider
  val existingFileHelper = event.existingFileHelper
  val packOutput = generator.packOutput

  generator.addProvider(event.includeServer(), T7DatapackBuiltinEntriesProvider(packOutput, lookupProvider))
  generator.addProvider(event.includeServer(), T7DataMapProvider(packOutput, lookupProvider))
  generator.addProvider(event.includeServer(), T7RecipeProvider(packOutput, lookupProvider))
  val blockTagProvider = generator.addProvider(
    event.includeServer(),
    T7BlockTagProvider(packOutput, lookupProvider, existingFileHelper)
  )
  generator.addProvider(
    event.includeServer(),
    T7ItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), existingFileHelper)
  )
  generator.addProvider(
    event.includeServer(),
    T7DamageTypeTagsProvider(packOutput, lookupProvider, existingFileHelper)
  )
  generator.addProvider(event.includeServer(), T7FluidTagProvider(packOutput, lookupProvider, existingFileHelper))
  generator.addProvider(event.includeServer(), T7GlobalLootModifierProvider(packOutput, lookupProvider))
  generator.addProvider(
    event.includeServer(), T7LootTableProvider(
      packOutput, listOf(
        SubProviderEntry(
          ::T7BlockLootSubProvider,
          LootContextParamSets.BLOCK
        ),
        SubProviderEntry(
          ::T7EntityLootSubProvider,
          LootContextParamSets.ENTITY
        )
      ), lookupProvider
    )
  )

  generator.addProvider(event.includeClient(), T7BlockStateProvider(packOutput, existingFileHelper))
  generator.addProvider(event.includeClient(), T7ItemModelProvider(packOutput, existingFileHelper))
  generator.addProvider(event.includeClient(), T7ParticleDescriptionProvider(packOutput, existingFileHelper))
  generator.addProvider(event.includeClient(), T7LanguageProvider(packOutput, "en_us"))

  CuriosIntegration.get().gatherData(event)
}

private fun modifyDefaultComponents(event: ModifyDefaultComponentsEvent) {
  event.modify(
    T7Items.THAVMITE_HAMMER
  ) {
    it.set(
      DataComponents.MAX_DAMAGE,
      T7Items.THAVMITE_HAMMER.get().tier.uses * 2
    )
  }
}

private fun entityAttributeModification(event: EntityAttributeModificationEvent) {
  if (!event.has(EntityType.PLAYER, T7Attributes.REVEALING))
    event.add(EntityType.PLAYER, T7Attributes.REVEALING)
}

private fun entityAttributeCreation(event: EntityAttributeCreationEvent) {
  event.put(T7EntityTypes.ANGRY_ZOMBIE.get(), AngryZombieEntity.createAttributes())
}

private fun registerSpawnPlacements(event: RegisterSpawnPlacementsEvent) {
  event.register(
    T7EntityTypes.ANGRY_ZOMBIE.get(),
    SpawnPlacementTypes.ON_GROUND,
    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
    Monster::checkMonsterSpawnRules,
    RegisterSpawnPlacementsEvent.Operation.REPLACE
  )
}

private fun onCommonSetup(event: FMLCommonSetupEvent) {
  event.enqueueWork {

    // This is only in case certain notifications need to be gatekept
    // i.e. require some research to be acquired etc.
    // The resource locations are all just sample text and not real

//    NotificationEventRegistry.register(
//      ResourceLocation.fromNamespaceAndPath(Thavma.MODID, "opened_crystallography")
//    ) { player ->
//      // Gate on server state — if the player hasn't unlocked this, return null
//      if (!player.persistentData.getBoolean("thavma.research.crystallography")) return@register null
//      NotificationEventRegistry.NotifySpec(
//        text  = Component.translatable("thavma.notif.opened_crystallography"),
//        color = 0x44CCFF
//      )
//    }


    NotificationEventRegistry.register(
      ResourceLocation.fromNamespaceAndPath(Thavma.MODID, "test_notification_sync"), { _ ->
        // No gate — always fires
        NotificationEventRegistry.NotifySpec(
          text = Component.translatable("thavma.notif.infusion_page"),
          color = 0xCC44FF
        )
      })
  }
}

private fun registerPayloadHandlers(event: RegisterPayloadHandlersEvent) {
  val registrar = event.registrar("1")
  registrar.playToClient(
    ScanResultPayload.TYPE,
    ScanResultPayload.STREAM_CODEC,
    ScanResultPayload::handle
  )
  registrar.playToClient(
    ResearchToastPayload.TYPE,
    ResearchToastPayload.STREAM_CODEC,
    ResearchToastPayload::handle
  )
  registrar.playToServer(
    ResearchScrollPayload.TYPE,
    ResearchScrollPayload.STREAM_CODEC,
    ResearchScrollPayload::handle
  )
  registrar.playToServer(
    SocketStatePayload.TYPE,
    SocketStatePayload.STREAM_CODEC,
    SocketStatePayload::handle
  )
  registrar.playToServer(
    FocusPayload.TYPE,
    FocusPayload.STREAM_CODEC,
    FocusPayload::handle
  )
  registrar.playToServer(
    HammerPayload.TYPE,
    HammerPayload.STREAM_CODEC,
    HammerPayload::handle
  )
  registrar.playToClient(
    ShowNotificationPacket.TYPE,
    ShowNotificationPacket.STREAM_CODEC,
    ::handleShowNotification
  )
  registrar.playToServer(
    ServerboundNotifyRequestPacket.TYPE,
    ServerboundNotifyRequestPacket.STREAM_CODEC
  ) { packet, context ->
    val player = context.player() as? ServerPlayer ?: return@playToServer
    context.enqueueWork {
      // Unknown IDs return null and are silently dropped — no feedback to client,
      // which prevents probing for valid event IDs
      val spec = NotificationEventRegistry.resolve(packet.eventId, player) ?: return@enqueueWork
      NotificationDispatcher.send(player, spec.text, spec.color, spec.image, spec.scale)
    }
  }
  registrar.playToServer(
    RequestHistoryPacket.TYPE,
    RequestHistoryPacket.STREAM_CODEC,
    ::handleHistoryRequest
  )

  registrar.playToClient(
    RequestHistoryPacket.HistoryResponsePacket.TYPE,
    RequestHistoryPacket.HistoryResponsePacket.STREAM_CODEC,
    ::handleHistoryResponse
  )
}

fun registerCommonModEvents() {
  KFF_MOD_BUS.addListener(::registerRegistries)
  KFF_MOD_BUS.addListener(::registerDatapackRegistries)
  KFF_MOD_BUS.addListener(::modifyRegistries)
  KFF_MOD_BUS.addListener(::registerCapabilities)
  KFF_MOD_BUS.addListener(::registerDataMapTypes)
  KFF_MOD_BUS.addListener(::gatherData)
  KFF_MOD_BUS.addListener(::modifyDefaultComponents)
  KFF_MOD_BUS.addListener(::entityAttributeModification)
  KFF_MOD_BUS.addListener(::entityAttributeCreation)
  KFF_MOD_BUS.addListener(::registerSpawnPlacements)
  KFF_MOD_BUS.addListener(::registerPayloadHandlers)
  KFF_MOD_BUS.addListener(::onCommonSetup)
}

@OnlyIn(Dist.CLIENT)
private fun handleShowNotification(packet: ShowNotificationPacket, context: IPayloadContext) {
  context.enqueueWork {
    val player = Minecraft.getInstance().player ?: return@enqueueWork
    PlayerNotifications.add(
      text = packet.text,
      player = player,
      image = packet.image,
      color = packet.color,
      scale = packet.scale,
      isPriority = true
    )
    // History was already recorded server-side — nothing to do here
  }
}
