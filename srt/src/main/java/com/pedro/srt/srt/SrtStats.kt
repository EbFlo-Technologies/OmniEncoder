package com.pedro.srt.srt

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

data class SrtStats(
    // The "State" - setting this to false disables ARQ/NAK resends
    var isRetransmissionEnabled: AtomicBoolean = AtomicBoolean(true),

    // The "Counters"
    val retransmittedPackets: AtomicInteger = AtomicInteger(0),
    val nakPacketsReceived: AtomicInteger = AtomicInteger(0)
)