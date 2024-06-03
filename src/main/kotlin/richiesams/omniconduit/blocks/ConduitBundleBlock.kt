package richiesams.omniconduit.blocks

import com.mojang.serialization.MapCodec
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.state.StateManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import richiesams.omniconduit.blockentities.ConduitBundleBlockEntity
import richiesams.omniconduit.blockentities.ModBlockEntities


class ConduitBundleBlock(settings: Settings?) : BlockWithEntity(settings), BlockEntityProvider {
    companion object {
        val CODEC: MapCodec<ConduitBundleBlock> = createCodec(::ConduitBundleBlock)
    }

    override fun <T : BlockEntity> getTicker(world: World?, state: BlockState?, type: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return validateTicker(type, ModBlockEntities.CONDUIT_BUNDLE, ConduitBundleBlockEntity::tick)
    }

    override fun getRenderType(state: BlockState): BlockRenderType {
        // Invisible, because we render using a BlockEntityRenderer
        return BlockRenderType.INVISIBLE
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block, BlockState>) {
    }

    override fun getCodec(): MapCodec<out BlockWithEntity> {
        return CODEC
    }

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity {
        return ConduitBundleBlockEntity(pos, state)
    }

    override fun getOutlineShape(state: BlockState?, world: BlockView, pos: BlockPos?, context: ShapeContext?): VoxelShape {
        val blockEntity: BlockEntity? = world.getBlockEntity(pos)
        if (blockEntity is ConduitBundleBlockEntity) {
            return blockEntity.getOutlineShape()
        }

        return super.getOutlineShape(state, world, pos, context)
    }

    override fun neighborUpdate(state: BlockState?, world: World?, pos: BlockPos?, sourceBlock: Block?, sourcePos: BlockPos?, notify: Boolean) {
        val blockEntity = world!!.getBlockEntity(pos)
        if (blockEntity is ConduitBundleBlockEntity) {
            blockEntity.neighborUpdate()
        }

        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify)
    }
}
