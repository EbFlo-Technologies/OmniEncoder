package com.pedro.udp.utils

import java.util.concurrent.locks.LockSupport

/**
 * A highly accurate UDP packet pacer utilizing a Token Bucket algorithm.
 */
class UdpPacketPacer(private var targetBitrateBps: Long) {

    private var tokens: Double = 0.0
    private var lastTime = System.nanoTime()
    private var sleepCount = 0
    private var lastLogTime = System.currentTimeMillis()
    // Capacity defines how much "burst" is allowed. 
    // Set to roughly 2 MTUs to ensure smooth pacing.
    private var capacityBytes = 1500.0 * 2.0

    fun updateBitrate(newBitrateBps: Long) {
        this.targetBitrateBps = newBitrateBps
    }

    /**
     * Blocks the thread precisely until it is safe to send based on target bitrate.
     */
    fun pace(packetSizeBytes: Int) {
        if (targetBitrateBps <= 0L) return // Pacing disabled

        val targetBytesPerSec = targetBitrateBps / 8.0

        while (true) {
            val now = System.nanoTime()
            val elapsedSeconds = (now - lastTime) / 1_000_000_000.0
            lastTime = now

            // Add new tokens based on elapsed time
            tokens += elapsedSeconds * targetBytesPerSec
            if (tokens > capacityBytes) tokens = capacityBytes

            // If we have enough tokens, consume them and allow the send
            if (tokens >= packetSizeBytes) {
                tokens -= packetSizeBytes
                return
            }

            // Not enough tokens. Calculate wait time.
            val deficitBytes = packetSizeBytes - tokens
            val timeToWaitSeconds = deficitBytes / targetBytesPerSec
            val waitNanos = (timeToWaitSeconds * 1_000_000_000L).toLong()

            // Park thread for precision wait
            if (waitNanos > 10_000L) {
                //sleepCount++ // Track that we engaged the brakes
                LockSupport.parkNanos(waitNanos)
            }
            // Print a summary every 1 second so we don't flood Logcat
            /*val nowMs = System.currentTimeMillis()
            if (nowMs - lastLogTime > 1000) {
                android.util.Log.d("Pacer", "Pacer engaged $sleepCount times in the last second.")
                sleepCount = 0
                lastLogTime = nowMs
            }*/
        }
    }
}