package com.kivi.remote.persistence.dao;


import com.kivi.remote.persistence.model.ServerApp;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Flowable;

import static androidx.room.OnConflictStrategy.IGNORE;

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

    @Insert(onConflict = IGNORE)
    long insert(ServerApp app);

    @Insert
    void insertMediaShareStaticApp(ServerApp app);


    @Query("DELETE FROM apps")
    void removeAll();
}
