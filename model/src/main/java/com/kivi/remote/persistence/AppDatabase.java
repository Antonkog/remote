package com.kivi.remote.persistence;



import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.kivi.remote.persistence.dao.ChennelsDao;
import com.kivi.remote.persistence.dao.RecentDevicesDao;
import com.kivi.remote.persistence.dao.RecommendationsDao;
import com.kivi.remote.persistence.dao.ServerAppsDao;
import com.kivi.remote.persistence.dao.ServerInputsDao;
import com.kivi.remote.persistence.model.RecentDevice;
import com.kivi.remote.persistence.model.ServerApp;
import com.kivi.remote.persistence.model.ServerChannel;
import com.kivi.remote.persistence.model.ServerInput;
import com.kivi.remote.persistence.model.ServerRecommendation;

@Database(entities = {ServerApp.class, RecentDevice.class, ServerInput.class, ServerChannel.class, ServerRecommendation.class}, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ServerAppsDao serverAppDao();

    public abstract ServerInputsDao serverInputsDao();

    public abstract RecentDevicesDao recentDeviceDao();

    public abstract RecommendationsDao recommendationsDao();

    public abstract ChennelsDao chennelsDao();
}
