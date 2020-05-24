package Archer;

/**
 * App: for testing archer
 */

public class App {

    @Archer.router(path="/index", method="get")
    public static String index(){
        
        return "<h2 style=\"text-align:center;\">Welcome to Archer!<h2>";
    }

    @Archer.router(path="/")
    public static String hello(){

        return "<a href=\"/index\"> Click me!</a>";
    }

    public static void main(String[] args) {
        Archer app = new Archer();
        app.run(8888);
    }
}