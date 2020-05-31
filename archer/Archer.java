package archer;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// import jdk.nashorn.internal.parser.JSONParser;

public class Archer implements Application {
    public static HashMap<String, Method> url_map = new HashMap<>();
    protected static ThreadLocal<HttpRequest> HttpRequest = new ThreadLocal<>();
    protected static ThreadLocal<HttpResponse> HttpResponse = new ThreadLocal<>();
    protected static ThreadLocal<HttpSession> HttpSession = new ThreadLocal<>();

    protected static HashMap<String, HttpSession> SessionPool = new HashMap<>();

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface router {
        String path() default "";
        String[] method() default {"GET"};
    }

    protected static String template_path = "template/";
    protected static String static_path = "static/";
    protected static String default_suffix = ".html";
    protected static String session_name = "Archer_SessionId";


    private Class appClass;
    public Archer(Class appClass){
        this.appClass = appClass;
    }


    private void PreprocessHttpSession(HashMap<String, String> environ){
        String cookie = environ.getOrDefault("Cookie", null);
        String value = null;
        if((value = Util.GetSessionValueFromCookie(cookie)) != null){
            synchronized(Archer.SessionPool){
                var session = Archer.SessionPool.getOrDefault(value, null);
                if(session != null){
                    Archer.HttpSession.set(session);
                    return;
                }
            }
        }

        value = String.valueOf(System.currentTimeMillis());
        Response.set_header("Set-Cookie", Archer.session_name + "=" + value +"; HttpOnly");
        HttpSession session = new HttpSession();
        synchronized(Archer.SessionPool){
            Archer.SessionPool.put(value, session);
        }
        Archer.HttpSession.set(session);
    }

    private HttpRequest PreprocessHttpRequest(HashMap<String, String> environ) {
        var http = new HttpRequest();
        // request method: get or post
        http.method = environ.get("REQUEST_METHOD");
        // request url and query args
        String query_url = environ.get("PATH_INFO");
        String[] info = query_url.split("\\?");
        http.url = info[0];
        if (info.length >= 2) {
            http.args = Util.parseQueryArgs(info[1]);
        }

        // now we only support 'get' and 'post'
        assert(http.method.equals("GET") || http.method.equals("POST"));

        if (http.method.equals("GET")) {
            return http;
        }
        // parse request form
        String content_type = environ.getOrDefault("Content-Type", "");
        String request_body = environ.getOrDefault("body", "");
        if(content_type.contains("application/x-www-form-urlencoded")){
            http.form = Util.parseQueryArgs(request_body);
        }else if(content_type.contains("application/json")){
            http.json = request_body;
        }
        
        return http;
    }

    private HashMap<String, Object> MatchRequest(String url) {
        HashMap<String, Object> res = new HashMap<>();
        Method handler = url_map.getOrDefault(url, null);
        if(handler != null){
            res.put("handler", handler);
            res.put("arg", null); // now we just support one arg
            return res;
        }
        for (String k : url_map.keySet()) {
            Matcher m = Pattern.compile(k).matcher(url);
            if(m.matches()){
                res.put("handler", url_map.get(k));
                if(m.groupCount() != 0){
                    res.put("arg", m.group(1));
                }
                break;
            }
        }
        return res;
    }

    private String DispatchRequest(String url) {
        HashMap<String, Object> fuck = MatchRequest(url);
        if (fuck.getOrDefault("handler", null) == null){
            Response.set_response(404);
            return null;
        }
        Method handler = (Method)fuck.get("handler");
        Object arg = fuck.getOrDefault("arg", null);
        
        // check if handler support this request method.
        boolean flag = false;
        router r = handler.getAnnotation(router.class);
        for (String request_type : r.method()) {
            if(request_type.toUpperCase().equals(Request.method())){
                flag = true;
                break;
            }
        }
        if(!flag){
            Response.set_response(405);
            return null;
        }

        String body = "";
        try {
            Object res;
            if(arg != null){
                res = handler.invoke(null, arg);
            }else{
                res = handler.invoke(null);
            }
            // now we only support String, which means return a html file path
            // assert (res instanceof String);
            if (res == null){

            }else if (res instanceof String) {
                body = (String) res;
                if(body.endsWith(Archer.default_suffix)){
                    body = Util.ReadResource(Archer.template_path + (String)res);
                }
            } else if (res instanceof Map) {
                body = Util.ParseMaptoString((Map<Object, Object>)res);
                Response.set_header("Content-Type", "application/json; charset=utf-8");
            }else{
                body = res.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }


    public ArrayList<String> call_app(HashMap<String, String> environ, CallBack callback) throws Exception{
        ArrayList<String> body = new ArrayList<>();
        
        // set threadlocal request valiable
        var http = PreprocessHttpRequest(environ);
        HttpRequest.set(http);

        // set threadlocal response valiable
        var response = new HttpResponse();
        HttpResponse.set(response);

        // deal with session
        PreprocessHttpSession(environ);

        String res = DispatchRequest(http.url);
        callback.start_response(Response.response_status() , Response.response_header());
        
        body.add(res);
        return body;
    }

    private void buildUrlMap(){
        Method[] methods = this.appClass.getDeclaredMethods();
        for (Method method : methods) {
            Archer.router router = method.getAnnotation(Archer.router.class);
            if(router != null){
                // System.out.print(method.getName() + ": router annotation ---> ");
                // System.out.println("path: " + router.path() + "\t" + "method: " + router.method());
                if(!Util.CheckUrlFormat(router.path())){
                    System.out.println("router path error: " + router.path());
                    System.exit(1);
                }
                String r = Util.ConvertUrl(router.path());
                if(url_map.getOrDefault(r, null) != null){
                    System.out.println("router path repeat: " + router.path());
                    System.exit(1);
                }
                // System.out.println(r + "\t---->\t" + method.getName());
                Archer.url_map.put(r, method);
            }
        }
    }

    public void run(int port){
        // display logo
        try{
            System.out.println(Util.ReadResource("./archer.logo"));
        }catch(IOException e){
            System.err.println("reading logo error: " + e);
        }
        
        System.out.println("Welcome to Archer!");
        System.out.println("run server in port: " + port + "\n");

        // build url map
        buildUrlMap();

        // make a server
        System.out.println("waiting for connections ......");
        Server server = new Server();
        server.run_server(port, this);
    }
}