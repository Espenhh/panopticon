package pro.panopticon.client.util

import java.time.LocalDateTime

interface NowSupplier {
    fun now(): LocalDateTime
}
