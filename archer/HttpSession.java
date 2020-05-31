package Archer;

import java.util.HashMap;

/**
 * HttpSession
 */
public class HttpSession {

    private HashMap<String, Object> session = new HashMap<>();
    
    public Object get(String key){
        return session.getOrDefault(key, null);
    }

    public void set(String key, Object value){
        session.put(key, value);
    }
    
    public HashMap<String, Object> getSession(){
        return session;
    }
}