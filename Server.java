import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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


class Handler extends Thread{
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public Handler(Socket socket) throws IOException{
        this.socket = socket;
        if(socket != null){
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }
    }

    @Override
    public void run(){
        try {
            String request_line = reader.readLine();
            
            System.out.println("connection from [" + socket.getRemoteSocketAddress() + "] ----- " + request_line);

            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 200 OK\r\n");
            sb.append("Content-Type: text/html\r\n");
            sb.append("\r\n");
            sb.append("<h2>Welcome to Archer!</h2>");

            writer.write(sb.toString());
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