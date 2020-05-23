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
    private static int port = 8888;

    public static void main(String[] args) {
        ServerSocket server;
        try {
            server = new ServerSocket(port);
            System.out.println("run server in " + port + "....");

            while(true){
                Socket socket = server.accept();
                new Handler(socket).start();
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

    private HashMap<String, String> environ = new HashMap<>();
    private String response_line;
    private HashMap<String, String> response_header = new HashMap<>();

    public Handler(Socket socket) throws IOException{
        this.socket = socket;
        if(socket != null){
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }
    }

    private HashMap<String, String> parse(BufferedReader reader) throws IOException{
        // HashMap<String, String> environ = new HashMap<>();
        String request_line = reader.readLine();
        if(request_line.length() == 0){
            return environ;
        }
        
        String[] info = request_line.split(" ");
        environ.put("REQUEST_METHOD", info[0]);
        environ.put("PATH_INFO", info[1]);
        environ.put("REQUEST_PROTOCOL", info[2]);

        String request_header = "";
        while(!(request_header = reader.readLine()).equals("")){
            String[] header = request_header.split(": ");
            environ.put(header[0], header[1]);
        }

        return environ;
    }

    public void start_response(String response_line, HashMap<String, String> response_header) throws IOException {
        writer.write(environ.get("REQUEST_PROTOCOL") + " " + response_line + "\r\n");
        for (String k : response_header.keySet()) {
            writer.write(k + ": " + response_header.get(k) + "\r\n");
        }
        writer.write("\r\n");
    }


    @Override
    public void run(){
        try {
            HashMap<String, String> environ = parse(reader);
            if(environ.isEmpty()){
                return;
            }
            System.out.println("connect from [" + socket.getRemoteSocketAddress() + "] ----- " + environ.get("PATH_INFO"));
            
            Application a = new Archer();

            Application archer = new Archer();
            ArrayList<String> body = archer.call_app(environ, this);

            // StringBuilder sb = new StringBuilder();

            // sb.append("HTTP/1.1 200 OK\r\n");
            // sb.append("Content-Type: text/html\r\n");
            // sb.append("\r\n");
            // sb.append("<h2>Welcome to Archer!</h2>");

            for (String string : body) {
                writer.write(string);
            }
            
            writer.flush();
            reader.close();
            socket.close();

        } catch (Exception e) {
            //TODO: handle exception
            System.out.println(e);
        }finally{

        }
    }
}