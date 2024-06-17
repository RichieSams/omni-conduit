package richiesams.omniconduit

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import richiesams.omniconduit.api.blockentities.OmniConduitBlockEntities
import richiesams.omniconduit.events.ModClientEvents
import richiesams.omniconduit.rendering.ConduitBundleBlockEntityRenderer

object OmniConduitClientBase : ClientModInitializer {
    override fun onInitializeClient() {
        ModClientEvents.registerModClientEvents()
        BlockEntityRendererFactories.register(OmniConduitBlockEntities.CONDUIT_BUNDLE, ::ConduitBundleBlockEntityRenderer)
    }
}