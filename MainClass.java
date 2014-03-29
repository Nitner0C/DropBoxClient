/**
 * Created by Corentin on 20/02/14.
 */

import com.dropbox.core.*;

import java.io.*;


public class MainClass {
    public static void main(String[] args) throws IOException, DbxException{
        System.out.println(System.getenv());
        MyWindow mW = new MyWindow("DropBox App", 600, 600);
    }
}
