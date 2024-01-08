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
     * 参考自https://blog.51cto.com/u_15082395/2645155
     */
    public static <T>  T getValue(Map map , String key){ // obtain the specific parameter
        String separator = ".";
        String[] separatorKeys = null;
        if (key.contains(separator)) {
            // 取下面配置项的情况, user.path.keys 这种
            separatorKeys = key.split("\\.");
        } else {
            // 直接取一个配置项的情况, user
            Object res = map.get(key);
            return res == null ? null : (T) res;
        }
        // 下面是取多个的情况
        String finalValue = null;
        Object tempObject = map;
        for (int i = 0; i < separatorKeys.length; i++) {
            //如果是user[0].path这种情况,则按list处理
            String innerKey = separatorKeys[i];
            Integer index = null;
            if (innerKey.contains("[")) {//innerKey = user[0]
                index = Integer.valueOf(GetSubString(innerKey, "[", "]"));//index = 0
                innerKey = innerKey.substring(0, innerKey.indexOf("["));//innerKey = user
            }
            Map mapTempObj = (Map) tempObject;
            Object object = mapTempObj.get(innerKey);
            // 如果没有对应的配置项,则返回设置的默认值
            if (object == null) {
                return null;
            }
            Object targetObj = object;
            if (index != null) {
                // 如果是取的数组中的值,在这里取值
                targetObj = ((ArrayList) object).get(index);
            }
            // 一次获取结束,继续获取后面的
            tempObject = targetObj;
            if (i == separatorKeys.length - 1) {
                //循环结束
                return (T) targetObj;
            }

        }

        return null;
    }
}
