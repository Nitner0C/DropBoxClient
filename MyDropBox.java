import com.dropbox.core.*;

import java.io.BufferedReader;
import java.io.*;
import java.util.Locale;

/**
 * Created by Corentin on 27/03/14.
 */
public class MyDropBox{

    private final String APP_KEY = "YOUR_APP_KEY";
    private final String APP_SECRET = "YOUR_APP_SECRET";
    private DbxWebAuthNoRedirect webAuth;
    private String authorizeUrl;
    private DbxRequestConfig config;
    private DbxClient client;
    private String name;

    public String getName() {
        return name;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public MyDropBox()
    {
        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
        config = new DbxRequestConfig(
                "DropBoxClient/1.0", Locale.getDefault().toString());
        webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        authorizeUrl = webAuth.start();
    }

    public boolean connect(String code){
        DbxAuthFinish authFinish = null;
        try {
            authFinish = webAuth.finish(code);
        } catch (DbxException e) {
            return false;
        }
        String accessToken = authFinish.accessToken;
        client = new DbxClient(config, accessToken);
        try {
            name = client.getAccountInfo().displayName;
        } catch (DbxException e) {
        }
        return true;
    }

    public DbxEntry.WithChildren getData(String path) {
        DbxEntry.WithChildren list;

        try {
            list = client.getMetadataWithChildren(path);
        } catch (DbxException e) {
            list = null;
        }
        return list;
    }

    public void download(String path, String name){
        String output = System.getenv("HOME") + "/Desktop/Dpx/" + name;
        FileOutputStream outputStream = null;
        File f = new File(output);
        if (!f.exists() || f.isDirectory())
        {
            try {
                f.getParentFile().mkdirs();
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            outputStream = new FileOutputStream(output);
        } catch (FileNotFoundException e) {
            return;
        }
        try {
            client.getFile(path, null, outputStream);
        } catch (IOException e) {
            return;
        } catch (DbxException e) {
            return;
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                return;
            }
        }
    }
    public void upload(File f, String p) {
        File inputFile = new File(f.getPath());

        if (!p.endsWith("/")) {
            p += "/";
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(inputFile);
            DbxEntry.File uploadedFile = client.uploadFile(p + f.getName(),
                    DbxWriteMode.add(), inputFile.length(), inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DbxException e) {
            e.printStackTrace();
        }

    }

    public void remove(String path) {
        try {
            client.delete(path);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public void createFolder(String path)
    {
        try {
            client.createFolder(path);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }
}
