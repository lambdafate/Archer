package Archer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util
 */
public class Util {



    protected static String GetSessionValueFromCookie(String cookie){
        if(cookie == null || cookie.length() == 0){
            return null;
        }
        String[] kvs = cookie.split(";");
        for (String kv : kvs) {
            String[] info = kv.strip().split("=");
            if (info.length == 2 && info[0].equals(Archer.session_name)) {
                return info[1];
            }
        }
        return null;
    }


    protected static String ConvertUrl(String url){
        return url.replaceAll("<.*>", "([^/]+)");
    }

    protected static boolean CheckUrlFormat(String url){
        if(url == null || url.length() == 0){
            return false;
        }
        if(url.charAt(0) != '/'){
            return false;
        }
        String pattern = "<.*?>";
        Matcher m = Pattern.compile(pattern).matcher(url);
        int num = 0;
        while (m.find()) {
            num++;
        }
        if(num > 1){
            return false;
        }
        if(num == 0){
            return true;
        }
        m = Pattern.compile("/(.*?)/<.*>(/)?").matcher(url);
        if(m.find()){
            return true;
        }
        return false;
    }



    protected static HashMap<String, String> parseQueryArgs(String query_args){
        HashMap<String, String> res = new HashMap<>();
        String[] args = query_args.split("&");
        for (String arg : args) {
            String[] kv = arg.split("=");
            // assert (kv.length == 2);
            if (kv.length == 2) {
                res.put(kv[0], kv[1]);
            }
        }
        return res;
    }


    protected static String GetFileSuffix(String filepath){
        if(filepath == null || filepath.length() == 0){
            return null;
        }
        int index = filepath.lastIndexOf(".");
        if(index == -1){
            return null;
        }
        return filepath.substring(index+1);
    }

    // check this url if wants to get a static file, like png, jpg, css
    protected static boolean CheckStaticResourse(String url){
        if(url == null || url.length() == 0){
            return false;
        }
        if(url.startsWith("/" + Archer.static_path) || url.equals("/favicon.ico")){
            return true;
        }
        return false;
    }

    protected static String ReadResource(String filepath) throws IOException {
        StringBuilder sb = new StringBuilder();
        File file = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        char buffer[] = new char[256];
        int n = -1;
        while ((n = br.read(buffer)) > 0) {
            sb.append(buffer, 0, n);
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
        // test chech url
        String[] urls = {
            "as", "/as",
            "/index/", "/index/<saber>/", "index/<saber>/<lancer>"
        };
        boolean[] result = {
            false, true,
            true, true, false
        };
        for (int i = 0; i < result.length; i++) {
            if(CheckUrlFormat(urls[i]) != result[i]){
                System.out.println("url format error");
                System.exit(1);
            }
            if(result[i]){
                urls[i] = ConvertUrl(urls[i]);
                System.out.println(urls[i]);
            }
        }
        System.out.println("**********");

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