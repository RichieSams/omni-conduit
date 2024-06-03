package richiesams.omniconduit.blockentities

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.World
import richiesams.omniconduit.api.conduits.*
import richiesams.omniconduit.conduits.ConduitShapeHelper
import richiesams.omniconduit.items.ConduitItem
import java.util.concurrent.atomic.AtomicReference


class ConduitBundleBlockEntity(pos: BlockPos?, state: BlockState?) : BlockEntity(ModBlockEntities.CONDUIT_BUNDLE, pos, state) {
    companion object {
        private const val coreHitboxExpansion: Double = 0.25 / 16.0
        private const val connectionHitboxExpansion: Double = 0.75 / 16.0

        fun tick(world: World, pos: BlockPos, state: BlockState, entity: ConduitBundleBlockEntity) {
            if (world.isClient) {
                return
            }

            var markDirty = false
            for (conduit in entity.conduitEntities) {
                markDirty = markDirty || conduit.tick(world, pos, state)
            }

            if (markDirty) {
                entity.markDirty()
            }
        }
    }

    private var conduitShape: AtomicReference<ConduitShape> = AtomicReference(ConduitShape(ArrayList(), ArrayList(), ArrayList()))
    private var conduitEntities: MutableList<ConduitEntity> = ArrayList()

    fun getConduitShape(): ConduitShape {
        return conduitShape.get()
    }

    fun canAddConduit(conduit: Conduit): Boolean {
        for (conduitEntity in conduitEntities) {
            val backingConduit: Conduit = conduitEntity.getBackingConduit()
            if (backingConduit == conduit) {
                return false
            }
        }

        return true
    }

    fun addConduit(serverPlayer: ServerPlayerEntity, hand: Hand): Boolean {
        val stack = serverPlayer.getStackInHand(hand)
        val item = stack.item
        if (item is ConduitItem) {
            val conduit: Conduit = item.conduit

            // Check if that type of conduit already exists in the bundle
            val iterator = conduitEntities.listIterator()
            while (iterator.hasNext()) {
                val conduitEntity: ConduitEntity = iterator.next()

                val backingConduit: Conduit = conduitEntity.getBackingConduit()

                // If the conduit is the same, it's a NOP
                if (conduit === backingConduit) {
                    return false
                }

                // If the conduit is the same type but not the exact same conduit,
                // (for example, a different tier) then replace it
                if (backingConduit.javaClass == conduit.javaClass) {
                    // Replace the existing conduit
                    // TODO: We'll need to do some kind of transfer of internal data
                    val newConduitEntity = conduit.createConduitEntity(this)

                    // Add an ItemStack of the existing conduit back to the user's inventory if able, or drop it
                    val oldConduitItemStack: ItemStack = conduitEntity.getBackingConduit().toItemStack()
                    if (!serverPlayer.inventory.insertStack(oldConduitItemStack)) {
                        ItemScatterer.spawn(world, pos, DefaultedList.copyOf(ItemStack.EMPTY, oldConduitItemStack))
                    }

                    markDirty()
                    conduitEntity.markConnectionsDirty()

                    return true
                }
            }

            // Conduit doesn't already exist
            // Add it
            conduitEntities.add(conduit.createConduitEntity(this))
            markDirty()
            return true
        }

        return false
    }

    override fun readNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registryLookup)

        val newConduits: MutableList<ConduitEntity> = ArrayList()
        val conduits = nbt.getList("Conduits", 10)
        for (i in conduits.indices) {
            val entity: ConduitEntity = ConduitEntity.fromNBT(this, conduits.getCompound(i))
            newConduits.add(entity)
        }

        conduitEntities = newConduits
        regenerateShape()
    }

    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        val conduits = NbtList()
        for (conduitEntity in conduitEntities) {
            val compound = NbtCompound()
            conduitEntity.writeNbt(compound)
            conduits.add(compound)
        }
        nbt.put("Conduits", conduits)

        super.writeNbt(nbt, registryLookup)
    }

    override fun markDirty() {
        super.markDirty()
        world!!.updateListeners(pos, world!!.getBlockState(pos), world!!.getBlockState(pos), Block.NOTIFY_LISTENERS)
        regenerateShape()
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound {
        return createNbt(registryLookup)
    }

    private fun regenerateShape() {
        val coreShapes = HashSet<CoreShape>()
        val connectionShapes = ArrayList<ConnectionShape>()

        var coreOutline: Box? = null
        val connectorOutlines = HashMap<Direction, Box>()

        var overrideOffset: ConduitOffset? = null
        if (conduitEntities.size == 1) {
            overrideOffset = ConduitOffset.NONE
        }

        for (conduitEntity in conduitEntities) {
            val backingConduit = conduitEntity.getBackingConduit()

            // We only want to add a single core for each offset type
            // So we track which are connected to
            var northSouthConnection = false
            var eastWestConnection = false
            var upDownConnection = false

            val connections = conduitEntity.getConnections()
            for (entry in connections) {
                var offset: ConduitOffset
                when (entry.key) {
                    Direction.NORTH, Direction.SOUTH -> {
                        offset = overrideOffset ?: backingConduit.northSouthOffset
                        northSouthConnection = true
                    }

                    Direction.EAST, Direction.WEST -> {
                        offset = overrideOffset ?: backingConduit.eastWestOffset
                        eastWestConnection = true
                    }

                    Direction.UP, Direction.DOWN -> {
                        offset = overrideOffset ?: backingConduit.upDownOffset
                        upDownConnection = true
                    }
                }

                connectionShapes.add(
                    ConnectionShape(
                        backingConduit.type,
                        backingConduit.connectorOuterSprite,
                        backingConduit.connectorInnerSprite,
                        ConduitShapeHelper.connectorFromOffset(offset, entry.key),
                        entry.key
                    )
                )

                val outline = ConduitShapeHelper.connectorOutlineFromOffset(offset, entry.key)
                val existingOutline = connectorOutlines.getOrDefault(entry.key, outline)

                connectorOutlines[entry.key] = outline.union(existingOutline)
            }

            // If there aren't any connections, then add a core at NorthSouth offset
            if (connections.isEmpty()) {
                val offset = overrideOffset ?: backingConduit.northSouthOffset
                coreShapes.add(
                    CoreShape(
                        backingConduit.type,
                        backingConduit.coreSprite,
                        ConduitShapeHelper.coreFromOffset(offset)
                    )
                )
                if (coreOutline == null) {
                    coreOutline = ConduitShapeHelper.coreOutlineFromOffset(offset)
                } else {
                    coreOutline = coreOutline.union(ConduitShapeHelper.coreOutlineFromOffset(offset))
                }
            } else {
                // Add the cores
                if (northSouthConnection) {
                    val offset = overrideOffset ?: backingConduit.northSouthOffset
                    coreShapes.add(
                        CoreShape(
                            backingConduit.type,
                            backingConduit.coreSprite,
                            ConduitShapeHelper.coreFromOffset(offset)
                        )
                    )
                    if (coreOutline == null) {
                        coreOutline = ConduitShapeHelper.coreOutlineFromOffset(offset)
                    } else {
                        coreOutline = coreOutline.union(ConduitShapeHelper.coreOutlineFromOffset(offset))
                    }
                }
                if (eastWestConnection) {
                    val offset = overrideOffset ?: backingConduit.eastWestOffset
                    coreShapes.add(
                        CoreShape(
                            backingConduit.type,
                            backingConduit.coreSprite,
                            ConduitShapeHelper.coreFromOffset(offset)
                        )
                    )
                    if (coreOutline == null) {
                        coreOutline = ConduitShapeHelper.coreOutlineFromOffset(offset)
                    } else {
                        coreOutline = coreOutline.union(ConduitShapeHelper.coreOutlineFromOffset(offset))
                    }
                }
                if (upDownConnection) {
                    val offset = overrideOffset ?: backingConduit.upDownOffset
                    coreShapes.add(
                        CoreShape(
                            backingConduit.type,
                            backingConduit.coreSprite,
                            ConduitShapeHelper.coreFromOffset(offset)
                        )
                    )
                    if (coreOutline == null) {
                        coreOutline = ConduitShapeHelper.coreOutlineFromOffset(offset)
                    } else {
                        coreOutline = coreOutline.union(ConduitShapeHelper.coreOutlineFromOffset(offset))
                    }
                }
            }
        }

        val outlines: List<Box> = connectorOutlines.values + listOf(coreOutline!!)

        // Now update the atomic, so the next render frame can see it
        conduitShape.set(ConduitShape(coreShapes.toList(), connectionShapes, outlines))
    }

    fun getOutlineShape(): VoxelShape {
        val shape = conduitShape.get()

        val voxels = ArrayList<VoxelShape>()
        for (box in shape.outlines) {
            voxels.add(VoxelShapes.cuboid(box))
        }

        return voxels.stream()
            .reduce { v1: VoxelShape?, v2: VoxelShape? -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }
            .orElseGet { VoxelShapes.fullCube() }
    }

    fun neighborUpdate() {
        for (conduitEntity in conduitEntities) {
            conduitEntity.markConnectionsDirty()
        }
    }
}