package richiesams.omniconduit.conduits


import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import richiesams.omniconduit.api.conduits.ConduitOffset

object ConduitShapeHelper {
    fun coreFromOffset(offset: ConduitOffset): Box {
        var cuboid = Box(
            6.5, 6.5, 6.5,
            9.5, 9.5, 9.5
        )

        when (offset) {
            ConduitOffset.NONE -> {
                // Nothing to do
            }

            ConduitOffset.UP -> {
                cuboid = cuboid.offset(0.0, 3.0, 0.0)
            }

            ConduitOffset.DOWN -> {
                cuboid = cuboid.offset(0.0, -3.0, 0.0)
            }

            ConduitOffset.NORTH -> {
                cuboid = cuboid.offset(0.0, 0.0, 3.0)
            }

            ConduitOffset.SOUTH -> {
                cuboid = cuboid.offset(0.0, 0.0, -3.0)
            }

            ConduitOffset.EAST -> {
                cuboid = cuboid.offset(3.0, 0.0, 0.0)
            }

            ConduitOffset.WEST -> {
                cuboid = cuboid.offset(-3.0, 0.0, 0.0)
            }

            ConduitOffset.UP_NORTH -> {
                cuboid = cuboid.offset(0.0, 3.0, 3.0)
            }

            ConduitOffset.UP_SOUTH -> {
                cuboid = cuboid.offset(0.0, 3.0, -3.0)
            }

            ConduitOffset.UP_EAST -> {
                cuboid = cuboid.offset(3.0, 3.0, 0.0)
            }

            ConduitOffset.UP_WEST -> {
                cuboid = cuboid.offset(-3.0, 3.0, 0.0)
            }

            ConduitOffset.DOWN_NORTH -> {
                cuboid = cuboid.offset(0.0, -3.0, 3.0)
            }

            ConduitOffset.DOWN_SOUTH -> {
                cuboid = cuboid.offset(0.0, -3.0, -3.0)
            }

            ConduitOffset.DOWN_EAST -> {
                cuboid = cuboid.offset(3.0, -3.0, 0.0)
            }

            ConduitOffset.DOWN_WEST -> {
                cuboid = cuboid.offset(-3.0, -3.0, 0.0)
            }

            ConduitOffset.NORTH_EAST -> {
                cuboid = cuboid.offset(3.0, 0.0, 3.0)
            }

            ConduitOffset.NORTH_WEST -> {
                cuboid = cuboid.offset(-3.0, 0.0, 3.0)
            }

            ConduitOffset.SOUTH_EAST -> {
                cuboid = cuboid.offset(3.0, 0.0, -3.0)
            }

            ConduitOffset.SOUTH_WEST -> {
                cuboid = cuboid.offset(-3.0, 0.0, -3.0)
            }
        }

        // Normalize and return
        return Box(
            cuboid.minX / 16.0, cuboid.minY / 16.0, cuboid.minZ / 16.0,
            cuboid.maxX / 16.0, cuboid.maxY / 16.0, cuboid.maxZ / 16.0
        )
    }

    fun connectorFromOffset(offset: ConduitOffset, connectionDirection: Direction): Box {
        // Create the base Box
        var connectorCuboid = when (connectionDirection) {
            Direction.DOWN -> {
                Box(
                    7.0, 0.0, 7.0,
                    9.0, 6.5, 9.0
                )
            }

            Direction.UP -> {
                Box(
                    7.0, 9.5, 7.0,
                    9.0, 16.0, 9.0
                )
            }

            Direction.NORTH -> {
                Box(
                    7.0, 7.0, 0.0,
                    9.0, 9.0, 6.5
                )
            }

            Direction.SOUTH -> {
                Box(
                    7.0, 7.0, 9.5,
                    9.0, 9.0, 16.0
                )
            }

            Direction.WEST -> {
                Box(
                    0.0, 7.0, 7.0,
                    6.5, 9.0, 9.0
                )
            }

            Direction.EAST -> {
                Box(
                    9.5, 7.0, 7.0,
                    16.0, 9.0, 9.0
                )
            }
        }

        // Now offset it
        when (offset) {
            ConduitOffset.NONE -> {
                // Nothing to do
            }

            ConduitOffset.UP -> {
                connectorCuboid = connectorCuboid.offset(0.0, 3.0, 0.0)
            }

            ConduitOffset.DOWN -> {
                connectorCuboid = connectorCuboid.offset(0.0, -3.0, 0.0)
            }

            ConduitOffset.NORTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, 0.0, 3.0)
            }

            ConduitOffset.SOUTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, 0.0, -3.0)
            }

            ConduitOffset.EAST -> {
                connectorCuboid = connectorCuboid.offset(3.0, 0.0, 0.0)
            }

            ConduitOffset.WEST -> {
                connectorCuboid = connectorCuboid.offset(-3.0, 0.0, 0.0)
            }

            ConduitOffset.UP_NORTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, 3.0, 3.0)
            }

            ConduitOffset.UP_SOUTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, 3.0, -3.0)
            }

            ConduitOffset.UP_EAST -> {
                connectorCuboid = connectorCuboid.offset(3.0, 3.0, 0.0)
            }

            ConduitOffset.UP_WEST -> {
                connectorCuboid = connectorCuboid.offset(-3.0, 3.0, 0.0)
            }

            ConduitOffset.DOWN_NORTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, -3.0, 3.0)
            }

            ConduitOffset.DOWN_SOUTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, -3.0, -3.0)
            }

            ConduitOffset.DOWN_EAST -> {
                connectorCuboid = connectorCuboid.offset(3.0, -3.0, 0.0)
            }

            ConduitOffset.DOWN_WEST -> {
                connectorCuboid = connectorCuboid.offset(-3.0, -3.0, 0.0)
            }

            ConduitOffset.NORTH_EAST -> {
                connectorCuboid = connectorCuboid.offset(3.0, 0.0, 3.0)
            }

            ConduitOffset.NORTH_WEST -> {
                connectorCuboid = connectorCuboid.offset(-3.0, 0.0, 3.0)
            }

            ConduitOffset.SOUTH_EAST -> {
                connectorCuboid = connectorCuboid.offset(3.0, 0.0, -3.0)
            }

            ConduitOffset.SOUTH_WEST -> {
                connectorCuboid = connectorCuboid.offset(-3.0, 0.0, -3.0)
            }
        }

        // Normalize and return
        return Box(
            connectorCuboid.minX / 16.0, connectorCuboid.minY / 16.0, connectorCuboid.minZ / 16.0,
            connectorCuboid.maxX / 16.0, connectorCuboid.maxY / 16.0, connectorCuboid.maxZ / 16.0
        )
    }
}
