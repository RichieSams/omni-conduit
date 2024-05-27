package richiesams.omniconduit.events

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.Hand
import richiesams.omniconduit.blockentities.ConduitBundleBlockEntity
import richiesams.omniconduit.blocks.ModBlocks

class ConduitAddEventHandler : ServerPlayNetworking.PlayPayloadHandler<ConduitAddEvent> {
    override fun receive(payload: ConduitAddEvent, context: ServerPlayNetworking.Context?) {
        // Execute on the main thread
        context?.player()?.server?.execute {
            val player = context.player()
            val world = player.world
            val targetState = world.getBlockState(payload.pos)
            // If the block doesn't exist yet, create it
            if (targetState.isAir) {
                world.setBlockState(payload.pos, ModBlocks.CONDUIT_BUNDLE.defaultState)
            }

            val blockEntity = world.getBlockEntity(payload.pos)
            if (blockEntity is ConduitBundleBlockEntity) {
                blockEntity.addConduit(player, if (payload.hand) Hand.MAIN_HAND else Hand.OFF_HAND)
            } else {
                throw RuntimeException("Received a CONDUIT_ADD_PACKET for a non ConduitBundleBlockEntity position")
            }
        }
    }
}