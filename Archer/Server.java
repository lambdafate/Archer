package Archer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Server
 */
public class Server {
    public static String version = "Archer/0.1";

    public void run_server(int port, Application application) {
        ServerSocket server;
        try {
            server = new ServerSocket(port);
            while(true){
                Socket socket = server.accept();
                new Handler(socket, application).start();
            }   
        }catch (IOException e){
            System.out.println(e);
        }catch (Exception e){
            System.out.println(e);
        }
    }
}

class Handler extends Thread implements CallBack{
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private Application application;

    private HashMap<String, String> environ = new HashMap<>();
    // private String response_line;
    // private HashMap<String, String> response_header = new HashMap<>();

    public Handler(Socket socket, Application application) throws IOException{
        this.socket = socket;
        this.application = application;
        if(socket != null){
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }
    }

    private HashMap<String, String> parse(BufferedReader reader) throws IOException{
        HashMap<String, String> environ = new HashMap<>();
        assert(reader != null);

        String request_line = reader.readLine();
        if(request_line == null || request_line.length() == 0){
            return environ;
        }
        String[] info = request_line.split(" ");
        if(info.length != 3){
            return environ;
        }
        environ.put("REQUEST_METHOD", info[0]);
        environ.put("PATH_INFO", info[1]);
        environ.put("REQUEST_PROTOCOL", info[2]);

        String request_header = "";
        while(!(request_header = reader.readLine()).equals("")){
            String[] header = request_header.split(": ");
            environ.put(header[0], header[1]);
        }

        // read 'form data' if request method is post?
        
        return environ;
    }

    public void start_response(String response_line, HashMap<String, String> response_header) throws IOException {
        // default content-type
        if(response_header.getOrDefault("Content-Type", null) == null){
            response_header.put("Content-Type", "text/html; charset=utf-8");
        }
        // server info
        response_header.put("Server", Server.version);

        writer.write(environ.get("REQUEST_PROTOCOL") + " " + response_line + "\r\n");
        for (String k : response_header.keySet()) {
            writer.write(k + ": " + response_header.get(k) + "\r\n");
        }
        writer.write("\r\n");
    }


    @Override
    public void run(){
        try {
            environ = parse(reader);
            if(environ.isEmpty()){
                return;
            }
            System.out.println("connect from [" + socket.getRemoteSocketAddress() + "] ----- " + environ.get("PATH_INFO"));
        
            ArrayList<String> body = application.call_app(environ, this);

            for (String data : body) {
                if(data != null){
                    writer.write(data);
                }
            }
            
            writer.flush();
            reader.close();
            socket.close();

        } catch (Exception e) {
            //TODO: handle exception
            e.printStackTrace();
        }finally{

        }
    }
}