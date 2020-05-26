package Archer;

import java.util.HashMap;
import java.util.Map;

/**
 * Response
 */
public class Response {

    private static final Map<Integer, String> tips = Map.of(
        200, "OK",
        404, "Not Found",
        405, "Method Not Allowed"
    ); 

    public static void set_response(int code){
        set_response(code, tips.getOrDefault(code, " ")); 
    }

    public static void set_response(int code, String tip){
        Archer.HttpResponse.get().response_status = code + " " + tip;
    }

    public static void set_header(String k, String v){
        Archer.HttpResponse.get().response_header.put(k, v);
    }

    protected static String response_status(){
        return Archer.HttpResponse.get().response_status;
    }
    protected static HashMap<String, String> response_header(){
        return Archer.HttpResponse.get().response_header;
    }
}