package com.wezom.kiviremote.persistence.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.wezom.kiviremote.persistence.model.ServerApp;
import com.wezom.kiviremote.persistence.model.ServerRecommendation;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface RecommendationsDao {

    @Query("SELECT * FROM recommendations")
    Flowable<List<ServerRecommendation>> getAll();

    @Insert
    void insertAll(List<ServerRecommendation> recommendations);

    @Query("DELETE FROM recommendations")
    void removeAll();
}
