package Archer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util
 */
public class Util {

    protected static String ReadResource(String filepath) throws IOException {
        StringBuilder sb = new StringBuilder();
        File file = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        assert (br != null);
        String line = "";
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\r\n");
        }
        br.close();
        return sb.toString();
    }

    /*
        parse map -> string.  http response header's content-type=application/json
        for example:
            {
                "bool": true,
                "integer": 20,
                "list": ["abc", {...}, ...],
                "map": {...} 
            }
    */
    protected static String ParseMaptoString(Map<Object, Object> map){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean[] flag = {false};
        map.forEach((k, v) -> {
            // sb.append("\t");
            if (flag[0]) {
                sb.append(",");
            } else {
                flag[0] = true;
            }
            sb.append("\"" + k + "\": ");
            sb.append(parseObject(v));
            // sb.append("\n");
        });
        sb.append("}");
        return sb.toString();
    }

    private static String parseObject(Object v){
        String res = "";
        if (isConstant(v)) {
            res = (parseConstant(v));
        } else if (v instanceof List) {
            res = (parseList((List<Object>) v));
        } else if (v instanceof Map) {
            res = (ParseMaptoString((Map<Object, Object>) v));
        }
        return res;
    }

    private static boolean isConstant(Object v){
        return v instanceof Boolean || v instanceof Integer || v instanceof Long || v instanceof Double || v instanceof Float || v instanceof String;
    }

    private static String parseConstant(Object constant){
        if(constant instanceof String){
            return "\"" + constant + "\"";
        }
        return "" + constant;
    }

    private static String parseList(List<Object> list){
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        sb.append("[");
        for (Object v : list) {
            if(flag){
                sb.append(", ");
            }else{
                flag = true;
            }
            sb.append(parseObject(v));
        }
        sb.append("]");
        return sb.toString();
    }



    public static void main(String[] args) {
        // test parse map to string
        Map<Object, Object> map = new HashMap<>();
        map.put("bool", true);
        map.put("integer", 20);
        map.put("double", 10.56);

        Map<Object, Object> map2 = new HashMap<>();
        map2.put("name", "lambdafate");
        map2.put("age", 20);

        List<Object> list = new ArrayList<>();
        list.add(1);
        list.add("fuck");
        list.add(map2);

        map.put("list", list);
        map.put("map", map2);

        String res = ParseMaptoString(map);
        System.out.println(res);
    }



}