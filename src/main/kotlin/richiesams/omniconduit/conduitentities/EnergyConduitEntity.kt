package richiesams.omniconduit.conduitentities

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import richiesams.omniconduit.api.conduits.Conduit
import richiesams.omniconduit.api.conduits.ConduitConnection
import richiesams.omniconduit.api.conduits.ConduitEntity
import richiesams.omniconduit.blockentities.ConduitBundleBlockEntity

class EnergyConduitEntity(conduit: Conduit, blockEntity: ConduitBundleBlockEntity) : ConduitEntity(conduit, blockEntity) {
    override fun tick(world: World?, pos: BlockPos, state: BlockState): Boolean {
        var markDirty = false

        if (updateConnections) {
            for (direction in Direction.entries) {
                // TODO: We'll need to use fabric API calls to actually check if the other entity contains a conduit
                //       that we can connect to. Or a block that we can connect to
                if (world!!.getBlockEntity(pos.offset(direction)) is ConduitBundleBlockEntity) {
                    connections[direction] = ConduitConnection(terminated = false, input = false, output = false)
                    markDirty = true
                } else {
                    if (connections.remove(direction) != null) {
                        markDirty = true
                    }
                }
            }
            updateConnections = false
        }

        return markDirty
    }
}