package richiesams.omniconduit.api.conduits

import net.minecraft.util.DyeColor

data class ConduitConnection(
    val type: ConduitConnectionType,
    val terminationMode: ConduitTerminationMode,
    val terminationInputChannel: DyeColor,
    val terminationOutputChannel: DyeColor,
)

enum class ConduitConnectionType {
    SINGLE_CONDUIT,
    MULTI_CONDUIT,
    TERMINATION,
}

enum class ConduitTerminationMode {
    NONE,
    INPUT_OUTPUT,
    INPUT_ONLY,
    OUTPUT_ONLY
}
