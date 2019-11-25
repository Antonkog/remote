package com.wezom.kiviremote.kivi_catalog.model;

public class ResultAppVersion {

    public Error error;
    public Result result;

    public class Result {
        public int actual_app_version;
    }

    public class Error {
        public int code;
        public String country_name;
        public String message;
    }
}
