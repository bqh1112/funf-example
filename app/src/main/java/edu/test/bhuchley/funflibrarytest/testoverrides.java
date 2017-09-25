package edu.test.bhuchley.funflibrarytest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.json.JsonUtils;

/**
 * Created by Benjamin on 9/16/2017.
 */

public class testoverrides extends JsonElement {
    public testoverrides(JsonObject jsonObject) {
        if (jsonObject == null) {
            throw new IllegalStateException("Cannot create null IJsonObject");
        }
        SortedMap<String, JsonElement> map = new TreeMap<String, JsonElement>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            map.put(entry.getKey(), JsonUtils.immutable(entry.getValue()));
        }
        //members = Collections.unmodifiableSortedMap(map);
    }
}
