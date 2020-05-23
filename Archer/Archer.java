package Archer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class Archer implements Application {
    public static HashMap<String, Method> url_map = new HashMap<>();

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface router {
        String path() default "";
        String method() default "get";
    }

    public ArrayList<String> call_app(HashMap<String, String> environ, CallBack callback) throws IOException{
        ArrayList<String> body = new ArrayList<>();
        HashMap<String, String> response_header = new HashMap<>();
        response_header.put("Content-Type", "text/html");

        Method handler = url_map.getOrDefault(environ.get("PATH_INFO"), null);
        if(handler == null){
            callback.start_response("404 NotFound", response_header);
            return body;
        }
        callback.start_response("200 OK", response_header);
        
        try{
            String res = (String)handler.invoke(null);
            body.add(res);

        }catch(Exception e){
            e.printStackTrace();
        }
        return body;
    }


    public void run(int port){
        File logo = new File("./archer.logo");
        if(logo.exists()){
            try{
                BufferedReader br = new BufferedReader(new FileReader(logo));
                String line = "";
                while(!(line = br.readLine()).equals("")){
                    System.out.println(line);
                }
                br.close();
            }catch(Exception e){ }
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
        Server.make_server(port, this);
    }
}