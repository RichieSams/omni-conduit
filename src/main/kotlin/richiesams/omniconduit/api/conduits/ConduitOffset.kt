package richiesams.omniconduit.api.conduits

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import net.minecraft.util.StringIdentifiable
import java.lang.reflect.Type


@JsonAdapter(ConduitOffset.Serializer::class)
enum class ConduitOffset(val descriptor: String) : StringIdentifiable {
    NONE("none"),
    UP("up"),
    DOWN("down"),
    NORTH("north"),
    SOUTH("south"),
    EAST("east"),
    WEST("west"),
    UP_NORTH("up_north"),
    UP_SOUTH("up_south"),
    UP_EAST("up_east"),
    UP_WEST("up_west"),
    DOWN_NORTH("down_north"),
    DOWN_SOUTH("down_south"),
    DOWN_EAST("down_east"),
    DOWN_WEST("down_west"),
    NORTH_EAST("north_east"),
    NORTH_WEST("north_west"),
    SOUTH_EAST("south_east"),
    SOUTH_WEST("south_west");

    override fun asString(): String {
        return this.descriptor
    }

    internal class Serializer : JsonSerializer<ConduitOffset>, JsonDeserializer<ConduitOffset> {
        override fun serialize(src: ConduitOffset, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src.asString())
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ConduitOffset {
            return getConduitOffsetByString(json.asString)
        }
    }

    companion object {
        fun getConduitOffsetByString(str: String): ConduitOffset {
            for (offset in entries) {
                if (offset.descriptor == str) {
                    return offset
                }
            }

            return NONE
        }
    }
}