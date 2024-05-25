package richiesams.omniconduit.blocks

import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import richiesams.omniconduit.OmniConduitModBase

object ModBlocks {
    var CONDUIT_BUNDLE: Block = ConduitBundleBlock(AbstractBlock.Settings.create().strength(4.0f).nonOpaque().dynamicBounds())


    private fun registerBlock(name: String, block: Block): Block {
        registerBlockItem(name, block)
        return Registry.register(Registries.BLOCK, Identifier(OmniConduitModBase.MOD_ID, name), block)
    }

    private fun registerBlockItem(name: String, block: Block) {
        Registry.register(
            Registries.ITEM, Identifier(OmniConduitModBase.MOD_ID, name),
            BlockItem(block, Item.Settings())
        )
    }

    fun registerBlocks() {
        registerBlock("conduit_bundle", CONDUIT_BUNDLE)
    }
}
