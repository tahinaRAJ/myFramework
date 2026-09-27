package dev.framework.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewUtil {
    private String view;
    private Map<String, List<?>> values;

    public ViewUtil() {
        this.values = new HashMap<>();
    }

    public ViewUtil(String view, Map<String, List<?>> values) {
        this.view = view;
        this.values = (values != null) ? values : new HashMap<>();
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, List<?>> getValues() {
        return values;
    }

    public void setValues(Map<String, List<?>> values) {
        this.values = values;
    }

    public void addValue(String key, List<?> value) {
        this.values.put(key, value);
    }

    public void addValue(String key, String value) {
        this.values.put(key, List.of(value));
    }
}
