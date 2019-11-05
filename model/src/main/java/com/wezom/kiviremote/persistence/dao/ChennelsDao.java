package com.wezom.kiviremote.persistence.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wezom.kiviremote.persistence.model.ServerChannel;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface ChennelsDao {

    @Query("SELECT * FROM channels")
    Flowable<List<ServerChannel>> getAll();

    @Insert
    void insertAll(List<ServerChannel> channels);

    @Query("DELETE FROM channels")
    void removeAll();
}
