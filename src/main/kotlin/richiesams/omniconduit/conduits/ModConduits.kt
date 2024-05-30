package richiesams.omniconduit.conduits

import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import richiesams.omniconduit.OmniConduitModBase
import richiesams.omniconduit.api.OnmiConduitRegistries
import richiesams.omniconduit.api.conduits.ConduitLoader
import richiesams.omniconduit.conduitentities.EnergyConduitEntity
import richiesams.omniconduit.conduitentities.FluidConduitEntity
import richiesams.omniconduit.conduitentities.ItemConduitEntity
import richiesams.omniconduit.conduitentities.RedstoneConduitEntity

object ModConduits {
    private val ITEM_CONDUIT_ID = Identifier(OmniConduitModBase.MOD_ID, "item_conduit")
    val ITEM_CONDUIT = ConduitLoader.load(ITEM_CONDUIT_ID, ::ItemConduit, ::ItemConduitEntity)

    private val BASIC_ENERGY_CONDUIT_ID = Identifier(OmniConduitModBase.MOD_ID, "basic_energy_conduit")
    val BASIC_ENERGY_CONDUIT = ConduitLoader.load(BASIC_ENERGY_CONDUIT_ID, ::EnergyConduit, ::EnergyConduitEntity)

    private val BASIC_FLUID_CONDUIT_ID = Identifier(OmniConduitModBase.MOD_ID, "basic_fluid_conduit")
    val BASIC_FLUID_CONDUIT = ConduitLoader.load(BASIC_FLUID_CONDUIT_ID, ::FluidConduit, ::FluidConduitEntity)

    private val REDSTONE_CONDUIT_ID = Identifier(OmniConduitModBase.MOD_ID, "redstone_conduit")
    val REDSTONE_CONDUIT = ConduitLoader.load(REDSTONE_CONDUIT_ID, ::RedstoneConduit, ::RedstoneConduitEntity)

    fun registerConduits() {
        Registry.register(OnmiConduitRegistries.CONDUIT, ITEM_CONDUIT_ID, ITEM_CONDUIT)
        Registry.register(OnmiConduitRegistries.CONDUIT, BASIC_ENERGY_CONDUIT_ID, BASIC_ENERGY_CONDUIT)
        Registry.register(OnmiConduitRegistries.CONDUIT, BASIC_FLUID_CONDUIT_ID, BASIC_FLUID_CONDUIT)
        Registry.register(OnmiConduitRegistries.CONDUIT, REDSTONE_CONDUIT_ID, REDSTONE_CONDUIT)
    }
}