package richiesams.omniconduit.items

import net.minecraft.item.Item
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import richiesams.omniconduit.api.blockentities.ConduitBundleBlockEntity
import richiesams.omniconduit.api.conduits.Conduit
import richiesams.omniconduit.blocks.ModBlocks

class ConduitItem(val conduit: Conduit, settings: Settings?) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        if (world.isClient) {
            return ActionResult.success(true)
        }

        var pos = context.blockPos
        val player = context.player ?: throw RuntimeException("ConduitItem used by a non PlayerEntity")
        val hand = context.hand

        // First we check if the block we hit is a ConduitBundle, and if we can add to it
        var blockEntity = world.getBlockEntity(pos)
        if (blockEntity is ConduitBundleBlockEntity) {
            // See if we can add a conduit
            if (blockEntity.canAddConduit(conduit)) {
                blockEntity.addConduit(player as ServerPlayerEntity, hand)
                return ActionResult.SUCCESS
            }
        }

        // Next check if the placement position is a ConduitBundle
        // (AKA, we clicked through the empty space of a ConduitBundle to the block behind)
        val itemPlacementContext = ItemPlacementContext(context)
        pos = itemPlacementContext.blockPos
        blockEntity = world.getBlockEntity(pos)
        if (blockEntity is ConduitBundleBlockEntity) {
            // See if we can add a conduit
            if (blockEntity.canAddConduit(conduit)) {
                blockEntity.addConduit(player as ServerPlayerEntity, hand)
                return ActionResult.SUCCESS
            }
        }

        // If not, then we check if we can place a new conduit
        if (itemPlacementContext.canPlace()) {
            world.setBlockState(pos, ModBlocks.CONDUIT_BUNDLE.defaultState)

            blockEntity = world.getBlockEntity(pos)
            if (blockEntity !is ConduitBundleBlockEntity) {
                throw RuntimeException("A ConduitBundle was created without creating a corresponding ConduitBundleBlockEntity")
            }

            blockEntity.addConduit(player as ServerPlayerEntity, hand)
            return ActionResult.SUCCESS
        }

        return ActionResult.PASS
    }
}
