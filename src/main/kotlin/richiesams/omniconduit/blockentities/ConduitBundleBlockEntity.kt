package richiesams.omniconduit.blockentities

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import richiesams.omniconduit.conduits.ConduitShape

class ConduitBundleBlockEntity(pos: BlockPos?, state: BlockState?) : BlockEntity(ModBlockEntities.CONDUIT_BUNDLE, pos, state) {
    private var conduitShape: ConduitShape = ConduitShape(ArrayList(), ArrayList())


    fun getConduitShape(): ConduitShape {
        return conduitShape
    }
}