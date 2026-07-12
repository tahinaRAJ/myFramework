package dev.framework.util;

import java.util.HashMap;
import java.util.Map;
public class ViewUtil {
    private String view;
    private Map<String, Object> values;

    public ViewUtil() {
        this.values = new HashMap<>();
    }
    public ViewUtil(String view, Map<String, Object> values) {
        this.view = view;
        this.values = values;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public void addValue(String key, Object value) {
        this.values.put(key, value);
    }

}