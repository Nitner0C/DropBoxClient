import com.dropbox.core.DbxEntry;

/**
 * Created by Corentin on 28/03/14.
 */
public class MyDbxEntry {
    private DbxEntry e;
    private String tS = "";

    public DbxEntry getE() {
        return e;
    }

    public MyDbxEntry(DbxEntry entry)
    {
        e = entry;
        if (entry.isFile()) {
            tS = "File : " + entry.name;
        } else {
            tS = "Folder : " + entry.path;
        }
    }

    public String toString()
    {
        return tS;
    }
}
