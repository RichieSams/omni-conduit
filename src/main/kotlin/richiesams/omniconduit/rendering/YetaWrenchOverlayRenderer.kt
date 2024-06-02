package richiesams.omniconduit.rendering

import net.minecraft.client.gui.DrawContext
import richiesams.omniconduit.api.conduits.ConduitDisplayMode
import richiesams.omniconduit.events.ClientHandler
import richiesams.omniconduit.items.YetaWrenchItem
import richiesams.omniconduit.util.SpriteReference

object YetaWrenchOverlayRenderer {
    private const val renderDurationInTicks = 20

    var lastRenderedMode: String = ""
    var lastRenderedTick: Long = 0
    var renderTicksRemaining: Int = 0

    fun render(drawContext: DrawContext, tickDelta: Float) {
        // If the user doesn't have a wrench equipped, then we don't render anything
        val wrenchItem = YetaWrenchItem.getEquippedWrench() ?: return
        val currentMode = YetaWrenchItem.getCurrentMode(wrenchItem)

        val now = ClientHandler.getTicksElapsed()
        if (currentMode != lastRenderedMode) {
            lastRenderedMode = currentMode
            renderTicksRemaining = renderDurationInTicks
            lastRenderedTick = now
        }

        if (renderTicksRemaining > 0) {
            if (lastRenderedTick == -1L) {
                lastRenderedTick = now
            }

            while (lastRenderedTick < now) {
                lastRenderedTick++
                renderTicksRemaining--
            }

            val centerX = drawContext.scaledWindowWidth / 2
            val centerY = drawContext.scaledWindowHeight / 2

            renderPrevious(drawContext, ConduitDisplayMode.previous(currentMode).icon, centerX, centerY)
            renderActive(drawContext, ConduitDisplayMode.getByType(currentMode).icon, centerX, centerY)
            renderNext(drawContext, ConduitDisplayMode.next(currentMode).icon, centerX, centerY)
        } else {
            renderPermanentIcon(drawContext, ConduitDisplayMode.getByType(currentMode).icon)
        }
    }

    private fun renderPrevious(drawContext: DrawContext, icon: SpriteReference, centerX: Int, centerY: Int) {
        drawContext.drawTexture(
            icon.identifier.withPath("textures/${icon.identifier.path}.png"),
            centerX - 22, centerY - 20,
            12, 12,
            icon.uvFrom.x * 16, icon.uvFrom.y * 16,
            ((icon.uvTo.x - icon.uvFrom.x) * 16.0).toInt(), ((icon.uvTo.y - icon.uvFrom.y) * 16.0).toInt(),
            16, 16
        )
    }

    private fun renderActive(drawContext: DrawContext, icon: SpriteReference, centerX: Int, centerY: Int) {
        drawContext.drawTexture(
            icon.identifier.withPath("textures/${icon.identifier.path}.png"),
            centerX - 8, centerY - 24,
            16, 16,
            icon.uvFrom.x * 16, icon.uvFrom.y * 16,
            ((icon.uvTo.x - icon.uvFrom.x) * 16.0).toInt(), ((icon.uvTo.y - icon.uvFrom.y) * 16.0).toInt(),
            16, 16
        )
    }

    private fun renderNext(drawContext: DrawContext, icon: SpriteReference, centerX: Int, centerY: Int) {
        drawContext.drawTexture(
            icon.identifier.withPath("textures/${icon.identifier.path}.png"),
            centerX + 10, centerY - 20,
            12, 12,
            icon.uvFrom.x * 16, icon.uvFrom.y * 16,
            ((icon.uvTo.x - icon.uvFrom.x) * 16.0).toInt(), ((icon.uvTo.y - icon.uvFrom.y) * 16.0).toInt(),
            16, 16
        )
    }

    private fun renderPermanentIcon(drawContext: DrawContext, icon: SpriteReference) {
        drawContext.drawTexture(
            icon.identifier.withPath("textures/${icon.identifier.path}.png"),
            drawContext.scaledWindowWidth - 20, drawContext.scaledWindowHeight - 20,
            16, 16,
            icon.uvFrom.x * 16, icon.uvFrom.y * 16,
            ((icon.uvTo.x - icon.uvFrom.x) * 16.0).toInt(), ((icon.uvTo.y - icon.uvFrom.y) * 16.0).toInt(),
            16, 16
        )
    }
}