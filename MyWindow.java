import com.dropbox.core.DbxEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Corentin on 27/03/14.
 */
public class MyWindow extends JFrame {

    private static final String LOGIN = "login";
    private static final String LISTING = "listing";

    private JPanel cards;
    private JList listing;
    private String actPath;
    private Stack<String> oldPaths;

    public MyWindow(String title, int width, int length) {
        oldPaths = new Stack<String>();
        this.setTitle(title);
        this.setSize(width, length);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        cards = new JPanel(new CardLayout());
        Component log = initLogin();
        Component lis = initListing();

        cards.add(LOGIN, log);
        cards.add(LISTING, lis);

        this.setContentPane(cards);
        this.pack();
        this.setVisible(true);
    }

    private Component initListing() {

        JPanel p = new JPanel();
        JScrollPane s = new JScrollPane();
        s.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        s.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        listing = new JList();
        listing.setMinimumSize(new Dimension(500, 500));
        listing.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    DbxEntry clickedOn;
                    if (index >= 0)
                    {
                        clickedOn = ((MyDbxEntry) list.getModel().getElementAt(index)).getE();
                        if (clickedOn.isFolder()) {
                            refreshListing(clickedOn.path, false);
                        }
                    }
                }
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    int index = list.locationToIndex(evt.getPoint());
                    if (index >= 0)
                    {
                        final DbxEntry clickedOn = ((MyDbxEntry) list.getModel().getElementAt(index)).getE();
                        JPopupMenu m = new JPopupMenu();
                        if (clickedOn.isFile()) {
                            JMenuItem dl = new JMenuItem("Download");
                            dl.setActionCommand("DL");
                            dl.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent actionEvent) {
                                    MyDbx.getInstance().download(clickedOn.path, clickedOn.name);                            }
                            });
                            m.add(dl);
                        }
                        JMenuItem delete = new JMenuItem("Delete");
                        delete.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent actionEvent) {
                                MyDbx.getInstance().remove(clickedOn.path);
                                refreshListing(actPath, true);
                            }
                        });
                        m.add(delete);
                        m.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }
            }
        });
        s.setViewportView(listing);
        final JButton uploadFile = new JButton("Upload File");
        uploadFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser c = new JFileChooser();

                int rVal = c.showOpenDialog(uploadFile);
                if (rVal == JFileChooser.APPROVE_OPTION) {
                    MyDbx.getInstance().upload(c.getSelectedFile(), actPath);
                    refreshListing(actPath, true);
                }
            }
        });
        JButton back = new JButton("Back");
        back.setPreferredSize(uploadFile.getPreferredSize());
        back.setMinimumSize(uploadFile.getMinimumSize());
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String path;

                if (oldPaths.isEmpty()) {
                    path = "/";
                } else {
                    path = oldPaths.pop();
                }
                refreshListing(path, true);
            }

        });

        JButton newFolder = new JButton("New Folder");
        newFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String folderName = JOptionPane.showInputDialog ("Enter Folder name");
                if (folderName != null && !folderName.isEmpty())
                {
                    String p = actPath;
                    if (!p.endsWith("/"))
                    {
                        p += "/";
                    }
                    MyDbx.getInstance().createFolder(p + folderName);
                    refreshListing(actPath, true);
                }
            }
        });

        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(10, 15, 0, 0);
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        p.add(back, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.insets = new Insets(0, 15, 0, 10);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.BASELINE_TRAILING;
        p.add(uploadFile, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.;
        c.weighty = 1.;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(15, 15, 15, 15);
        p.add(s, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(15, 15, 0, 15);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.BASELINE_TRAILING;
        p.add(newFolder, c);


        return p;
    }

    private void refreshListing(String path, boolean back)
    {
        if (!back){
            oldPaths.add(actPath);
        }
        actPath = path;
        DbxEntry.WithChildren dataListing = MyDbx.getInstance().getData(path);
        ArrayList<MyDbxEntry> data = new ArrayList<MyDbxEntry>();
        for (DbxEntry child : dataListing.children) {
            data.add(new MyDbxEntry(child));
        }
        listing.setListData(data.toArray());
        listing.validate();
    }

    private Component initLogin() {
        JPanel login = new JPanel();
        login.setLayout(new GridBagLayout());


        ClipboardOwner cp = new ClipboardOwner() {
            @Override
            public void lostOwnership(Clipboard clipboard, Transferable transferable) {
            }
        };
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection ss = new StringSelection(MyDbx.getInstance().getAuthorizeUrl());
        cb.setContents(ss, cp);

        Component w = new JLabel("Welcome on my Dbx app!");
        Component step1 = new JLabel("1) Go to the link in your clipboard");
        Component step2 = new JLabel("Click \"Allow\" to allow the app to access your files.");
        Component code = new JLabel("Paste code here :");
        final JTextArea lbl = new JTextArea();
        final JTextField tf = new JTextField();

        final JButton copy = new JButton("Copy link");
        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ClipboardOwner cp = new ClipboardOwner() {
                    @Override
                    public void lostOwnership(Clipboard clipboard, Transferable transferable) {
                    }
                };
                Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection ss = new StringSelection(MyDbx.getInstance().getAuthorizeUrl());
                cb.setContents(ss, cp);
            }
        });

        final JButton b = new JButton("Submit");
        b.setPreferredSize(copy.getPreferredSize());
        b.setMinimumSize(copy.getMinimumSize());
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                lbl.setText("Please wait :)");
                boolean connected = MyDbx.getInstance().connect(tf.getText());
                if (connected) {
                    lbl.setText("You can now use my app");
                    b.setVisible(false);
                    actPath = "/";
                    oldPaths.push("/");
                    setTitle("DropBox App : " + MyDbx.getInstance().getName());
                    refreshListing("/", false);
                    CardLayout cl = (CardLayout) (cards.getLayout());
                    cl.show(cards, LISTING);
                } else {
                    lbl.setText("Code mistake, please copy/paste the code given at the url");
                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 15, 0, 0);
        login.add(w, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 15, 0, 10);
        login.add(step1, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 15, 0, 10);
        login.add(step2, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.;
        gbc.weighty = 0.;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        gbc.insets = new Insets(10, 15, 0, 10);
        login.add(code, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.BASELINE;
        gbc.insets = new Insets(10, 5, 0, 10);
        login.add(tf, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 15, 0, 10);
        login.add(lbl, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        gbc.insets = new Insets(0, 15, 0, 10);
        login.add(b, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        gbc.insets = new Insets(10, 15, 0, 0);
        login.add(copy, gbc);

        return login;
    }
}
