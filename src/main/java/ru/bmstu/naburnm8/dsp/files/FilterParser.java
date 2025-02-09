package ru.bmstu.naburnm8.dsp.files;

import ru.bmstu.naburnm8.dsp.filtering.Filter;

import java.util.ArrayList;
import java.util.List;

public class FilterParser {

    public static List<Filter> parseFilters(){ // not implemented properly yet
        ArrayList<Filter> filters = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            filters.add(new Filter(new double[]{1,0}, new double[]{1,1}, 65536));
        }
        return filters;
    }
}
