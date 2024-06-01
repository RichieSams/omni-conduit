package richiesams.omniconduit.events

import net.minecraft.client.MinecraftClient

object ClientHandler {
    private var ticksElapsed: Long = 0

    fun onClientTick(client: MinecraftClient) {
        ticksElapsed++
    }

    fun getTicksElapsed(): Long {
        return ticksElapsed
    }
}