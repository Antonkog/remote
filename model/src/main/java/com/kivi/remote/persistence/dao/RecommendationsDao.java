package com.kivi.remote.persistence.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.kivi.remote.persistence.model.ServerRecommendation;

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
