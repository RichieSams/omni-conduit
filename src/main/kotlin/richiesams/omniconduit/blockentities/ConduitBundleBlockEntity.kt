package richiesams.omniconduit.blockentities

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
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
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import richiesams.omniconduit.OmniConduitModBase
import richiesams.omniconduit.api.conduits.*
import richiesams.omniconduit.conduits.ConduitShapeHelper
import richiesams.omniconduit.items.ConduitItem
import java.util.concurrent.atomic.AtomicReference


class ConduitBundleBlockEntity(pos: BlockPos?, state: BlockState?) : BlockEntity(ModBlockEntities.CONDUIT_BUNDLE, pos, state) {
    companion object {
        private const val coreHitboxExpansion: Double = 0.25 / 16.0
        private const val connectionHitboxExpansion: Double = 0.75 / 16.0
    }

    private var conduitShape: AtomicReference<ConduitShape> = AtomicReference(ConduitShape(ArrayList(), ArrayList()))

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
        val cores = ArrayList<CoreShape>()
        val connections = ArrayList<ConnectionShape>()

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

            // TODO: For now we just create connections in ALL directions
            for (direction in Direction.entries) {
                var offset: ConduitOffset
                when (direction) {
                    Direction.NORTH, Direction.SOUTH -> {
                        offset = overrideOffset ?: backingConduit.NorthSouthOffset
                        northSouthConnection = true
                    }

                    Direction.EAST, Direction.WEST -> {
                        offset = overrideOffset ?: backingConduit.EastWestOffset
                        eastWestConnection = true
                    }

                    Direction.UP, Direction.DOWN -> {
                        offset = overrideOffset ?: backingConduit.UpDownOffset
                        upDownConnection = true
                    }
                }

                connections.add(
                    ConnectionShape(
                        backingConduit.ConnectorOuterSprite,
                        backingConduit.ConnectorInnerSprite,
                        ConduitShapeHelper.connectorFromOffset(offset, direction),
                        direction
                    )
                )
            }

            // If there aren't any connections, then add a core at NorthSouth offset
            // TODO: Update this when we stop adding all the connections
            if (false) {
                cores.add(
                    CoreShape(
                        backingConduit.CoreSprite,
                        ConduitShapeHelper.coreFromOffset(overrideOffset ?: backingConduit.NorthSouthOffset)
                    )
                )
            } else {
                // Add the cores
                if (northSouthConnection) {
                    cores.add(
                        CoreShape(
                            backingConduit.CoreSprite,
                            ConduitShapeHelper.coreFromOffset(overrideOffset ?: backingConduit.NorthSouthOffset)
                        )
                    )
                }
                if (eastWestConnection) {
                    cores.add(
                        CoreShape(
                            backingConduit.CoreSprite,
                            ConduitShapeHelper.coreFromOffset(overrideOffset ?: backingConduit.EastWestOffset)
                        )
                    )
                }
                if (upDownConnection) {
                    cores.add(
                        CoreShape(
                            backingConduit.CoreSprite,
                            ConduitShapeHelper.coreFromOffset(overrideOffset ?: backingConduit.UpDownOffset)
                        )
                    )
                }
            }
        }

        // Now update the atomic, so the next render frame can see it
        conduitShape.set(ConduitShape(cores, connections))
    }

    fun getOutlineShape(): VoxelShape {
        val client: MinecraftClient = MinecraftClient.getInstance()
        val hit: HitResult? = client.crosshairTarget

        if (hit is BlockHitResult) {
            val blockEntity = world!!.getBlockEntity(hit.blockPos)
            if (blockEntity is ConduitBundleBlockEntity) {
                OmniConduitModBase.LOGGER.info("Hit ConduitBundleblockEntity")
                
                val shape = blockEntity.conduitShape.get()

                // Fail safe
                if (shape.cores.isEmpty() && shape.connections.isEmpty()) {
                    return VoxelShapes.empty()
                }

                var hitPos = hit.getPos()
                hitPos = hitPos.subtract(Vec3d.of(hit.blockPos))

                hitPos = when (hit.side) {
                    Direction.DOWN -> {
                        hitPos.add(0.0, 0.01, 0.0)
                    }

                    Direction.UP -> {
                        hitPos.add(0.0, -0.01, 0.0)
                    }

                    Direction.NORTH -> {
                        hitPos.add(0.0, 0.0, 0.01)
                    }

                    Direction.SOUTH -> {
                        hitPos.add(0.0, 0.0, -0.01)
                    }

                    Direction.WEST -> {
                        hitPos.add(0.01, 0.0, 0.0)
                    }

                    Direction.EAST -> {
                        hitPos.add(-0.01, 0.0, 0.0)
                    }

                    null -> {
                        throw RuntimeException("RayCast didn't hit a direction")
                    }
                }
                for (core in shape.cores) {
                    val expandedBox = core.box.expand(coreHitboxExpansion)
                    if (expandedBox.contains(hitPos)) {
                        return VoxelShapes.cuboid(expandedBox)
                    }
                }
                for (connection in shape.connections) {
                    val expandedBox = when (connection.direction) {
                        Direction.UP, Direction.DOWN -> {
                            connection.box.expand(connectionHitboxExpansion, 0.0, connectionHitboxExpansion)
                        }

                        Direction.NORTH, Direction.SOUTH -> {
                            connection.box.expand(connectionHitboxExpansion, connectionHitboxExpansion, 0.0)
                        }

                        Direction.EAST, Direction.WEST -> {
                            connection.box.expand(0.0, connectionHitboxExpansion, connectionHitboxExpansion)
                        }
                    }
                    if (expandedBox.contains(hitPos)) {
                        return VoxelShapes.cuboid(expandedBox)
                    }
                }
            }
        }

        return VoxelShapes.empty()
    }

    fun getCollisionShape(): VoxelShape {
        val shape = conduitShape.get()

        // Fail-safe
        if (shape.cores.isEmpty() && shape.connections.isEmpty()) {
            return VoxelShapes.fullCube()
        }

        val voxels = ArrayList<VoxelShape>()
        if (shape.cores.isNotEmpty()) {
            voxels.add(VoxelShapes.cuboid(0.2, 0.2, 0.2, 0.8, 0.8, 0.8))
        }

        val directions = HashSet<Direction>()
        for (connection in shape.connections) {
            directions.add(connection.direction)
        }
        for (direction in directions) {
            when (direction) {
                Direction.DOWN -> {
                    voxels.add(VoxelShapes.cuboid(0.25, 0.0, 0.25, 0.75, 0.2, 0.75))
                }

                Direction.UP -> {
                    voxels.add(VoxelShapes.cuboid(0.25, 0.8, 0.25, 0.75, 1.0, 0.75))
                }

                Direction.NORTH -> {
                    voxels.add(VoxelShapes.cuboid(0.25, 0.25, 0.0, 0.75, 0.75, 0.2))
                }

                Direction.SOUTH -> {
                    voxels.add(VoxelShapes.cuboid(0.25, 0.25, 0.8, 0.75, 0.75, 1.0))
                }

                Direction.WEST -> {
                    voxels.add(VoxelShapes.cuboid(0.0, 0.25, 0.25, 0.2, 0.75, 0.75))
                }

                Direction.EAST -> {
                    voxels.add(VoxelShapes.cuboid(0.8, 0.25, 0.25, 1.0, 0.75, 0.75))
                }
            }
        }

        return voxels.stream()
            .reduce { v1: VoxelShape?, v2: VoxelShape? -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }
            .orElseGet { VoxelShapes.fullCube() }
    }

    fun getRaycastShape(): VoxelShape {
        val shape = conduitShape.get()

        val voxels = ArrayList<VoxelShape>()
        for (core in shape.cores) {
            voxels.add(
                VoxelShapes.cuboid(
                    core.box.minX, core.box.minY, core.box.minZ,
                    core.box.maxX, core.box.maxY, core.box.maxZ
                )
            )
        }

        for (connection in shape.connections) {
            voxels.add(
                VoxelShapes.cuboid(
                    connection.box.minX, connection.box.minY, connection.box.minZ,
                    connection.box.maxX, connection.box.maxY, connection.box.maxZ
                )
            )
        }

        return voxels.stream()
            .reduce { v1: VoxelShape?, v2: VoxelShape? -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }
            .orElseGet { VoxelShapes.fullCube() }
    }
}