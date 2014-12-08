package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4


public class ClockTickCallback implements TickCallback {

    private final Clock clock;
    private long lastTick;
    private final long length;

    public ClockTickCallback(Clock clock, long intervalStart, long tickLength) {
        this.clock = clock;
        lastTick = intervalStart;
        length = tickLength;
    }

    public void onTick() {
        long now = System.currentTimeMillis();
        long passed = now - lastTick;
        long ticks = passed / length;
        lastTick += ticks * length;
        clock.tick((int) ticks);
    }
}