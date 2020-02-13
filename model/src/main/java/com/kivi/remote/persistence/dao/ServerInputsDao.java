package com.kivi.remote.persistence.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.kivi.remote.persistence.model.ServerInput;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface ServerInputsDao {

    @Query("SELECT * FROM inputs")
    Flowable<List<ServerInput>> getAll();
//
//    @Query("SELECT DISTINCT portName from inputs")
//    Flowable<List<ServerInput>> getDistinctBeiName();


    @Insert
    void insertAll(List<ServerInput> inputs);

    @Query("DELETE FROM inputs")
    void removeAll();
}
