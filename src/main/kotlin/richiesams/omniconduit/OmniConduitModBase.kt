package richiesams.omniconduit

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import richiesams.omniconduit.api.BlockApiLookups
import richiesams.omniconduit.api.blockentities.OmniConduitBlockEntities
import richiesams.omniconduit.blocks.ModBlocks
import richiesams.omniconduit.conduits.ModConduits
import richiesams.omniconduit.events.ModEvents
import richiesams.omniconduit.items.ModDataComponentTypes
import richiesams.omniconduit.items.ModItems

object OmniConduitModBase : ModInitializer {
    const val MOD_ID: String = "omniconduit"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)
    private val BASE_ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier(MOD_ID, "base_group"))

    override fun onInitialize() {
        MinecraftClient.getInstance()

        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        LOGGER.info("Initializing OmniConduit mod")

        ModBlocks.registerBlocks()
        OmniConduitBlockEntities.registerBlockEntities()
        ModConduits.registerConduits()
        ModDataComponentTypes.registerDataComponentTypes()
        ModItems.registerItems()
        BlockApiLookups.registerAPIs()

        // Register the item group now that everything is loaded
        Registry.register(Registries.ITEM_GROUP, BASE_ITEM_GROUP, FabricItemGroup.builder()
            .icon { ItemStack(Items.ANVIL) }
            .displayName(Text.translatable("itemGroup.omniconduit.base_group"))
            .entries { _, entries -> addItemGroupEntries(entries) }
            .build()
        )

        ModEvents.registerModEvents()
    }

    private fun addItemGroupEntries(entries: ItemGroup.Entries) {
        entries.add(ModItems.YETA_WRENCH)
        entries.add(ModItems.ITEM_CONDUIT)
        entries.add(ModItems.BASIC_ENERGY_CONDUIT)
        entries.add(ModItems.BASIC_FLUID_CONDUIT)
        entries.add(ModItems.REDSTONE_CONDUIT)
    }
}