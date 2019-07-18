package com.wezom.kiviremote.persistence.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.wezom.kiviremote.persistence.model.ServerApp;
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
