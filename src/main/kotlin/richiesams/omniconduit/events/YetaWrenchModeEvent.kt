package richiesams.omniconduit.events

import io.netty.buffer.ByteBuf
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import richiesams.omniconduit.OmniConduitModBase

data class YetaWrenchModeEvent(val mode: String) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }

    companion object {
        val ID = CustomPayload.Id<YetaWrenchModeEvent>(Identifier(OmniConduitModBase.MOD_ID, "yeta_wrench_mode_packet"))
        val PACKET_CODEC: PacketCodec<ByteBuf, YetaWrenchModeEvent> = PacketCodec.tuple(
            PacketCodecs.STRING, YetaWrenchModeEvent::mode,
            ::YetaWrenchModeEvent
        )

        fun sendPacketToServer(mode: String) {
            ClientPlayNetworking.send(YetaWrenchModeEvent(mode))
        }
    }
}