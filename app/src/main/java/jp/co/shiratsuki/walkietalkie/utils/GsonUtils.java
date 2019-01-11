package jp.co.shiratsuki.walkietalkie.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gson解析json工具类
 * Created by LiYuliang on 2017/6/5 0005.
 *
 * @author LiYuliang
 * @version 2017/10/27
 */

public final class GsonUtils {

    /**
     * 解析json字符串
     *
     * @param json
     * @param classT
     * @return
     */
    public static <T> T parseJSON(String json, Class<T> classT) {
        T info = null;
        try {
            Gson gson = new Gson();
            info = gson.fromJson(json, classT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * 解析json数组
     * <p>
     * Type所在的包：java.lang.reflect
     * TypeToken所在的包：com.google.gson.reflect.TypeToken
     * Type type = new TypeToken<ArrayList<TypeInfo>>(){}.getType();
     * List<TypeInfo> types = GsonUtils.parseJSONArray(jsonArr, type);
     *
     * @param jsonArr
     * @param type
     * @return
     */
    public static <T> T parseJSONArray(String jsonArr, Type type) {
        T infos = null;
        try {
            Gson gson = new Gson();
            infos = gson.fromJson(jsonArr, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infos;
    }

    public static String convertJSON(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }


    /**
     * 获取JsonObject
     * @param json
     * @return
     */
    public static JsonObject parseJson(String json){
        JsonParser parser = new JsonParser();
        return parser.parse(json).getAsJsonObject();
    }

    /**
     * 将JSONObjec对象转换成Map-List集合
     * @param json
     * @return
     */
    public static Map<String, Object> toMap(JsonObject json){
        Map<String, Object> map = new HashMap<String, Object>();
        Set<Map.Entry<String, JsonElement>> entrySet = json.entrySet();
        for (Iterator<Map.Entry<String, JsonElement>> iter = entrySet.iterator(); iter.hasNext(); ){
            Map.Entry<String, JsonElement> entry = iter.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if(value instanceof JsonArray)
                map.put((String) key, toList((JsonArray) value));
            else if(value instanceof JsonObject)
                map.put((String) key, toMap((JsonObject) value));
            else
                map.put((String) key, value);
        }
        return map;
    }

    /**
     * 将JSONArray对象转换成List集合
     * @param json
     * @return
     */
    public static List<Object> toList(JsonArray json){
        List<Object> list = new ArrayList<Object>();
        for (int i=0; i<json.size(); i++){
            Object value = json.get(i);
            if(value instanceof JsonArray){
                list.add(toList((JsonArray) value));
            }
            else if(value instanceof JsonObject){
                list.add(toMap((JsonObject) value));
            }
            else{
                list.add(value);
            }
        }
        return list;
    }
}
