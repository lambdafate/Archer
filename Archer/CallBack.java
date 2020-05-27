package Archer;

import java.util.HashMap;

public interface CallBack {
    public void start_response(String response_line, HashMap<String, String> response_header) throws Exception ;
}