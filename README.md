# Archer
![MIT LICENSE](https://img.shields.io/github/license/lambdafate/Archer?style=flat-square)  

Archer is a web framework, which contains a web server. It's made absolutely for learning and practice. I make it by refrencing [WSGI](https://zh.wikipedia.org/zh-hans/Web%E6%9C%8D%E5%8A%A1%E5%99%A8%E7%BD%91%E5%85%B3%E6%8E%A5%E5%8F%A3) and [flask](https://github.com/pallets/flask), but archer is simpler and do less work.  

### a simple example
```java
public class App {

    @Archer.router(path="/")
    public static Object index(){
        return "Hello, World!";
    }
    
    public static void main(String[] args) {
        var archer = new Archer(App.class);
        archer.run(8888);
    }
}
```
then you can click [http://127.0.0.1:8888/hello/Archer](http://127.0.0.1:8888/hello/Archer).  

### a another simple example
```java
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
```  
you can [click here](http://39.107.83.159:8888/) to look what happens.
