package richiesams.omniconduit.api.conduits

data class ConduitConnection(
    val terminated: Boolean,
    val input: Boolean,
    val output: Boolean
)
