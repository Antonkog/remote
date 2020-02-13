package com.kivi.remote.kivi_catalog.model;

public class FilterModel {

    public int id;
    public String title;
    public int startYear;
    public int endYear;
    public String sortType;

    public FilterModel(String title, int startYear, int endYear) {
        this.title = title;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    public FilterModel(String title, int id) {
        this.title = title;
        this.id = id;
    }

    public FilterModel(String title, String sortType) {
        this.title = title;
        this.sortType = sortType;
    }

    public FilterModel clone() {
        return new FilterModel(title, id);
    }

    public static FilterModel[] getCategories() {
        return new FilterModel[] {
                new FilterModel("Фильмы", 14),
                new FilterModel("Сериалы", 15),
                new FilterModel("Мультфильмы", 17)
        };
    }

    public static FilterModel[] getGenres() {
        return new FilterModel[] {
                new FilterModel("Зарубежные", 20),
                new FilterModel("Боевики", 1),
                new FilterModel("Военные", 10),
                new FilterModel("Триллеры", 12),
                new FilterModel("Мистические", 16),
                new FilterModel("Русские", 19),
                new FilterModel("Артхаус", 21),
                new FilterModel("Комедии", 2),
                new FilterModel("Мелодрамы", 3),
                new FilterModel("Драмы", 4),
                new FilterModel("Приключения", 5),
                new FilterModel("Фантастика", 6),
                new FilterModel("Детективы", 7),
                new FilterModel("Для детей", 8),
                new FilterModel("Исторические", 9),
                new FilterModel("Ужасы", 11),
                new FilterModel("Для всей семьи", 13),
                new FilterModel("Документальные", 14),
                new FilterModel("Самое новое", 18)
        };
    }

    public static FilterModel[] getCountries() {
        return new FilterModel[] {
                new FilterModel("Россия", 1),
                new FilterModel("Беларусь", 2),
                new FilterModel("США", 4),
                new FilterModel("Великобритания", 6),
                new FilterModel("Франция", 8),
                new FilterModel("Китай", 12),
                new FilterModel("Польша", 17),
                new FilterModel("Дания", 18),
                new FilterModel("Казахстан", 22),
                new FilterModel("Южная Корея", 25),
                new FilterModel("Италия", 29),
                new FilterModel("Украина", 33),
                new FilterModel("Испания", 48),
                new FilterModel("Швеция", 49),
                new FilterModel("Индия", 56),
                new FilterModel("Югославия", 83),
                new FilterModel("Хорватия", 86),
                new FilterModel("СССР", 87)
        };
    }

    public static FilterModel[] getSortList() {
        return new FilterModel[] {
                new FilterModel("Популярности", "pop"),
                new FilterModel("Дате появления", "new"),
                new FilterModel("Рейтингу ivi", "ivi"),
                new FilterModel("Рейтингу Кинопоиска", "kp"),
                new FilterModel("Рейтингу IMDB", "imdb"),
                new FilterModel("Бюджету", "budget"),
                new FilterModel("Сборам в мире", "boxoffice"),
                new FilterModel("Году выхода", "year"),
        };
    }

    public static FilterModel[] getYears(){
        return new FilterModel[] {
                new FilterModel("Все года", 1900, 2018),
                new FilterModel("2019", 2019,2019),
                new FilterModel("2018", 2018,2018),
                new FilterModel("2018-2019", 2018,2019),
                new FilterModel("2010-2019", 2010, 2019),
                new FilterModel("2000-2010", 2000, 2010),
                new FilterModel("1990-2000", 1990, 2000),
                new FilterModel("1980-1990", 1980, 1990),
                new FilterModel("До 1980", 1900, 1980)
        };
    }




}
