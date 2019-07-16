package com.wezom.kiviremote.persistence.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.wezom.kiviremote.persistence.model.ServerApp;
import com.wezom.kiviremote.persistence.model.ServerInput;

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
