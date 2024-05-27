package richiesams.omniconduit.api.conduits

import com.google.gson.JsonObject
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.minecraft.util.Identifier
import org.apache.commons.io.IOUtils
import richiesams.omniconduit.util.SerializationUtil
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets


object ConduitLoader {
    fun load(identifier: Identifier, conduitFactory: Factory<out Conduit>, conduitEntityFactory: Conduit.Factory<out ConduitEntity>): Conduit {
        val location = "assets/${identifier.namespace}/conduits/${identifier.path}.json"

        val jsonObject: JsonObject
        try {
            val inputStream: InputStream = FabricLauncherBase.getLauncher().getResourceAsStream(location) ?: throw RuntimeException("Failed to find resource file $location")

            jsonObject = SerializationUtil.GSON.fromJson(IOUtils.toString(inputStream, StandardCharsets.UTF_8), JsonObject::class.java)
        } catch (e: IOException) {
            throw RuntimeException("Failed to read conduit resource file $location", e)
        }

        return conduitFactory.create(jsonObject, conduitEntityFactory)
    }

    fun interface Factory<T : Conduit?> {
        fun create(jsonObject: JsonObject, conduitEntityFactory: Conduit.Factory<out ConduitEntity>): T
    }
}