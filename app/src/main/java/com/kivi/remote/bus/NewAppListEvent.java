package com.kivi.remote.bus;


import com.kivi.remote.net.model.ServerAppInfo;

import java.util.List;

public class NewAppListEvent {
    private List<ServerAppInfo> appInfo;

    public List<ServerAppInfo> getAppInfo() {
        return appInfo;
    }

    public NewAppListEvent setAppInfo(List<ServerAppInfo> appInfo) {
        this.appInfo = appInfo;
        return this;
    }
}
