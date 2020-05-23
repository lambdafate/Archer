package Archer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Archer implements Application {

    public ArrayList<String> call_app(HashMap<String, String> environ, CallBack callback) throws IOException{
        HashMap<String, String> response_header = new HashMap<>();
        response_header.put("Content-Type", "text/html");

        callback.start_response("200 OK", response_header);

        ArrayList<String> body = new ArrayList<>();
        body.add("<h2>Welcome to Archer!</h2>");
        for (String k : environ.keySet()) {
            body.add("<div>" + k + ": " + environ.get(k) + "</div>");
        }
        return body;
    }
}