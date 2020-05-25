package Archer;

import java.util.HashMap;

/**
 * HttpResponse
 */
public class HttpResponse {
    protected String response_status;
    protected HashMap<String, String> response_header = new HashMap<>();

    public HttpResponse(){
        response_status = "200 OK";
        response_header.put("Content-Type", "text/html");
        response_header.put("Server", Server.version);
    }
}