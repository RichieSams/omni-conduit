package richiesams.omniconduit.events

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import richiesams.omniconduit.OmniConduitModBase

data class ConduitAddEvent(val pos: BlockPos, val hand: Boolean) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }

    companion object {
        val ID = CustomPayload.Id<ConduitAddEvent>(Identifier(OmniConduitModBase.MOD_ID, "conduit_add_packet"))
        val PACKET_CODEC: PacketCodec<ByteBuf, ConduitAddEvent> = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ConduitAddEvent::pos,
            PacketCodecs.BOOL, ConduitAddEvent::hand,
            ::ConduitAddEvent
        )

//        fun sendPacketToServer(pos: BlockPos, hand: Hand) {
//            ClientPlayNetworking.send(ConduitAddEvent(pos, hand == Hand.MAIN_HAND))
//        }
    }
}
