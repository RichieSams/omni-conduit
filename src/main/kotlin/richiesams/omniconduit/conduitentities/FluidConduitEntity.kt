package richiesams.omniconduit.conduitentities

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import richiesams.omniconduit.api.BlockApiLookups
import richiesams.omniconduit.api.blockentities.ConduitBundleBlockEntity
import richiesams.omniconduit.api.conduits.Conduit
import richiesams.omniconduit.api.conduits.ConduitConnection
import richiesams.omniconduit.api.conduits.ConduitConnectionType
import richiesams.omniconduit.api.conduits.ConduitEntity

class FluidConduitEntity(conduit: Conduit, blockEntity: ConduitBundleBlockEntity) : ConduitEntity(conduit, blockEntity) {
    private val adjacentConduitBundles: Array<BlockApiCache<ConduitBundleBlockEntity, Unit>?>

    init {
        val world = blockEntity.world
        if (world is ServerWorld) {
            val pos = blockEntity.pos
            adjacentConduitBundles = arrayOf(
                BlockApiCache.create(BlockApiLookups.CONDUIT_BUNDLE, world, pos.offset(Direction.DOWN)),
                BlockApiCache.create(BlockApiLookups.CONDUIT_BUNDLE, world, pos.offset(Direction.UP)),
                BlockApiCache.create(BlockApiLookups.CONDUIT_BUNDLE, world, pos.offset(Direction.NORTH)),
                BlockApiCache.create(BlockApiLookups.CONDUIT_BUNDLE, world, pos.offset(Direction.SOUTH)),
                BlockApiCache.create(BlockApiLookups.CONDUIT_BUNDLE, world, pos.offset(Direction.WEST)),
                BlockApiCache.create(BlockApiLookups.CONDUIT_BUNDLE, world, pos.offset(Direction.EAST)),
            )
        } else {
            adjacentConduitBundles = arrayOfNulls(6)
        }
    }

    override fun tick(world: ServerWorld, pos: BlockPos, state: BlockState): Boolean {
        var markDirty = false

        if (updateConnections) {
            for (direction in Direction.entries) {
                // TODO: We'll need to use fabric API calls to actually check if the other entity contains a conduit
                //       that we can connect to. Or a block that we can connect to
                val otherConduitBundle = adjacentConduitBundles[direction.id]!!.find(null)
                if (otherConduitBundle != null) {
                    if (otherConduitBundle.hasConduitOfType(conduit.javaClass)) {
                        if (otherConduitBundle.conduitCount() == 1) {
                            connections[direction] = ConduitConnection(ConduitConnectionType.CONDUIT_SINGLE, input = false, output = false)
                        } else {
                            connections[direction] = ConduitConnection(ConduitConnectionType.CONDUIT, input = false, output = false)
                        }

                        markDirty = true
                        continue
                    }
                }

                if (connections.remove(direction) != null) {
                    markDirty = true
                }
            }
            updateConnections = false
        }

        return markDirty
    }
}