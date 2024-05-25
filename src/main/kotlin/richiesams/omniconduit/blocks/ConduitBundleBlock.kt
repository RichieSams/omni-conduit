package richiesams.omniconduit.blocks

import com.mojang.serialization.MapCodec
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.state.StateManager
import net.minecraft.util.math.BlockPos
import richiesams.omniconduit.blockentities.ConduitBundleBlockEntity

class ConduitBundleBlock(settings: Settings?) : BlockWithEntity(settings), BlockEntityProvider {
    companion object {
        val CODEC: MapCodec<ConduitBundleBlock> = createCodec(::ConduitBundleBlock)
    }

    override fun appendProperties(stateManager: StateManager.Builder<Block, BlockState>) {
    }

    override fun getCodec(): MapCodec<out BlockWithEntity> {
        return CODEC
    }

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity? {
        return ConduitBundleBlockEntity(pos, state)
    }

}
