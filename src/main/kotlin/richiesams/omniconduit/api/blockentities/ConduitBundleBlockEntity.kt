package richiesams.omniconduit.api.blockentities

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
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.World
import richiesams.omniconduit.api.conduits.*
import richiesams.omniconduit.conduits.ConduitShapeHelper
import richiesams.omniconduit.items.ConduitItem
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max


class ConduitBundleBlockEntity(pos: BlockPos?, state: BlockState?) : BlockEntity(OmniConduitBlockEntities.CONDUIT_BUNDLE, pos, state) {
    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, entity: ConduitBundleBlockEntity) {
            if (world.isClient || world !is ServerWorld) {
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

    class ConduitReference(
        val conduitEntity: ConduitEntity,
        val direction: Direction
    )

    private var conduitEntities: MutableList<ConduitEntity> = ArrayList()

    private var conduitRenderShape: AtomicReference<ConduitRenderShape> = AtomicReference(ConduitRenderShape(ArrayList(), ArrayList()))

    private var connectionReferences: HashMap<Box, ConduitReference> = HashMap()
    private var terminationReferences: HashMap<Box, ConduitReference> = HashMap()
    private var coreReferences: HashMap<Box, ConduitEntity> = HashMap()
    private var boundingBox: VoxelShape = VoxelShapes.empty()


    fun getRenderShape(): ConduitRenderShape {
        return conduitRenderShape.get()
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
                    world!!.updateNeighborsAlways(pos, cachedState.block)

                    return true
                }
            }

            // Conduit doesn't already exist
            // Add it
            conduitEntities.add(conduit.createConduitEntity(this))

            markDirty()
            world!!.updateNeighborsAlways(pos, cachedState.block)

            return true
        }

        return false
    }

    fun <T> hasConduitOfType(clazz: Class<T>): Boolean where T : Conduit {
        for (entity in conduitEntities) {
            if (entity.getBackingConduit().javaClass == clazz) {
                return true
            }
        }

        return false
    }

    fun conduitCount(): Int {
        return conduitEntities.size
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
        regenerateShapesAndLookups()
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
        regenerateShapesAndLookups()
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound {
        return createNbt(registryLookup)
    }

    private fun regenerateShapesAndLookups() {
        // TODO: We need to have a lookup table (or multiple) that says, "this box represents X connection / core / termination"
        //       So we can do outlines and click events. IE, wrench click a connection to remove it, or add it back
        //
        // Maybe we de-normalize it though. Have a render-only struct that's POD and behind an atomic. And then have the more
        // complicated lookup in the BlockEntity itself, but only usable outside of rendering


        val coreShapes = HashSet<CoreShape>()
        val connectionShapes = ArrayList<ConnectionShape>()

        var coreBoundingBox: Box? = null
        val connectorBoundingBoxes = HashMap<Direction, Box>()

        val connectionRefs = HashMap<Box, ConduitReference>()
        val terminationRefs = HashMap<Box, ConduitReference>()
        val coreRefs = HashMap<Box, ConduitEntity>()

        var overrideOffset: ConduitOffset? = null
        if (conduitEntities.size == 1) {
            overrideOffset = ConduitOffset.NONE
        }

        for (conduitEntity in conduitEntities) {
            val backingConduit = conduitEntity.getBackingConduit()

            // We only want to add a single core for each offset type
            // So we track which are connected to
            var northSouthCore = false
            var eastWestCore = false
            var upDownCore = false
            var noneCore = false

            val connections = conduitEntity.getConnections()
            for ((direction, connection) in connections) {
                var offset: ConduitOffset
                when (connection.type) {
                    ConduitConnectionType.MULTI_CONDUIT -> {
                        when (direction) {
                            Direction.NORTH, Direction.SOUTH -> {
                                offset = overrideOffset ?: backingConduit.northSouthOffset
                                northSouthCore = true
                            }

                            Direction.EAST, Direction.WEST -> {
                                offset = overrideOffset ?: backingConduit.eastWestOffset
                                eastWestCore = true
                            }

                            Direction.UP, Direction.DOWN -> {
                                offset = overrideOffset ?: backingConduit.upDownOffset
                                upDownCore = true
                            }
                        }
                    }

                    ConduitConnectionType.SINGLE_CONDUIT -> {
                        offset = ConduitOffset.NONE
                        noneCore = true
                    }

                    ConduitConnectionType.TERMINATION -> {
                        TODO()
                    }
                }

                connectionShapes.add(
                    ConnectionShape(
                        backingConduit.type,
                        backingConduit.connectorOuterSprite,
                        backingConduit.connectorInnerSprite,
                        ConduitShapeHelper.connectorFromOffset(offset, direction),
                        direction
                    )
                )

                // Define the outline box
                val outline = ConduitShapeHelper.connectorOutlineFromOffset(offset, direction)
                connectionRefs[outline] = ConduitReference(conduitEntity, direction)

                // The bounding box is the union of all outlines for a given direction
                val existingOutline = connectorBoundingBoxes.getOrDefault(direction, outline)
                connectorBoundingBoxes[direction] = outline.union(existingOutline)
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

                // Define the outline box
                val outline = ConduitShapeHelper.coreOutlineFromOffset(offset)
                coreRefs[outline] = conduitEntity

                // The bounding box is the union of all core outlines
                if (coreBoundingBox == null) {
                    coreBoundingBox = outline
                } else {
                    coreBoundingBox = coreBoundingBox.union(ConduitShapeHelper.coreOutlineFromOffset(offset))
                }
            } else {
                // Add the cores
                if (northSouthCore) {
                    val offset = overrideOffset ?: backingConduit.northSouthOffset
                    coreShapes.add(
                        CoreShape(
                            backingConduit.type,
                            backingConduit.coreSprite,
                            ConduitShapeHelper.coreFromOffset(offset)
                        )
                    )

                    // Define the outline box
                    val outline = ConduitShapeHelper.coreOutlineFromOffset(offset)
                    coreRefs[outline] = conduitEntity

                    // The bounding box is the union of all core outlines
                    if (coreBoundingBox == null) {
                        coreBoundingBox = outline
                    } else {
                        coreBoundingBox = coreBoundingBox.union(ConduitShapeHelper.coreOutlineFromOffset(offset))
                    }
                }
                if (eastWestCore) {
                    val offset = overrideOffset ?: backingConduit.eastWestOffset
                    coreShapes.add(
                        CoreShape(
                            backingConduit.type,
                            backingConduit.coreSprite,
                            ConduitShapeHelper.coreFromOffset(offset)
                        )
                    )

                    // Define the outline box
                    val outline = ConduitShapeHelper.coreOutlineFromOffset(offset)
                    coreRefs[outline] = conduitEntity

                    // The bounding box is the union of all core outlines
                    if (coreBoundingBox == null) {
                        coreBoundingBox = outline
                    } else {
                        coreBoundingBox = coreBoundingBox.union(ConduitShapeHelper.coreOutlineFromOffset(offset))
                    }
                }
                if (upDownCore) {
                    val offset = overrideOffset ?: backingConduit.upDownOffset
                    coreShapes.add(
                        CoreShape(
                            backingConduit.type,
                            backingConduit.coreSprite,
                            ConduitShapeHelper.coreFromOffset(offset)
                        )
                    )

                    // Define the outline box
                    val outline = ConduitShapeHelper.coreOutlineFromOffset(offset)
                    coreRefs[outline] = conduitEntity

                    // The bounding box is the union of all core outlines
                    if (coreBoundingBox == null) {
                        coreBoundingBox = outline
                    } else {
                        coreBoundingBox = coreBoundingBox.union(ConduitShapeHelper.coreOutlineFromOffset(offset))
                    }
                }
                if (noneCore) {
                    coreShapes.add(
                        CoreShape(
                            backingConduit.type,
                            backingConduit.coreSprite,
                            ConduitShapeHelper.coreFromOffset(ConduitOffset.NONE)
                        )
                    )

                    // Define the outline box
                    val outline = ConduitShapeHelper.coreOutlineFromOffset(ConduitOffset.NONE)
                    coreRefs[outline] = conduitEntity

                    // The bounding box is the union of all core outlines
                    if (coreBoundingBox == null) {
                        coreBoundingBox = outline
                    } else {
                        coreBoundingBox = coreBoundingBox.union(ConduitShapeHelper.coreOutlineFromOffset(ConduitOffset.NONE))
                    }
                }
            }
        }

        // Update the entity variables
        connectionReferences = connectionRefs
        terminationReferences = terminationRefs
        coreReferences = coreRefs

        // The bounding box is the Boolean OR of all the individual bounding boxes
        val boundingBoxes: List<Box> = connectorBoundingBoxes.values + listOf(coreBoundingBox!!)
        boundingBox = boundingBoxes.stream()
            .map { box: Box -> VoxelShapes.cuboid(box) }
            .reduce { v1: VoxelShape, v2: VoxelShape -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }
            .orElseGet { VoxelShapes.fullCube() }

        // Now update the atomic, so the next render frame can see it
        conduitRenderShape.set(ConduitRenderShape(coreShapes.toList(), connectionShapes))
    }

    fun getBoundingBoxShape(): VoxelShape {
        return boundingBox
    }

    fun getOutlineShape(client: MinecraftClient): VoxelShape {
        val maxDistance = max(client.player!!.blockInteractionRange, client.player!!.entityInteractionRange)

        val start: Vec3d = client.cameraEntity!!.getClientCameraPosVec(client.tickDelta)
        val rotation: Vec3d = client.cameraEntity!!.getRotationVec(client.tickDelta)
        val end = start.add(rotation.x * maxDistance, rotation.y * maxDistance, rotation.z * maxDistance)

        var minDistance = Double.MAX_VALUE
        var closestHit: Box? = null
        for ((box, _) in connectionReferences) {
            val hit = box.offset(this.pos).raycast(start, end)
            if (!hit.isEmpty) {
                val distance = start.squaredDistanceTo(hit.get())
                if (distance < minDistance) {
                    minDistance = distance
                    closestHit = box
                }
            }
        }
        for ((box, _) in terminationReferences) {
            val hit = box.offset(this.pos).raycast(start, end)
            if (!hit.isEmpty) {
                val distance = start.squaredDistanceTo(hit.get())
                if (distance < minDistance) {
                    minDistance = distance
                    closestHit = box
                }
            }
        }
        for ((box, _) in coreReferences) {
            val hit = box.offset(this.pos).raycast(start, end)
            if (!hit.isEmpty) {
                val distance = start.squaredDistanceTo(hit.get())
                if (distance < minDistance) {
                    minDistance = distance
                    closestHit = box
                }
            }
        }

        return if (closestHit != null) VoxelShapes.cuboid(closestHit) else VoxelShapes.empty()
    }

    fun neighborUpdate() {
        for (conduitEntity in conduitEntities) {
            conduitEntity.markConnectionsDirty()
        }
    }
}