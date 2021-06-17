package pro.panopticon.client.util

import java.time.LocalDateTime

class NowSupplierImpl : NowSupplier {
    override fun now(): LocalDateTime {
        return LocalDateTime.now()
    }
}
