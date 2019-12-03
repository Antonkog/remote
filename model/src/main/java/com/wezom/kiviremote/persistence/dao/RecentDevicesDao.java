package com.wezom.kiviremote.persistence.dao;



import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.wezom.kiviremote.persistence.model.RecentDevice;

import java.util.List;

import io.reactivex.Flowable;

import static androidx.room.OnConflictStrategy.IGNORE;


@Dao
public interface RecentDevicesDao {

    @Query("SELECT * FROM recent_devices")
    List<RecentDevice> getAll();

    @Query("SELECT * FROM recent_devices ORDER BY wasConnected DESC LIMIT 5")
    Flowable<List<RecentDevice>> getFiveByConnection();

    @Query("DELETE FROM recent_devices WHERE actual_name = :name")
    void removeByName(String name);

    @Update
    int update(RecentDevice device);

//    @Insert(onConflict = IGNORE)
//    void insert(List<RecentDevice>devices);

    @Insert(onConflict = IGNORE)
    long insert(RecentDevice device);

    @Query("SELECT * FROM recent_devices WHERE actual_name = :name")
    Flowable<RecentDevice> getDevice(String name); //- replaced by preference that hold name

//    @Query("DELETE FROM recent_devices")
//    void removeAll();

    @Delete
    void deleteDevices(List<RecentDevice> devices);

    @Delete
    void deleteDevices(RecentDevice... devices);
}
