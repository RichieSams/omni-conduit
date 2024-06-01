package richiesams.omniconduit.rendering

import net.minecraft.client.gui.DrawContext
import richiesams.omniconduit.conduits.ModConduits
import richiesams.omniconduit.util.SpriteReference

object YetaWrenchOverlayRenderer {
    fun render(drawContext: DrawContext, tickDelta: Float) {
        val centerX = drawContext.scaledWindowWidth / 2
        val centerY = drawContext.scaledWindowHeight / 2

        renderPrevious(drawContext, ModConduits.ITEM_CONDUIT.icon, centerX, centerY)
        renderActive(drawContext, ModConduits.BASIC_ENERGY_CONDUIT.icon, centerX, centerY)
        renderNext(drawContext, ModConduits.BASIC_FLUID_CONDUIT.icon, centerX, centerY)
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
}