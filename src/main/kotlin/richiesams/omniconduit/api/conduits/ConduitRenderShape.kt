package richiesams.omniconduit.api.conduits

import net.minecraft.util.DyeColor
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import richiesams.omniconduit.util.SpriteReference

class ConduitRenderShape(
    val cores: List<CoreShape>,
    val connections: List<ConnectionShape>,
    val terminations: List<TerminationShape>
)

class CoreShape(
    val type: String,
    val sprite: SpriteReference,
    val box: Box,
)

class ConnectionShape(
    val type: String,
    val outerSprite: SpriteReference,
    val innerSprite: SpriteReference?,
    val box: Box,
    val direction: Direction
)

class TerminationShape(
    val type: String,
    val outerBox: Box,
    val innerBox: Box,
    val ioConnectorBox: Box,
    val terminationMode: ConduitTerminationMode,
    val inputChannel: DyeColor,
    val outputChannel: DyeColor,
    val direction: Direction
)