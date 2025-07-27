package richiesams.omniconduit.screens

import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import org.apache.commons.lang3.Range


abstract class PlayerInventoryScreenHandler(
    type: ScreenHandlerType<ScreenHandler?>?, syncID: Int,
    val playerInventory: PlayerInventory,
    hotbarXOffset: Int, hotbarYOffset: Int,
    mainInventoryXOffset: Int, mainInventoryYOffset: Int,
) : ScreenHandler(type, syncID) {
    protected val hotbarSlotIndicies: Range<Int>
    protected val mainInventorySlotIndices: Range<Int>

    init {
        // Add the hotbar slots
        var startIndex = slots.size
        for (i in 0..8) {
            addSlot(Slot(playerInventory, startIndex + i, hotbarXOffset + i * 18, hotbarYOffset))
        }
        hotbarSlotIndicies = Range.of(startIndex, slots.size - 1)

        // Add the main inventory slots
        startIndex = slots.size
        for (y in 0..2) {
            for (x in 0..8) {
                addSlot(Slot(playerInventory, x + y * 9 + 9, mainInventoryXOffset + x * 18, mainInventoryYOffset + y * 18))
            }
        }
        mainInventorySlotIndices = Range.of(startIndex, slots.size - 1)
    }
}