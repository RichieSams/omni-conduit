package richiesams.omniconduit.events

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import richiesams.omniconduit.items.ModDataComponentTypes
import richiesams.omniconduit.items.ModItems

class YetaWrenchModeEventHandler : ServerPlayNetworking.PlayPayloadHandler<YetaWrenchModeEvent> {
    override fun receive(payload: YetaWrenchModeEvent, context: ServerPlayNetworking.Context?) {
        // Execute on the main thread
        context?.player()?.server?.execute {
            val player = context.player()

            val itemStack = player.getStackInHand(player.activeHand)

            // Sanity check
            if (itemStack.item != ModItems.YETA_WRENCH) {
                throw RuntimeException("Received a YetaWrenchModeEvent packet for a non Yeta Wrench item")
            }

            itemStack.set(ModDataComponentTypes.YETA_WRENCH_MODE, payload.mode)
        }
    }
}