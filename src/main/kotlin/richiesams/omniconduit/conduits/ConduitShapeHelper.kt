package richiesams.omniconduit.conduits


import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import richiesams.omniconduit.api.conduits.ConduitOffset

object ConduitShapeHelper {
    private const val CORE_WIDTH = 3.0 / 16.0
    private const val CORE_HALF_WIDTH = CORE_WIDTH / 2.0

    private const val CONNECTOR_WIDTH = CORE_WIDTH * 0.7
    private const val CONNECTOR_HALF_WIDTH = CONNECTOR_WIDTH / 2.0

    // We use large replacement values for zero and one
    // So we can clamp them to "real" zero / one after offset
    private const val CONNECTOR_ZERO = -50.0
    private const val CONNECTOR_ONE = 50.0

    fun coreFromOffset(offset: ConduitOffset): Box {
        var cuboid = Box(
            0.5 - CORE_HALF_WIDTH, 0.5 - CORE_HALF_WIDTH, 0.5 - CORE_HALF_WIDTH,
            0.5 + CORE_HALF_WIDTH, 0.5 + CORE_HALF_WIDTH, 0.5 + CORE_HALF_WIDTH,
        )

        when (offset) {
            ConduitOffset.NONE -> {
                // Nothing to do
            }

            ConduitOffset.UP -> {
                cuboid = cuboid.offset(0.0, CORE_WIDTH, 0.0)
            }

            ConduitOffset.DOWN -> {
                cuboid = cuboid.offset(0.0, -CORE_WIDTH, 0.0)
            }

            ConduitOffset.NORTH -> {
                cuboid = cuboid.offset(0.0, 0.0, -CORE_WIDTH)
            }

            ConduitOffset.SOUTH -> {
                cuboid = cuboid.offset(0.0, 0.0, CORE_WIDTH)
            }

            ConduitOffset.EAST -> {
                cuboid = cuboid.offset(CORE_WIDTH, 0.0, 0.0)
            }

            ConduitOffset.WEST -> {
                cuboid = cuboid.offset(-CORE_WIDTH, 0.0, 0.0)
            }

            ConduitOffset.UP_NORTH -> {
                cuboid = cuboid.offset(0.0, CORE_WIDTH, -CORE_WIDTH)
            }

            ConduitOffset.UP_SOUTH -> {
                cuboid = cuboid.offset(0.0, CORE_WIDTH, CORE_WIDTH)
            }

            ConduitOffset.UP_EAST -> {
                cuboid = cuboid.offset(CORE_WIDTH, CORE_WIDTH, 0.0)
            }

            ConduitOffset.UP_WEST -> {
                cuboid = cuboid.offset(-CORE_WIDTH, CORE_WIDTH, 0.0)
            }

            ConduitOffset.DOWN_NORTH -> {
                cuboid = cuboid.offset(0.0, -CORE_WIDTH, -CORE_WIDTH)
            }

            ConduitOffset.DOWN_SOUTH -> {
                cuboid = cuboid.offset(0.0, -CORE_WIDTH, CORE_WIDTH)
            }

            ConduitOffset.DOWN_EAST -> {
                cuboid = cuboid.offset(CORE_WIDTH, -CORE_WIDTH, 0.0)
            }

            ConduitOffset.DOWN_WEST -> {
                cuboid = cuboid.offset(-CORE_WIDTH, -CORE_WIDTH, 0.0)
            }

            ConduitOffset.NORTH_EAST -> {
                cuboid = cuboid.offset(CORE_WIDTH, 0.0, -CORE_WIDTH)
            }

            ConduitOffset.NORTH_WEST -> {
                cuboid = cuboid.offset(-CORE_WIDTH, 0.0, -CORE_WIDTH)
            }

            ConduitOffset.SOUTH_EAST -> {
                cuboid = cuboid.offset(CORE_WIDTH, 0.0, CORE_WIDTH)
            }

            ConduitOffset.SOUTH_WEST -> {
                cuboid = cuboid.offset(-CORE_WIDTH, 0.0, CORE_WIDTH)
            }
        }

        return cuboid
    }

    fun connectorFromOffset(offset: ConduitOffset, connectionDirection: Direction): Box {
        var connectorCuboid = when (connectionDirection) {
            Direction.DOWN -> {
                Box(
                    0.5 - CONNECTOR_HALF_WIDTH, CONNECTOR_ZERO, 0.5 - CONNECTOR_HALF_WIDTH,
                    0.5 + CONNECTOR_HALF_WIDTH, 0.5 - CORE_HALF_WIDTH, 0.5 + CONNECTOR_HALF_WIDTH
                )
            }

            Direction.UP -> {
                Box(
                    0.5 - CONNECTOR_HALF_WIDTH, 0.5 + CORE_HALF_WIDTH, 0.5 - CONNECTOR_HALF_WIDTH,
                    0.5 + CONNECTOR_HALF_WIDTH, CONNECTOR_ONE, 0.5 + CONNECTOR_HALF_WIDTH
                )
            }

            Direction.NORTH -> {
                Box(
                    0.5 - CONNECTOR_HALF_WIDTH, 0.5 - CONNECTOR_HALF_WIDTH, CONNECTOR_ZERO,
                    0.5 + CONNECTOR_HALF_WIDTH, 0.5 + CONNECTOR_HALF_WIDTH, 0.5 - CORE_HALF_WIDTH
                )
            }

            Direction.SOUTH -> {
                Box(
                    0.5 - CONNECTOR_HALF_WIDTH, 0.5 - CONNECTOR_HALF_WIDTH, 0.5 + CORE_HALF_WIDTH,
                    0.5 + CONNECTOR_HALF_WIDTH, 0.5 + CONNECTOR_HALF_WIDTH, CONNECTOR_ONE
                )
            }

            Direction.WEST -> {
                Box(
                    CONNECTOR_ZERO, 0.5 - CONNECTOR_HALF_WIDTH, 0.5 - CONNECTOR_HALF_WIDTH,
                    0.5 - CORE_HALF_WIDTH, 0.5 + CONNECTOR_HALF_WIDTH, 0.5 + CONNECTOR_HALF_WIDTH
                )
            }

            Direction.EAST -> {
                Box(
                    0.5 + CORE_HALF_WIDTH, 0.5 - CONNECTOR_HALF_WIDTH, 0.5 - CONNECTOR_HALF_WIDTH,
                    CONNECTOR_ONE, 0.5 + CONNECTOR_HALF_WIDTH, 0.5 + CONNECTOR_HALF_WIDTH
                )
            }
        }

        // Now offset it
        when (offset) {
            ConduitOffset.NONE -> {
                // Nothing to do
            }

            ConduitOffset.UP -> {
                connectorCuboid = connectorCuboid.offset(0.0, CORE_WIDTH, 0.0)
            }

            ConduitOffset.DOWN -> {
                connectorCuboid = connectorCuboid.offset(0.0, -CORE_WIDTH, 0.0)
            }

            ConduitOffset.NORTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, 0.0, -CORE_WIDTH)
            }

            ConduitOffset.SOUTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, 0.0, CORE_WIDTH)
            }

            ConduitOffset.EAST -> {
                connectorCuboid = connectorCuboid.offset(CORE_WIDTH, 0.0, 0.0)
            }

            ConduitOffset.WEST -> {
                connectorCuboid = connectorCuboid.offset(-CORE_WIDTH, 0.0, 0.0)
            }

            ConduitOffset.UP_NORTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, CORE_WIDTH, -CORE_WIDTH)
            }

            ConduitOffset.UP_SOUTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, CORE_WIDTH, CORE_WIDTH)
            }

            ConduitOffset.UP_EAST -> {
                connectorCuboid = connectorCuboid.offset(CORE_WIDTH, CORE_WIDTH, 0.0)
            }

            ConduitOffset.UP_WEST -> {
                connectorCuboid = connectorCuboid.offset(-CORE_WIDTH, CORE_WIDTH, 0.0)
            }

            ConduitOffset.DOWN_NORTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, -CORE_WIDTH, -CORE_WIDTH)
            }

            ConduitOffset.DOWN_SOUTH -> {
                connectorCuboid = connectorCuboid.offset(0.0, -CORE_WIDTH, CORE_WIDTH)
            }

            ConduitOffset.DOWN_EAST -> {
                connectorCuboid = connectorCuboid.offset(CORE_WIDTH, -CORE_WIDTH, 0.0)
            }

            ConduitOffset.DOWN_WEST -> {
                connectorCuboid = connectorCuboid.offset(-CORE_WIDTH, -CORE_WIDTH, 0.0)
            }

            ConduitOffset.NORTH_EAST -> {
                connectorCuboid = connectorCuboid.offset(CORE_WIDTH, 0.0, -CORE_WIDTH)
            }

            ConduitOffset.NORTH_WEST -> {
                connectorCuboid = connectorCuboid.offset(-CORE_WIDTH, 0.0, -CORE_WIDTH)
            }

            ConduitOffset.SOUTH_EAST -> {
                connectorCuboid = connectorCuboid.offset(CORE_WIDTH, 0.0, CORE_WIDTH)
            }

            ConduitOffset.SOUTH_WEST -> {
                connectorCuboid = connectorCuboid.offset(-CORE_WIDTH, 0.0, CORE_WIDTH)
            }
        }

        // Clamp it to [0.0, 1.0]
        return Box(
            connectorCuboid.minX.coerceAtLeast(0.0), connectorCuboid.minY.coerceAtLeast(0.0), connectorCuboid.minZ.coerceAtLeast(0.0),
            connectorCuboid.maxX.coerceAtMost(1.0), connectorCuboid.maxY.coerceAtMost(1.0), connectorCuboid.maxZ.coerceAtMost(1.0)
        )
    }
}
