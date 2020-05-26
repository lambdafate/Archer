package Archer;

import java.util.HashMap;

/**
 * Session
 */
public class Session {

    public static Object get(String key){
        return Archer.HttpSession.get().get(key);
    }

    public static void set(String key, Object value){
        Archer.HttpSession.get().set(key, value);
    }

    public static HashMap<String, Object> getSession(){
        return Archer.HttpSession.get().getSession();
    }
}