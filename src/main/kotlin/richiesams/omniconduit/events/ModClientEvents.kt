package richiesams.omniconduit.events

import net.minecraft.client.MinecraftClient
import net.minecraft.item.Items
import net.minecraft.text.Text

object ModClientEvents {
    fun registerModClientEvents() {
        HudRenderCallback.EVENT.register(YetaWrenchOverlayRenderer::render)

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