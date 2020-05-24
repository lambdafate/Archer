package Archer;

/**
 * App: for testing archer
 */

public class App {

    @Archer.router(path="/index", method="get")
    public static Object index(){
        
        return "index.html";
    }

    @Archer.router(path="/")
    public static Object hello(){
        
        return "welcome.html";
    }

    public static void main(String[] args) {
        Archer app = new Archer();
        app.run(8888);
    }
}