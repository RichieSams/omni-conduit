package richiesams.omniconduit.api.conduits

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import richiesams.omniconduit.api.OnmiConduitRegistries
import richiesams.omniconduit.api.blockentities.ConduitBundleBlockEntity


abstract class ConduitEntity protected constructor(
    protected val conduit: Conduit,
    protected val blockEntity: ConduitBundleBlockEntity
) {
    protected var connections = HashMap<Direction, ConduitConnection>()
    protected var updateConnections: Boolean = true

    abstract fun tick(world: ServerWorld, pos: BlockPos, state: BlockState): Boolean

    fun getBackingConduit(): Conduit {
        return conduit
    }

    fun readNbt(nbt: NbtCompound) {
        val newConnections = HashMap<Direction, ConduitConnection>()
        val connectionsList = nbt.getList("Connections", 10)
        for (i in connectionsList.indices) {
            val connection = connectionsList.getCompound(i)
            val direction: Direction =
                Direction.byName(connection.getString("Direction")) ?: throw RuntimeException("Invalid direction value")
            val connectionType = ConduitConnectionType.valueOf(connection.getString("Type"))
            val terminationMode = ConduitTerminationMode.valueOf(connection.getString("TerminationMode"))
            val terminationInputChannel = DyeColor.byName(connection.getString("TerminationInputChannel"), null)
            val terminationOutputChannel = DyeColor.byName(connection.getString("TerminationOutputChannel"), null)

            newConnections[direction] = ConduitConnection(connectionType, terminationMode, terminationInputChannel!!, terminationOutputChannel!!)
        }

        this.connections = newConnections
    }

    fun writeNbt(nbt: NbtCompound) {
        nbt.putString("id", OnmiConduitRegistries.CONDUIT.getId(conduit).toString())
        val connectionsList = NbtList()
        for (entry in connections.entries) {
            val connection = NbtCompound()
            connection.putString("Direction", entry.key.toString())
            connection.putString("Type", entry.value.type.toString())
            connection.putString("TerminationMode", entry.value.terminationMode.toString())
            connection.putString("TerminationInputChannel", entry.value.terminationInputChannel.toString())
            connection.putString("TerminationOutputChannel", entry.value.terminationOutputChannel.toString())

            connectionsList.add(connection)
        }
        nbt.put("Connections", connectionsList)
    }

    fun markConnectionsDirty() {
        updateConnections = true
    }

    fun getConnections(): Map<Direction, ConduitConnection> {
        return connections
    }

    companion object {
        fun fromNBT(blockEntity: ConduitBundleBlockEntity, nbt: NbtCompound): ConduitEntity {
            val identifierStr = nbt.getString("id")
            val identifier = Identifier.tryParse(identifierStr) ?: throw RuntimeException("Conduit entity has invalid type: $identifierStr")
            val conduit = OnmiConduitRegistries.CONDUIT.get(identifier) ?: throw RuntimeException("Failed to find matching Conduit")

            val conduitEntity = conduit.createConduitEntity(blockEntity)
            conduitEntity.readNbt(nbt)

            return conduitEntity
        }
    }
}