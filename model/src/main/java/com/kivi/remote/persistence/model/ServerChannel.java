package com.kivi.remote.persistence.model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "channels")
public class ServerChannel  {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "ServerId")
    private String ServerId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "is_active")
    private Boolean is_active;

    @ColumnInfo(name = "imageUrl")
    private String imageUrl;

    @ColumnInfo(name = "created_at")
    private String created_at;

    @ColumnInfo(name = "sort")
    private String sort;

    @ColumnInfo(name = "edited_at")
    private String edited_at;

    @ColumnInfo(name = "has_timeshift")
    private String has_timeshift;

    @ColumnInfo(name = "adult_content")
    private String adult_content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIs_active() {
        return is_active;
    }

    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getEdited_at() {
        return edited_at;
    }

    public void setEdited_at(String edited_at) {
        this.edited_at = edited_at;
    }

    public String getHas_timeshift() {
        return has_timeshift;
    }

    public void setHas_timeshift(String has_timeshift) {
        this.has_timeshift = has_timeshift;
    }

    public String getAdult_content() {
        return adult_content;
    }

    public void setAdult_content(String adult_content) {
        this.adult_content = adult_content;
    }

    public String getServerId() {
        return ServerId;
    }

    public void setServerId(String serverId) {
        ServerId = serverId;
    }
}
