import archer.*;

/**
 * App: for testing archer
 */

public class App {

    @Archer.router(path="/login", method={"GET", "POST"})
    public static Object login(){
        if(islogin()){
            Response.redirect_to("/");
        }
        if(Request.method().equals("GET")){
            return "login.html";
        }
        Session.set("login", true);
        return Response.redirect_to("/");
    }

    @Archer.router(path="/", method={"GET"})
    public static Object index(){
        if(!islogin()){
            return Response.redirect_to("/login");
        }
        return "index.html";
    }

    public static boolean islogin(){
        return Session.get("login") != null && (Boolean)Session.get("login");
    }

    public static void main(String[] args) {
        var archer = new Archer(App.class);
        archer.run(8888);
    }
}