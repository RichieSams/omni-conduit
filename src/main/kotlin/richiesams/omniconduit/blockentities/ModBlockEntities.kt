package richiesams.omniconduit.blockentities

import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import richiesams.omniconduit.OmniConduitModBase
import richiesams.omniconduit.blocks.ModBlocks


object ModBlockEntities {
    val CONDUIT_BUNDLE: BlockEntityType<ConduitBundleBlockEntity> =
        BlockEntityType.Builder.create(::ConduitBundleBlockEntity, ModBlocks.CONDUIT_BUNDLE).build()

    fun registerBlockEntities() {
        Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(OmniConduitModBase.MOD_ID, "conduit_bundle"),
            CONDUIT_BUNDLE
        )
    }
}