package com.kivi.remote.persistence.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.kivi.remote.persistence.model.ServerChannel;

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
