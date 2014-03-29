/**
 * Created by Corentin on 27/03/14.
 */
public class MyDbx {
    private static MyDropBox dbx = null;

    public static MyDropBox getInstance() {
        if (dbx == null)
            dbx = new MyDropBox();
        return dbx;
    }
}
