package richiesams.omniconduit.events

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import richiesams.omniconduit.items.YetaWrenchItem
import richiesams.omniconduit.rendering.YetaWrenchOverlayRenderer

object ModClientEvents {
    fun registerModClientEvents() {
        HudRenderCallback.EVENT.register(YetaWrenchOverlayRenderer::render)
        ClientTickEvents.END_CLIENT_TICK.register(ClientHandler::onClientTick)

        MouseEvents.MOUSE_WHEEL_SCROLLED.register { _: Double, dy: Double ->
            return@register YetaWrenchItem.onScrollWheel(dy)
        }
    }
}