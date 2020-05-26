package Archer;

import java.util.*;

/**
 * App: for testing archer
 */

public class App {

    @Archer.router(path="/login", method={"GET", "POST"})
    public static Object login(){
        if(Request.method().equals("GET")){
            return "index.html";
        }

        String first = Request.form("firstname");
        String second = Request.form("lastname");

        Session.set("firstname", first);
        Session.set("lastname",  second);
        Session.set("login", true);
        
        return Response.redirect_to("/");
    }

    @Archer.router(path="/", method={"GET"})
    public static Object hello(){
        if(Session.get("login") != null && (Boolean)Session.get("login")){
            return Session.getSession();
        }
        return "你没有登陆!";
    }

    public static void main(String[] args) {
        Archer app = new Archer();
        app.run(8888);
    }
}