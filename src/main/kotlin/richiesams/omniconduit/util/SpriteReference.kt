package richiesams.omniconduit.util

import com.google.common.reflect.TypeToken
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f
import java.lang.reflect.Type

@JvmRecord
data class SpriteReference(
    val identifier: Identifier,
    val uvFrom: Vec2f,
    val uvTo: Vec2f
) {
    companion object {
        private val uvListType: Type = object : TypeToken<List<Float>>() {}.type

        fun fromJSON(jsonObject: JsonObject): SpriteReference {
            val textureElement = jsonObject["texture"] ?: throw JsonSyntaxException("Missing \"texture\" element in SpriteReference")

            val textureStr = textureElement.asString
            val textureIdentifier = Identifier.tryParse(textureStr) ?: throw JsonSyntaxException("Invalid texture identifier $textureStr")
            val UVs: List<Float> = SerializationUtil.GSON.fromJson(jsonObject["uv"], uvListType)
            if (UVs.size != 4) {
                throw JsonSyntaxException("Invalid uv section - Requires 4 values, given ${UVs.size}")
            }

            return SpriteReference(
                textureIdentifier,  // Normalize the coordinates
                Vec2f(UVs[0] / 16.0f, UVs[1] / 16.0f), Vec2f(UVs[2] / 16.0f, UVs[3] / 16.0f)
            )
        }
    }
}
