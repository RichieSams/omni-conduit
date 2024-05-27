package richiesams.omniconduit.conduits

import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import richiesams.omniconduit.OmniConduitModBase
import richiesams.omniconduit.OnmiConduitRegistries
import richiesams.omniconduit.api.conduits.ConduitLoader
import richiesams.omniconduit.conduitentities.ItemConduitEntity

object ModConduits {
    private val ITEM_CONDUIT_ID = Identifier(OmniConduitModBase.MOD_ID, "item_conduit")
    val ITEM_CONDUIT = ConduitLoader.load(ITEM_CONDUIT_ID, ::ItemConduit, ::ItemConduitEntity)

    fun registerConduits() {
        Registry.register(OnmiConduitRegistries.CONDUIT, ITEM_CONDUIT_ID, ITEM_CONDUIT)
    }
}