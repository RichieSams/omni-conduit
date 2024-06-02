package richiesams.omniconduit.items

import net.minecraft.component.DataComponentType
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.dynamic.Codecs
import richiesams.omniconduit.OmniConduitModBase

object ModDataComponentTypes {
    val YETA_WRENCH_MODE = DataComponentType.builder<String>().codec(Codecs.NON_EMPTY_STRING).packetCodec(PacketCodecs.STRING).build()

    fun registerDataComponentTypes() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, "${OmniConduitModBase.MOD_ID}:yeta_wrench_mode", YETA_WRENCH_MODE)
    }
}