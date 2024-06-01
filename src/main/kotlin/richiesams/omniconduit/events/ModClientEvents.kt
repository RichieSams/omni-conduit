package richiesams.omniconduit.events

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.item.Items
import net.minecraft.text.Text
import richiesams.omniconduit.rendering.YetaWrenchOverlayRenderer

object ModClientEvents {
    fun registerModClientEvents() {
        HudRenderCallback.EVENT.register(YetaWrenchOverlayRenderer::render)
        ClientTickEvents.END_CLIENT_TICK.register(ClientHandler::onClientTick)

        MouseEvents.MOUSE_WHEEL_SCROLLED.register { dx: Double, dy: Double ->
            val player = MinecraftClient.getInstance().player ?: return@register false

            if (player.isSneaking && player.getStackInHand(player.activeHand).item == Items.GRASS_BLOCK) {
                player.sendMessage(Text.literal("Scrolled dx: $dx dy: $dy"))
                return@register true
            }

            return@register false
        }
    }
}