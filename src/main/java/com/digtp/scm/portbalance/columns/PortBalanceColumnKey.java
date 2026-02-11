package com.digtp.scm.portbalance.columns;

import com.digtp.scm.entity.ShippingCombination;
import com.digtp.scm.entity.Track;

public record PortBalanceColumnKey(ComboKey comboKey,
                                   TrackKey trackKey,
                                   PortBalanceMetric metric) {

    public static PortBalanceColumnKey of(ComboKey comboKey, TrackKey trackKey, PortBalanceMetric metric) {
        return new PortBalanceColumnKey(comboKey, trackKey, metric);
    }

    public static PortBalanceColumnKey from(ShippingCombination combination,
                                            Track track,
                                            PortBalanceMetric metric) {
        return new PortBalanceColumnKey(ComboKey.from(combination), TrackKey.from(track), metric);
    }
}
