package Archer;

import java.util.HashMap;

/**
 * Request
 */

public class Request {
    public static String method(){
        return Archer.HttpRequest.get().method;
    }

    public static String args(String key){
        var r = Archer.HttpRequest.get();
        return r.args.get(key);
    }

    public static HashMap<String, String> args(){
        return Archer.HttpRequest.get().args;
    }
    
}