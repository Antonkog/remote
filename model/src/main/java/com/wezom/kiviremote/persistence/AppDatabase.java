package com.wezom.kiviremote.persistence;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.wezom.kiviremote.persistence.dao.RecentDevicesDao;
import com.wezom.kiviremote.persistence.dao.ServerAppsDao;
import com.wezom.kiviremote.persistence.dao.ServerInputsDao;
import com.wezom.kiviremote.persistence.model.RecentDevice;
import com.wezom.kiviremote.persistence.model.ServerApp;
import com.wezom.kiviremote.persistence.model.ServerInput;

@Database(entities = {ServerApp.class, RecentDevice.class, ServerInput.class}, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ServerAppsDao serverAppDao();

    public abstract ServerInputsDao serverInputsDao();

    public abstract RecentDevicesDao recentDeviceDao();
}
