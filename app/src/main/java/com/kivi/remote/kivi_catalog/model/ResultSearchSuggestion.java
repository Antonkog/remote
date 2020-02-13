package com.kivi.remote.kivi_catalog.model;

import java.util.List;

public class ResultSearchSuggestion {

    public SearchSuggestion[] result;

    public class SearchSuggestion {
        public int id;
        public String title;
        public List<Integer> genres;
        public List<Poster> posters;
        public String description;
        public int year;
        public List<Integer> years;
        public List<IviSeason> seasons;

        String object_type;

        public boolean isVideo(){
            return "compilation".equals(object_type) || "video".equals(object_type);
        }

        public boolean isSeries() {
            return object_type.equals("compilation");
        }

        public class Poster {
            public String url;
        }
    }
}