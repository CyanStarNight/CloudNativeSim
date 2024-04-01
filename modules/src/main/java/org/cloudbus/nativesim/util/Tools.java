package org.cloudbus.nativesim.util;

import lombok.NonNull;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;

/**
 * @author JingFeng Wu
 */
public class Tools {
    public static boolean MatchWithLabels(List<String> labels1,List<String> labels2){
        return labels1.stream().anyMatch(labels2::contains);
    }
    public static List< Map<String,Object> > ReadMultilineYaml(String filePath) {
        InputStream inputStream = null;
        List<Map<String, Object>> maps = new ArrayList<>();
        Yaml yaml = new Yaml();
        try{
            inputStream = new FileInputStream(filePath);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        for (Object obj : yaml.loadAll(inputStream)){
            Map<String, Object> d = (Map<String, Object>) obj;
            maps.add(d);
        }
        assert !maps.isEmpty();
        return maps;
    }

    public static Map<String, Object> ReadYaml(String filePath){
        InputStream inputStream = null;
        Map<String, Object> map = null;
        Yaml yaml = new Yaml();
        try{
            inputStream = new FileInputStream(filePath);
            map = yaml.load(inputStream);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        assert map != null;
        return map;
    }

    public static Map<String,Object> ReadJson(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File(filePath);
        Map<String, Object> map = null;
        try {
            map = mapper.readValue(jsonFile, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e){
            e.printStackTrace();
        }
        assert map != null;
        return map;
    }

    //    public static String UUID(){
//        return UUID.randomUUID().toString();
//    }

    public static void callFunction(Object obj, String methodName) throws Exception {
        try {
            Method method = obj.getClass().getMethod(methodName, int.class);
            method.invoke(obj, 123);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Integer GetNumFromString(String s){
        if (s == null) return 0;
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(s);
        s = m.replaceAll("").trim();
        return Integer.valueOf(s).intValue();
    }

    public static String GetSubString(String s, String from, String end){//return the substring without "from" and "end"
        if (!s.contains(from) || !s.contains(end)) return null;
        int index_from = s.indexOf(from);
        int index_end = s.indexOf(end);
        return s.substring(index_from+from.length(),index_end);
    }

    /**
     * 从Map中获取配置的值
     * 传的key支持两种形式, 一种是单独的,如user.path.key
     * 一种是获取数组中的某一个,如 user.path.key[0]
     * 取不到配置时返回0或者null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Map<String, Object> configMap, String key) {
        if (configMap == null || key == null || key.isEmpty()) {
            return null;
        }

        String[] keyParts = key.split("\\.");

        // 遍历配置Map，逐级查找
        Object value = configMap;
        for (String part : keyParts) {
            if (part.contains("[")) {
                // 处理数组形式的key
                String arrayKey = part.substring(0, part.indexOf("["));
                int index = Integer.parseInt(part.replaceAll("[^0-9]", ""));
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(arrayKey);
                    if (value instanceof Object[]) {
                        value = ((Object[]) value)[index];
                    } else if (value instanceof Iterable) {
                        for (Object item : (Iterable<?>) value) {
                            if (index == 0) {
                                value = item;
                                break;
                            }
                            index--;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                // 处理普通形式的key
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(part);
                } else {
                    return null;
                }
            }

            if (value == null) {
                return null;
            }
        }

        // 返回泛型值
        try {
            @SuppressWarnings("unchecked")
            T result = (T) value;
            return result;
        } catch (ClassCastException e) {
            return null;
        }
    }
}
