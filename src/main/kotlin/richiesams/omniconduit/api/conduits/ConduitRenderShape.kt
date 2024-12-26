package richiesams.omniconduit.api.conduits

import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import richiesams.omniconduit.util.SpriteReference

class ConduitRenderShape(
    val cores: List<CoreShape>,
    val connections: List<ConnectionShape>,
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