package com.kivi.remote.persistence.model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recommendations")
public class ServerRecommendation {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "contentID")
    private String contentID;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "subTitle")
    private String subTitle;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "imageUrl")
    private String imageUrl;

    @ColumnInfo(name = "uri")
    private String uri;

    @ColumnInfo(name = "kind")
    private String kind;

    @ColumnInfo(name = "imdb")
    private String imdb;

    @ColumnInfo(name = "monetizationType")
    private String monetizationType;

    @ColumnInfo(name = "isFavourite")
    private Boolean isFavourite;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getMonetizationType() {
        return monetizationType;
    }

    public void setMonetizationType(String monetizationType) {
        this.monetizationType = monetizationType;
    }

    public Boolean getFavourite() {
        return isFavourite;
    }

    public void setFavourite(Boolean favourite) {
        isFavourite = favourite;
    }

    public String getContentID() {
        return contentID;
    }

    public void setContentID(String contentID) {
        this.contentID = contentID;
    }
}
