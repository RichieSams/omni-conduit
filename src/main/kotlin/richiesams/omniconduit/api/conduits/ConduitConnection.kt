package richiesams.omniconduit.api.conduits

data class ConduitConnection(
    val type: ConduitConnectionType,
    val terminationMode: ConduitTerminationMode
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
