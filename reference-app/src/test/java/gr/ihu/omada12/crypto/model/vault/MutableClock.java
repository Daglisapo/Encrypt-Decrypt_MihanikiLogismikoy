package gr.ihu.omada12.crypto.model.vault;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Ρολόι για tests: ο χρόνος προχωρά μόνο όταν το ζητήσουμε. */
public class MutableClock extends Clock {
    private Instant now = Instant.parse("2026-10-22T10:00:00Z");

    public void advance(Duration d) { now = now.plus(d); }

    @Override public ZoneId getZone() { return ZoneOffset.UTC; }

    @Override public Clock withZone(ZoneId zone) { return this; }

    @Override public Instant instant() { return now; }
}
