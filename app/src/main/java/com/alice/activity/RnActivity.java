package com.alice.activity;

import com.facebook.react.ReactActivity;

public class RnActivity extends ReactActivity {

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "alice";
    }
}
