package Archer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public interface Application {
    public ArrayList<String> call_app(HashMap<String, String> environ, CallBack callback) throws Exception;
}