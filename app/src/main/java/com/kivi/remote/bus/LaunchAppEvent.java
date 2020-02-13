package com.kivi.remote.bus;


public class LaunchAppEvent {
    String packageName;

    public LaunchAppEvent(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public LaunchAppEvent setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }
}
