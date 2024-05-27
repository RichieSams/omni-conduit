package richiesams.omniconduit.items

import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import richiesams.omniconduit.OmniConduitModBase
import richiesams.omniconduit.conduits.ModConduits


object ModItems {
    var ITEM_CONDUIT: ConduitItem = ConduitItem(ModConduits.ITEM_CONDUIT, Item.Settings())

    fun registerItems() {
        Registry.register(Registries.ITEM, Identifier(OmniConduitModBase.MOD_ID, "item_conduit"), ITEM_CONDUIT)
    }
}