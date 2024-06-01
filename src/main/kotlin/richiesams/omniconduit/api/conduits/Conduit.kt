package richiesams.omniconduit.api.conduits

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import richiesams.omniconduit.api.OnmiConduitRegistries
import richiesams.omniconduit.blockentities.ConduitBundleBlockEntity
import richiesams.omniconduit.util.SerializationUtil
import richiesams.omniconduit.util.SpriteReference


open class Conduit(val type: String, jsonObject: JsonObject, private val factory: Factory<out ConduitEntity>) {
    val eastWestOffset: ConduitOffset = SerializationUtil.GSON.fromJson(jsonObject["eastWestOffset"], ConduitOffset::class.java)
    val upDownOffset: ConduitOffset = SerializationUtil.GSON.fromJson(jsonObject["upDownOffset"], ConduitOffset::class.java)
    val northSouthOffset: ConduitOffset = SerializationUtil.GSON.fromJson(jsonObject["northSouthOffset"], ConduitOffset::class.java)

    val coreSprite: SpriteReference
    val connectorOuterSprite: SpriteReference
    val connectorInnerSprite: SpriteReference?

    val icon: SpriteReference

    init {
        val core = jsonObject.getAsJsonObject("core") ?: throw JsonSyntaxException("Missing \"core\" section in conduit definition")
        this.coreSprite = SpriteReference.fromJSON(core)

        val connector = jsonObject.getAsJsonObject("connector") ?: throw JsonSyntaxException("Missing \"connector\" section in conduit definition")
        val connectorOuter = connector.getAsJsonObject("outer") ?: throw JsonSyntaxException("Missing \"connector::outer\" section in conduit definition")
        this.connectorOuterSprite = SpriteReference.fromJSON(connectorOuter)
        val connectorInner = connector.getAsJsonObject("inner")
        if (connectorInner == null) {
            // The inner sprite is optional
            this.connectorInnerSprite = null
        } else {
            this.connectorInnerSprite = SpriteReference.fromJSON(connectorInner)
        }

        val icon = jsonObject.getAsJsonObject("icon") ?: throw JsonSyntaxException("Missing \"icon\" section in conduit definition")
        this.icon = SpriteReference.fromJSON(icon)

    }


    fun toItemStack(): ItemStack {
        val identifier: Identifier = OnmiConduitRegistries.CONDUIT.getId(this) ?: throw RuntimeException("No conduit registered for ConduitEntity")

        val item: Item = Registries.ITEM.get(identifier)
        if (item === Items.AIR) {
            throw RuntimeException("Failed to get Conduit item for $identifier")
        }

        return item.defaultStack
    }

    fun createConduitEntity(blockEntity: ConduitBundleBlockEntity): ConduitEntity {
        return factory.create(this, blockEntity)
    }

    fun interface Factory<T : ConduitEntity> {
        fun create(conduit: Conduit, blockEntity: ConduitBundleBlockEntity): T
    }
}
