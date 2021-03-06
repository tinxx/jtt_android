// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.content.*;
import android.content.pm.*;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import org.junit.*;
import org.junit.runner.RunWith;

import com.aragaer.jtt.mechanics.AndroidTicker;

import static org.junit.Assert.*;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;


@RunWith(AndroidJUnit4.class)
public class JttServiceTest {

    private Context context;
    private MyReceiver receiver;

    @Before public void setUp() {
        context = getTargetContext();
        receiver = new MyReceiver();
        context.removeStickyBroadcast(new Intent(AndroidTicker.ACTION_JTT_TICK));
        context.registerReceiver(receiver, new IntentFilter(AndroidTicker.ACTION_JTT_TICK));
    }

    @After public void cleanUp() {
        context.stopService(new Intent(context, JttService.class));
        context.removeStickyBroadcast(new Intent(AndroidTicker.ACTION_JTT_TICK));
    }

    @Test public void testTicksOnStart() throws Exception {
        Intent intent = new Intent(context, JttService.class);
        context.startService(intent);
        synchronized (receiver) {
            receiver.wait(1000);
        }
    }

    // FIXME: This test fails on emulator
    @Ignore
    @Test public void testTicksOnScreenOn() throws Exception {
        Intent intent = new Intent(context, JttService.class);
        context.startService(intent);
        synchronized (receiver) {
            receiver.wait(1000);
        }

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.sleep();
        receiver.triggered = null;
        device.wakeUp();
        synchronized (receiver) {
            receiver.wait(1000);
        }
        assertNotNull(receiver.triggered);
    }

    private class MyReceiver extends BroadcastReceiver {
        Intent triggered;

        @Override public void onReceive(Context context, Intent intent) {
            triggered = intent;
            synchronized (this) {
                this.notify();
            }
        }
    }
}
