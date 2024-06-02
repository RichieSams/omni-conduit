package richiesams.omniconduit.items

import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import richiesams.omniconduit.OmniConduitModBase
import richiesams.omniconduit.api.conduits.ConduitDisplayMode
import richiesams.omniconduit.conduits.ModConduits


object ModItems {
    val YETA_WRENCH: YetaWrenchItem = YetaWrenchItem(
        Item.Settings()
            .maxCount(1)
            .component(ModDataComponentTypes.YETA_WRENCH_MODE, ConduitDisplayMode.ALL.type)
    )
    
    val ITEM_CONDUIT: ConduitItem = ConduitItem(ModConduits.ITEM_CONDUIT, Item.Settings())
    val BASIC_ENERGY_CONDUIT: ConduitItem = ConduitItem(ModConduits.BASIC_ENERGY_CONDUIT, Item.Settings())
    val BASIC_FLUID_CONDUIT: ConduitItem = ConduitItem(ModConduits.BASIC_FLUID_CONDUIT, Item.Settings())
    val REDSTONE_CONDUIT: ConduitItem = ConduitItem(ModConduits.REDSTONE_CONDUIT, Item.Settings())

    fun registerItems() {
        Registry.register(Registries.ITEM, Identifier(OmniConduitModBase.MOD_ID, "yeta_wrench"), YETA_WRENCH)
        Registry.register(Registries.ITEM, Identifier(OmniConduitModBase.MOD_ID, "item_conduit"), ITEM_CONDUIT)
        Registry.register(Registries.ITEM, Identifier(OmniConduitModBase.MOD_ID, "basic_energy_conduit"), BASIC_ENERGY_CONDUIT)
        Registry.register(Registries.ITEM, Identifier(OmniConduitModBase.MOD_ID, "basic_fluid_conduit"), BASIC_FLUID_CONDUIT)
        Registry.register(Registries.ITEM, Identifier(OmniConduitModBase.MOD_ID, "redstone_conduit"), REDSTONE_CONDUIT)
    }
}