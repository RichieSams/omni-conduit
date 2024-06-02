package richiesams.omniconduit.events

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object ModEvents {
    fun registerModEvents() {
        PayloadTypeRegistry.playC2S().register(YetaWrenchModeEvent.ID, YetaWrenchModeEvent.PACKET_CODEC)
        ServerPlayNetworking.registerGlobalReceiver(YetaWrenchModeEvent.ID, YetaWrenchModeEventHandler())
    }
}