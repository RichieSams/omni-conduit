package richiesams.omniconduit.blocks

import com.mojang.serialization.MapCodec
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.state.StateManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import richiesams.omniconduit.blockentities.ConduitBundleBlockEntity


class ConduitBundleBlock(settings: Settings?) : BlockWithEntity(settings), BlockEntityProvider {
    companion object {
        val CODEC: MapCodec<ConduitBundleBlock> = createCodec(::ConduitBundleBlock)
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
            return blockEntity.getRaycastShape()
        }

        return super.getOutlineShape(state, world, pos, context)
    }

    override fun getCollisionShape(state: BlockState?, world: BlockView, pos: BlockPos?, context: ShapeContext?): VoxelShape {
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is ConduitBundleBlockEntity) {
            return blockEntity.getCollisionShape()
        }

        return super.getCollisionShape(state, world, pos, context)
    }

    override fun getRaycastShape(state: BlockState?, world: BlockView?, pos: BlockPos?): VoxelShape {
        val blockEntity = world?.getBlockEntity(pos)
        if (blockEntity is ConduitBundleBlockEntity) {
            return blockEntity.getRaycastShape()
        }

        return super.getRaycastShape(state, world, pos)
    }
}
