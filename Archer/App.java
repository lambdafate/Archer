package Archer;

import java.util.*;

/**
 * App: for testing archer
 */

public class App {

    @Archer.router(path="/index", method={"POST"})
    public static Object index(){
        // System.out.println("name: " + name);
        if(Request.method().equals("GET")){
            return "index.html";
        }
        return Request.form();
    }

    @Archer.router(path="/", method={"POST"})
    public static Object hello(){
        Map<Object, Object> map = new HashMap<>();
        map.put("bool", true);
        map.put("integer", 20);
        map.put("double", 10.56);

        Map<Object, Object> map2 = new HashMap<>();
        map2.put("name", "你的名字");
        map2.put("age", 20);

        List<Object> list = new ArrayList<>();
        list.add(1);
        list.add("fuck");
        list.add(map2);

        map.put("list", list);
        map.put("map", map2);

        Response.set_header("key", "value");
        return map;
    }

    public static void main(String[] args) {
        Archer app = new Archer();
        app.run(8888);
    }
}