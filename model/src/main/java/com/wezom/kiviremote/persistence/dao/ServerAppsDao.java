package com.wezom.kiviremote.persistence.dao;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.wezom.kiviremote.persistence.model.ServerApp;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface ServerAppsDao {

    @Query("SELECT * FROM apps")
    Flowable<List<ServerApp>> getAll();

    @Insert
    void insertAll(List<ServerApp> apps);

    @Query("SELECT * FROM apps WHERE package_name = :name")
    Flowable<ServerApp> getApp(String name); //- replaced by preference that hold name

    @Update
    int update(ServerApp app);

    @Insert
    void insertMediaShareStaticApp(ServerApp app);


    @Query("DELETE FROM apps")
    void removeAll();
}
