package richiesams.omniconduit.api

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.minecraft.util.Identifier
import richiesams.omniconduit.OmniConduitModBase
import richiesams.omniconduit.api.blockentities.ConduitBundleBlockEntity
import richiesams.omniconduit.api.blockentities.OmniConduitBlockEntities

object BlockApiLookups {
    val CONDUIT_BUNDLE: BlockApiLookup<ConduitBundleBlockEntity, Unit> =
        BlockApiLookup.get(Identifier(OmniConduitModBase.MOD_ID, "conduit_bundle"), ConduitBundleBlockEntity::class.java, Unit::class.java)

    fun registerAPIs() {
        CONDUIT_BUNDLE.registerSelf(OmniConduitBlockEntities.CONDUIT_BUNDLE)
    }
}
