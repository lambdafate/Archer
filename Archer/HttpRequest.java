package Archer;

import java.util.HashMap;

/**
 * HttpRequest
 */
public class HttpRequest {
    protected String method;
    protected String url;
    protected HashMap<String, String> args = new HashMap<>();
    protected HashMap<String, String> form = new HashMap<>();

}