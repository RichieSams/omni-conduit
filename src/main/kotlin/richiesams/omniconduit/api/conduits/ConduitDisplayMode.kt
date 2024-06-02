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

        val CONFIGURE = ConduitDisplayMode(
            "configure",
            SpriteReference(Identifier(OmniConduitModBase.MOD_ID, "gui/conduit-icons"), Vec2f(0.5f, 0.25f), Vec2f(0.75f, 0.5f)),
        )
        val ALL = ConduitDisplayMode(
            "all",
            SpriteReference(Identifier(OmniConduitModBase.MOD_ID, "gui/conduit-icons"), Vec2f(0.0f, 0.25f), Vec2f(0.25f, 0.5f)),
        )
        val NONE = ConduitDisplayMode(
            "none",
            SpriteReference(Identifier(OmniConduitModBase.MOD_ID, "gui/conduit-icons"), Vec2f(0.25f, 0.25f), Vec2f(0.5f, 0.5f)),
        )

        init {
            // Register them so ALL is last, which is the "default" mode
            // Then all new conduits will be next to ALL, and previous will be the less used NONE and CONFIGURE
            registerConduitDisplayMode(CONFIGURE)
            registerConduitDisplayMode(NONE)
            registerConduitDisplayMode(ALL)
        }

        fun registerConduitDisplayMode(mode: ConduitDisplayMode) {
            values.add(mode)
        }

        fun getByType(type: String): ConduitDisplayMode {
            for (value in values) {
                if (value.type == type) {
                    return value
                }
            }

            throw RuntimeException("$type is not a registered ConduitDisplayMode")
        }

        fun next(curr: String): ConduitDisplayMode {
            val iter = values.iterator()

            while (iter.hasNext()) {
                val value = iter.next()

                if (curr == value.type) {
                    if (iter.hasNext()) {
                        return iter.next()
                    }

                    return values.first
                }
            }

            throw RuntimeException("$curr is not a registered ConduitDisplayMode")
        }

        fun previous(curr: String): ConduitDisplayMode {
            val iter = values.reversed().iterator()

            while (iter.hasNext()) {
                val value = iter.next()

                if (curr == value.type) {
                    if (iter.hasNext()) {
                        return iter.next()
                    }

                    return values.last
                }
            }

            throw RuntimeException("$curr is not a registered ConduitDisplayMode")
        }
    }
}
