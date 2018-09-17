package com.wezom.kiviremote.persistence;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.wezom.kiviremote.persistence.dao.RecentDevicesDao;
import com.wezom.kiviremote.persistence.dao.ServerAppsDao;
import com.wezom.kiviremote.persistence.model.RecentDevice;
import com.wezom.kiviremote.persistence.model.ServerApp;

@Database(entities = {ServerApp.class, RecentDevice.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ServerAppsDao serverAppDao();

    public abstract RecentDevicesDao recentDeviceDao();
}
