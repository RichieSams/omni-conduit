package richiesams.omniconduit

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
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
import richiesams.omniconduit.blockentities.ModBlockEntities
import richiesams.omniconduit.blocks.ModBlocks

object OmniConduitModBase : ModInitializer {
    val LOGGER = LoggerFactory.getLogger("omni-conduit")

    const val MOD_ID: String = "omni_conduit"
    private val BASE_ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier(MOD_ID, "base_group"))

    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        LOGGER.info("Hello Fabric world!")

        ModBlocks.registerBlocks()
        ModBlockEntities.registerBlockEntities()

        // Register the item group now that everything is loaded
        Registry.register(Registries.ITEM_GROUP, BASE_ITEM_GROUP, FabricItemGroup.builder()
            .icon { ItemStack(Items.ANVIL) }
            .displayName(Text.translatable("itemGroup.omni_conduit.base_group"))
            .entries { _, entries -> addItemGroupEntries(entries) }
            .build()
        )
    }

    private fun addItemGroupEntries(entries: ItemGroup.Entries) {
        entries.add(ModBlocks.CONDUIT_BUNDLE.asItem())
    }
}