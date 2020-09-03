package pro.panopticon.client.util;

import java.time.LocalDateTime;

public interface NowSupplier {
    LocalDateTime now();
}
