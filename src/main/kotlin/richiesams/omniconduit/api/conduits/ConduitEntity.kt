package richiesams.omniconduit.api.conduits

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import richiesams.omniconduit.api.OnmiConduitRegistries
import richiesams.omniconduit.blockentities.ConduitBundleBlockEntity


abstract class ConduitEntity protected constructor(
    protected val conduit: Conduit,
    protected val blockEntity: ConduitBundleBlockEntity
) {
    protected var connections = HashMap<Direction, ConduitConnection>()

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
            val terminated = connection.getBoolean("Terminated")
            val input = connection.getBoolean("Input")
            val output = connection.getBoolean("Output")

            newConnections[direction] = ConduitConnection(terminated, input, output)
        }

        this.connections = newConnections
    }

    fun writeNbt(nbt: NbtCompound) {
        nbt.putString("id", OnmiConduitRegistries.CONDUIT.getId(conduit).toString())
        val connectionsList = NbtList()
        for (entry in connections.entries) {
            val connection = NbtCompound()
            connection.putString("Direction", entry.key.toString())
            connection.putBoolean("Terminated", entry.value.terminated)
            connection.putBoolean("Input", entry.value.input)
            connection.putBoolean("Output", entry.value.output)

            connectionsList.add(connection)
        }
        nbt.put("Connections", connectionsList)
    }

    fun markConnectionsDirty() {
        //updateConnections = true
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