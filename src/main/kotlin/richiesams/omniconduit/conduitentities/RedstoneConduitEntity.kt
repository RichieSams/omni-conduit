package richiesams.omniconduit.conduitentities

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import richiesams.omniconduit.api.BlockApiLookups
import richiesams.omniconduit.api.blockentities.ConduitBundleBlockEntity
import richiesams.omniconduit.api.conduits.*


class RedstoneConduitEntity(conduit: Conduit, blockEntity: ConduitBundleBlockEntity) : ConduitEntity(conduit, blockEntity) {
    private val adjacentConduitBundles: Array<BlockApiCache<ConduitBundleBlockEntity, Unit>?> = arrayOfNulls(6)

    private fun getAdjacentConduitBundle(direction: Direction): ConduitBundleBlockEntity? {
        var cache = adjacentConduitBundles[direction.id]
        if (cache == null) {
            cache = BlockApiCache.create(BlockApiLookups.CONDUIT_BUNDLE, blockEntity.world as ServerWorld, blockEntity.pos.offset(direction))
            adjacentConduitBundles[direction.id] = cache
        }

        return cache!!.find(null)
    }

    override fun tick(world: ServerWorld, pos: BlockPos, state: BlockState): Boolean {
        var markDirty = false

        if (updateConnections) {
            for (direction in Direction.entries) {
                // TODO: We'll need to use fabric API calls to actually check if the other entity contains a conduit
                //       that we can connect to. Or a block that we can connect to
                val otherConduitBundle = getAdjacentConduitBundle(direction)
                if (otherConduitBundle != null) {
                    if (otherConduitBundle.hasConduitOfType(conduit.javaClass)) {
                        if (otherConduitBundle.conduitCount() == 1) {
                            connections[direction] = ConduitConnection(ConduitConnectionType.SINGLE_CONDUIT, ConduitTerminationMode.NONE, DyeColor.RED, DyeColor.RED)
                        } else {
                            connections[direction] = ConduitConnection(ConduitConnectionType.MULTI_CONDUIT, ConduitTerminationMode.NONE, DyeColor.RED, DyeColor.RED)
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