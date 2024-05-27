package richiesams.omniconduit

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.util.Identifier
import richiesams.omniconduit.api.conduits.Conduit

object OnmiConduitRegistries {
    val CONDUIT: SimpleRegistry<Conduit> = FabricRegistryBuilder.createSimple(
        RegistryKey.ofRegistry<Conduit>(Identifier(OmniConduitModBase.MOD_ID, "conduit_registry"))
    )
        .attribute(RegistryAttribute.SYNCED)
        .buildAndRegister()
}