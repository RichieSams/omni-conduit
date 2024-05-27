package richiesams.omniconduit.api.conduits

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import richiesams.omniconduit.OnmiConduitRegistries
import richiesams.omniconduit.blockentities.ConduitBundleBlockEntity
import richiesams.omniconduit.util.SerializationUtil
import richiesams.omniconduit.util.SpriteReference


open class Conduit(jsonObject: JsonObject, factory: Conduit.Factory<out ConduitEntity>) {
    val EastWestOffset: ConduitOffset = SerializationUtil.GSON.fromJson(jsonObject["eastWestOffset"], ConduitOffset::class.java)
    val UpDownOffset: ConduitOffset = SerializationUtil.GSON.fromJson(jsonObject["upDownOffset"], ConduitOffset::class.java)
    val NorthSouthOffset: ConduitOffset = SerializationUtil.GSON.fromJson(jsonObject["northSouthOffset"], ConduitOffset::class.java)

    val CoreSprite: SpriteReference
    val ConnectorOuterSprite: SpriteReference
    var ConnectorInnerSprite: SpriteReference? = null


    private val factory: Factory<out ConduitEntity> = factory

//    private val factory: Factory<out ConduitEntity?>

    init {
        val core = jsonObject.getAsJsonObject("core") ?: throw JsonSyntaxException("Missing \"core\" section in conduit definition")
        this.CoreSprite = SpriteReference.fromJSON(core)

        val connector = jsonObject.getAsJsonObject("connector") ?: throw JsonSyntaxException("Missing \"connector\" section in conduit definition")
        val connectorOuter = connector.getAsJsonObject("outer") ?: throw JsonSyntaxException("Missing \"connector::outer\" section in conduit definition")
        this.ConnectorOuterSprite = SpriteReference.fromJSON(connectorOuter)
        val connectorInner = connector.getAsJsonObject("inner")
        if (connectorInner == null) {
            // The inner sprite is optional
            this.ConnectorInnerSprite = null
        } else {
            this.ConnectorInnerSprite = SpriteReference.fromJSON(connectorInner)
        }
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
