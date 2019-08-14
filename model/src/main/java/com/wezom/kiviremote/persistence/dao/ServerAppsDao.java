package com.wezom.kiviremote.persistence.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.wezom.kiviremote.persistence.model.ServerApp;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface ServerAppsDao {

    @Query("SELECT * FROM apps")
    Flowable<List<ServerApp>> getAll();

    @Insert
    void insertAll(List<ServerApp> apps);


    @Insert
    void insertMediaShareStaticApp(ServerApp app);


    @Query("DELETE FROM apps")
    void removeAll();
}
