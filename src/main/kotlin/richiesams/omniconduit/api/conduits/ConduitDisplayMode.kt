package richiesams.omniconduit.api.conduits

import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f
import richiesams.omniconduit.OmniConduitModBase
import richiesams.omniconduit.util.SpriteReference


class ConduitDisplayMode(val type: String, val icon: SpriteReference) {
    constructor(conduit: Conduit) :
            this(conduit.type, conduit.icon) {
    }

    companion object {
        private val values = LinkedHashSet<ConduitDisplayMode>()

        val NEUTRAL = ConduitDisplayMode(
            "neutral",
            SpriteReference(Identifier(OmniConduitModBase.MOD_ID, "gui/widgets"), Vec2f(0.0f, 0.0f), Vec2f(1.0f, 1.0f)),
        )
        val ALL = ConduitDisplayMode(
            "all",
            SpriteReference(Identifier(OmniConduitModBase.MOD_ID, "gui/widgets"), Vec2f(0.0f, 0.0f), Vec2f(1.0f, 1.0f)),
        )
        val NONE = ConduitDisplayMode(
            "none",
            SpriteReference(Identifier(OmniConduitModBase.MOD_ID, "gui/widgets"), Vec2f(0.0f, 0.0f), Vec2f(1.0f, 1.0f)),
        )

        init {
            registerConduitDisplayMode(NEUTRAL)
            registerConduitDisplayMode(ALL)
            registerConduitDisplayMode(NONE)
        }

        fun registerConduitDisplayMode(mode: ConduitDisplayMode) {
            values.add(mode)
        }

        fun values(): Iterator<ConduitDisplayMode> {
            return values.iterator()
        }
    }
}
