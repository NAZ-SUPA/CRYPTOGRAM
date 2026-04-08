package com.kurdish.cryptogram;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Device-side instrumentation test scaffold:
 * - Executes with AndroidJUnit4 on an emulator/device.
 * - Uses InstrumentationRegistry to access app runtime context.
 * - Confirms installed package identity to validate launch/test plumbing.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Fetches context from the instrumented process hosting the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Package assertion guards against manifest/applicationId mismatch regressions.
        assertEquals("com.kurdish.cryptogram", appContext.getPackageName());
    }
}