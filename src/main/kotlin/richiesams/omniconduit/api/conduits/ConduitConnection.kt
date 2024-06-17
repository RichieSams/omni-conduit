package richiesams.omniconduit.api.conduits

data class ConduitConnection(
    val type: ConduitConnectionType,
    val input: Boolean,
    val output: Boolean
)

enum class ConduitConnectionType {
    CONDUIT,
    CONDUIT_SINGLE,
    TERMINATION,
}
