package richiesams.omniconduit.screens

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import richiesams.omniconduit.api.conduits.ConduitEntity

class ConduitTerminatorScreenHandler(
    val conduitEntity: ConduitEntity,
    type: ScreenHandlerType<ScreenHandler?>?, syncID: Int,
    playerInventory: PlayerInventory,
    hotbarXOffset: Int, hotbarYOffset: Int,
    mainInventoryXOffset: Int, mainInventoryYOffset: Int
) : PlayerInventoryScreenHandler(type, syncID, playerInventory, hotbarXOffset, hotbarYOffset, mainInventoryXOffset, mainInventoryYOffset) {
    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        // TODO: Only allow filter transfers

        return ItemStack.EMPTY
    }
j
    override fun canUse(player: PlayerEntity?): Boolean {
        conduitEntity.

        player.pos.squaredDistanceTo()
        playerInventory.canPlayerUse()
        TODO("Not yet implemented")
    }
}