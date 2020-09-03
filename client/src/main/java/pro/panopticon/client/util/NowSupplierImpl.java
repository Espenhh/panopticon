package pro.panopticon.client.util;

import java.time.LocalDateTime;

public class NowSupplierImpl implements NowSupplier {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

}
