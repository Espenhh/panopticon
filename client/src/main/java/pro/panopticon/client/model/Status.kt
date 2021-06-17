package pro.panopticon.client.model

data class Status(
    val environment: String,
    val system: String,
    val component: String,
    val server: String,
    val measurements: List<Measurement>,
) {
    constructor(
        componentInfo: ComponentInfo,
        measurements: List<Measurement>,
    ) : this(
        componentInfo.environment,
        componentInfo.system,
        componentInfo.component,
        componentInfo.server,
        measurements,
    )
}
