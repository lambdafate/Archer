package archer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * Server
 */
public class Server {
    public static String version = "Archer/0.1";

    public static String binary_content_type = "application/octet-stream";
    public static String text_content_type = "text/plain";

    public static HashMap<String, String> general_files = new HashMap<>();
    static {
        general_files.put("html", "text/html");
        general_files.put("css", "text/css");
        general_files.put("js", "application/x-javascript");

        general_files.put("jpg", "image/jpeg");
        general_files.put("jpe", "image/jpeg");
        general_files.put("jpeg", "image/jpeg");
        general_files.put("png", "image/png");
        general_files.put("gif", "image/gif");
        general_files.put("ico", "image/x-icon");

        general_files.put("doc", "application/msword");
        general_files.put("pdf", "application/pdf");
        general_files.put("txt", "text/plain");

        general_files.put("json", "application/json");
    }

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
    private DataInputStream reader;
    private DataOutputStream writer;

    private Application application;

    private HashMap<String, String> environ = new HashMap<>();

    public Handler(Socket socket, Application application) throws IOException{
        this.socket = socket;
        this.application = application;
        if(socket != null){
            reader = new DataInputStream(socket.getInputStream());
            writer = new DataOutputStream(socket.getOutputStream());
        }
    }

    private HashMap<String, String> parse(DataInputStream reader) throws IOException{
        HashMap<String, String> environ = new HashMap<>();
    
        String request_line = reader.readLine();
        if(request_line == null || request_line.length() == 0){
            return environ;
        }
        String[] info = request_line.split(" ");
        if(info.length != 3){
            return environ;
        }
        environ.put("REQUEST_METHOD", info[0].toUpperCase());
        environ.put("PATH_INFO", info[1]);
        environ.put("REQUEST_PROTOCOL", info[2]);

        String request_header = "";
        while(!(request_header = reader.readLine()).equals("")){
            String[] header = request_header.split(": ");
            environ.put(header[0], header[1]);
        }
        
        // read 'form data' if request method is post?
        // we make socket shutdowmInput here 
        // for reading all bytes lefe in InputStream , if not do,
        // the reading below will block until socket close.  
        socket.shutdownInput();

        StringBuilder request_body = new StringBuilder();
        String line = "";
        while((line = reader.readLine()) != null){
            request_body.append(line);
        }
        // System.out.println(request_body);
        environ.put("body", request_body.toString());

        return environ;
    }

    public void start_response(String response_line, HashMap<String, String> response_header) throws Exception {
        // default content-type
        if(response_header.getOrDefault("Content-Type", null) == null){
            response_header.put("Content-Type", "text/html; charset=utf-8");
        }
        // server info
        response_header.put("Server", Server.version);

        // Data
        response_header.put("Date", new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date()) + " GMT");
        
        WriteUTF8(environ.get("REQUEST_PROTOCOL") + " " + response_line + "\r\n");
        for (String k : response_header.keySet()) {
            WriteUTF8(k + ": " + response_header.get(k) + "\r\n");
        }
        WriteUTF8("\r\n");
    }

    private void response_static(String filepath) throws Exception {
        File file = new File(filepath.substring(1));
        HashMap<String, String> header = new HashMap<>();
        if(!file.exists()){
            start_response("404 Not Found", header);
            return;
        }
        String suffix = Util.GetFileSuffix(filepath);
        String default_type = (suffix == null? Server.binary_content_type: Server.text_content_type);
        header.put("Content-Type", Server.general_files.getOrDefault(suffix, default_type));
        start_response("200 OK", header);
        
        DataInputStream br = new DataInputStream(new FileInputStream(file));
        byte buffer[] = new byte[256];
        int n = -1;
        while((n = br.read(buffer)) > 0){
            writer.write(buffer, 0, n);
        }
        br.close();
        
    }


    private void WriteUTF8(String str) throws Exception {
        byte[] bytes = str.getBytes("UTF-8");
        writer.write(bytes, 0, bytes.length);
    }

    @Override
    public void run(){
        try {
            environ = parse(reader);
            if(environ.isEmpty()){
                return;
            }

            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]:").format(new Date()) + " connect from [" + socket.getRemoteSocketAddress() + "] ----- " + environ.get("REQUEST_METHOD") + "  " + environ.get("PATH_INFO"));
            
            if(Util.CheckStaticResourse(environ.getOrDefault("PATH_INFO", null))){
                response_static(environ.getOrDefault("PATH_INFO", null));   
            }else{
                ArrayList<String> body = application.call_app(environ, this);
                for (String data : body) {
                    if (data != null) {
                        WriteUTF8(data);
                    }
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