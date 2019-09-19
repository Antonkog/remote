package com.wezom.kiviremote.persistence.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.wezom.kiviremote.persistence.model.RecentDevice;

import java.util.List;

import io.reactivex.Flowable;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface RecentDevicesDao {

    @Query("SELECT * FROM recent_devices")
    Flowable<List<RecentDevice>> getAll();

    @Query("SELECT * FROM recent_devices ORDER BY wasConnected DESC LIMIT 5")
    Flowable<List<RecentDevice>> getFiveByConnection();


    @Query("SELECT * FROM recent_devices WHERE actual_name = :name")
    Flowable<RecentDevice> getDevice(String name);

    @Insert
    void insertAll(List<RecentDevice> devices);

    @Update
    void update(RecentDevice device);

    @Insert(onConflict = REPLACE)
    void insertReplace(RecentDevice device);

    @Insert(onConflict = IGNORE)
    void insert(List<RecentDevice>devices);

    @Query("DELETE FROM recent_devices")
    void removeAll();

    @Query("DELETE FROM recent_devices WHERE actual_name = :name")
    void removeByName(String name);

    @Delete
    void deleteDevices(List<RecentDevice> devices);

    @Delete
    void deleteDevices(RecentDevice... devices);
}
