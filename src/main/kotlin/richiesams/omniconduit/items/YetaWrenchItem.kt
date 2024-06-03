package richiesams.omniconduit.items

import net.minecraft.client.MinecraftClient
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import richiesams.omniconduit.api.conduits.ConduitDisplayMode
import richiesams.omniconduit.events.YetaWrenchModeEvent

class YetaWrenchItem(settings: Settings?) : Item(settings) {
    companion object {
        fun onScrollWheel(dy: Double): Boolean {
            if (dy == 0.0) {
                return false;
            }

            val player = MinecraftClient.getInstance().player ?: return false
            val stackInHand = player.getStackInHand(player.activeHand)

            // Only consume the event if the player is holding the wrench
            if (stackInHand.item != ModItems.YETA_WRENCH) {
                return false
            }

            // AND they're sneaking
            if (!player.isSneaking) {
                return false
            }

            // Figure out what mode is next

            // We're the client, so we can't change the Component data ourselves
            // We have to send a packet to the server to do it
            val currentMode = stackInHand.components.get(ModDataComponentTypes.YETA_WRENCH_MODE) ?: throw RuntimeException("Yeta wrench found without YETA_WRENCH_MODE Component data")

            if (dy < 0.0) {
                YetaWrenchModeEvent.sendPacketToServer(ConduitDisplayMode.next(currentMode).type)
            } else {
                YetaWrenchModeEvent.sendPacketToServer(ConduitDisplayMode.previous(currentMode).type)
            }

            return true
        }

        fun getEquippedWrench(): ItemStack? {
            val player = MinecraftClient.getInstance().player ?: return null
            val stackInHand = player.getStackInHand(player.activeHand)

            // If the user isn't holding a yeta wrench, default to ALL
            if (stackInHand.item != ModItems.YETA_WRENCH) {
                return null
            }

            return stackInHand
        }

        fun getCurrentMode(stack: ItemStack?): String {
            // If the user isn't holding a yeta wrench, default to ALL
            if (stack == null || stack.item != ModItems.YETA_WRENCH) {
                return ConduitDisplayMode.ALL.type
            }

            val mode = stack.components.get(ModDataComponentTypes.YETA_WRENCH_MODE) ?: throw RuntimeException("Yeta wrench found without YETA_WRENCH_MODE Component data")
            return mode
        }
    }
}