package com.wezom.kiviremote.kivi_catalog;

import android.os.Build;
import android.text.TextUtils;

import com.wezom.kiviremote.kivi_catalog.model.MetaGenre;
import com.wezom.kiviremote.kivi_catalog.model.ResultAppVersion;
import com.wezom.kiviremote.kivi_catalog.model.ResultMetaGenre;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class Constants {

    public static String IVI_KEY = "50e48c1a93a5cc14f6ffd30d00579ce2";
    public static String IVI_KEY1 = "dab093129afd929a";
    public static String IVI_KEY2 = "b561262535fb252f";
    public static int IVI_APP_VERSION = 17906;

    private static ResultAppVersion currentAppVersion;
    private static ResultMetaGenre metaGenre;
    private static boolean isInited;

    public static int getAppVer() {
        if(!isInited) {
            return IVI_APP_VERSION;
        } else {
            return currentAppVersion.result.actual_app_version;
        }
    }

    public static Single<Integer> updateAppVersion() {
        return new IviService().getService(null, 60 * 60 * 24 * 7, 60 * 60 * 24 * 7, 5 * 1024 * 1024)
                .getActualAppVersion(IVI_APP_VERSION, "KIVI_" + getDeviceName())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(resultAppVersion -> {
                    currentAppVersion = resultAppVersion;
                    new IviService().getService(null, 60 * 60 * 24 * 7, 60 * 60 * 24 * 7, 5 * 1024 * 1024).getMetaGenres(currentAppVersion.result.actual_app_version)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(resultMetaGenre -> metaGenre = resultMetaGenre);

                    if(resultAppVersion.error != null) {
                        throw new Exception(resultAppVersion.error.message);
                    } else if(resultAppVersion.result == null || resultAppVersion.result.actual_app_version == 0) {
                        throw new Exception("Can`t load, try again later");
                    } else {
                        isInited = true;
                        return resultAppVersion.result.actual_app_version;
                    }
                });
    }

    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }


    public static boolean isInitedAppVer() {
        return isInited;
    }

    public static String getAppVerErrorMessage(){
        return currentAppVersion != null && currentAppVersion.error != null ?
                currentAppVersion.error.message : null;
    }

    public static List<String> getGenresList(int[] genresId) {
        List<String> result = new ArrayList<>();
        if (metaGenre != null && metaGenre.result != null && metaGenre.result.length > 0) {
            for (int id : genresId) {
                for (MetaGenre genre : metaGenre.result) {
                    if(result.contains(genre.title)) {
                        continue;
                    }
                    if (genre.id == id) {
                        result.add(genre.title);
                    } else {
                        for (int id1 : genre.genre_list) {
                            if (id1 == id) {
                                result.add(genre.title);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }


}