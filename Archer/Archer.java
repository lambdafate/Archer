package Archer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Archer implements Application {
    public static HashMap<String, Method> url_map = new HashMap<>();
    protected static ThreadLocal<HttpRequest> HttpRequest = new ThreadLocal<>();
    protected static ThreadLocal<HttpResponse> HttpResponse = new ThreadLocal<>();


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface router {
        String path() default "";

        String method() default "get";
    }

    protected static String template_path = "template/";
    protected static String static_path = "static/";
    protected static String default_suffix = ".html";

    private HttpRequest PreprocessHttpRequest(HashMap<String, String> environ) {
        var http = new HttpRequest();
        // request method: get or post
        http.method = environ.get("REQUEST_METHOD");
        // request url and query args
        String query_url = environ.get("PATH_INFO");
        String[] info = query_url.split("\\?");
        http.url = info[0];
        if (info.length >= 2) {
            String[] args = info[1].split("&");
            for (String arg : args) {
                String[] kv = arg.split("=");
                // assert (kv.length == 2);
                if(kv.length == 2){
                    http.args.put(kv[0], kv[1]);
                }
            }
        }

        // request form
        if (http.method.toUpperCase() == "POST") {

        }

        return http;
    }

    private Method MatchRequest(String url) {
        return url_map.getOrDefault(url, null);
    }

    private String DispatchRequest(String url) {
        Method handler = MatchRequest(url);
        if (handler == null){
            Response.set_response(404);
            return null;
        }

        String body = "";
        try {
            Object res = handler.invoke(null);
            // now we only support String, which means return a html file path
            // assert (res instanceof String);
            if (res instanceof String) {
                body = (String) res;
                if(body.endsWith(Archer.default_suffix)){
                    body = Util.ReadResource(Archer.template_path + (String)res);
                }
            } else if (res instanceof Map) {
                body = Util.ParseMaptoString((Map<Object, Object>)res);
                Response.set_header("Content-Type", "application/json");
            }else{
                throw new Exception("Unsupport this type " + res);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }

    public ArrayList<String> call_app(HashMap<String, String> environ, CallBack callback) throws IOException{
        ArrayList<String> body = new ArrayList<>();

        // set threadlocal request valiable
        var http = PreprocessHttpRequest(environ);
        HttpRequest.set(http);

        // set threadlocal response valiable
        var response = new HttpResponse();
        HttpResponse.set(response);

        // Method handler = MatchRequest(http.url);
        // if(handler == null){
        //     callback.start_response("404 NotFound", response_header);
        //     return body;
        // }

        String res = DispatchRequest(http.url);
        callback.start_response(Response.response_status() , Response.response_header());
        
        body.add(res);
        return body;
    }

    public void run(int port){
        try{
            System.out.println(Util.ReadResource("./archer.logo"));
        }catch(IOException e){
            System.err.println("reading logo error: " + e);
        }
        
        System.out.println("Welcome to Archer!");
        System.out.println("run server in port: " + port + "\n");

        // build url map
        System.out.println("check all methods for building url map....");
        Method[] methods = App.class.getDeclaredMethods();
        for (Method method : methods) {
            Archer.router router = method.getAnnotation(Archer.router.class);
            if(router != null){
                System.out.print(method.getName() + ": router annotation ---> ");
                System.out.println("path: " + router.path() + "\t" + "method: " + router.method());
                Archer.url_map.put(router.path(), method);
            }
        }

        // make a server
        Server server = new Server();
        server.run_server(port, this);
    }
}