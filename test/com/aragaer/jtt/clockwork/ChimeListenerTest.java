package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.*;

import android.content.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk=18)
public class ChimeListenerTest {

    private TestListener listener;

    @Before
    public void setUp() {
        listener = new TestListener();
    }

    @Test
    public void shouldRegisterForTickEvent() {
        listener.register(Robolectric.application);

        assertThat("is registered",
            Robolectric
                .getShadowApplication()
                .getReceiversForIntent(
                    new Intent(Chime.ACTION_JTT_TICK)),
            hasItem((BroadcastReceiver) listener));
    }

    @Test
    public void shouldCallOnChime() {
        listener.onReceive(null, new Intent().putExtra(Chime.EXTRA_JTT, 123));

        assertThat("chime event count", listener.calls, equalTo(1));
        assertThat("chime tick number", listener.tick, equalTo(123));
    }

    static class TestListener extends ChimeListener {
        int tick;
        int calls;
        public void onChime(int tick) {
            calls++;
            this.tick = tick;
        }
    }
}
