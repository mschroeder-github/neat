package com.github.mschroeder.github.neat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import static javax.swing.SwingUtilities.invokeLater;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.similarity.FuzzyScore;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class EditorFrame extends javax.swing.JFrame {

    public static final Color editedTabColor = new Color(0, 152, 255);
    public static final Color savedTabColor = Color.black;

    public static final Options options = new Options();
    public static final CommandLineParser parser = new DefaultParser();

    static {
        options.addOption("h", "help", false, "prints this help");
    }

    public static final String APPNAME = "neat";
    public static final int PORT = (int) 'N' * 100 + (int) 'T'; //NeaT

    private final File configFolder;
    private final File pluginsFolder;

    private boolean disableShortcutListening;

    //private List<EditorPlugin> plugins;
    //private EditorPluginTableModel editorPluginTableModel;
    public EditorFrame(CommandLine cmd) {
        initComponents();

        setIconImage(getToolkit().getImage(EditorFrame.class.getResource("neat-16x16.png")));

        configFolder = new File(System.getProperty("user.home"), ".neat");
        configFolder.mkdirs();

        //load plugins from plugins folder
        pluginsFolder = new File(configFolder, "plugins");
        pluginsFolder.mkdirs();
        //use class path
        try {
            addPluginClassPath(pluginsFolder.getAbsolutePath());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        //load meta data
        EditorPluginRegistry.getInstance().load(pluginsFolder);

        //editorPluginTableModel = new EditorPluginTableModel();
        //jTablePluginParams.setModel(editorPluginTableModel);
        jListPlugins.addListSelectionListener((e) -> {
            if (!jListPlugins.isSelectionEmpty()) {
                updatePluginDetails(jListPlugins.getSelectedValue());
            }
        });
        jListPlugins.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                EditorPlugin plugin = (EditorPlugin) value;
                renderPlugin(l, plugin);
                return l;
            }
        });
        jTextFieldSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchForPlugin();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchForPlugin();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchForPlugin();
            }
        });

        searchForPlugin();
        initServer();

        interpreteCommandLine(cmd);

        setSize(1200, 800);
        setLocationRelativeTo(null);

        /*
        *   <code>true</code> if the KeyboardFocusManager should take no
        *   further action with regard to the KeyEvent; <code>false</code>
        *   otherwise
         */
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((KeyEvent evt) -> {
            if (disableShortcutListening) {
                return false;
            }

            if (evt.getID() == KeyEvent.KEY_PRESSED) { //.KEY_RELEASED) {
                return processEditorKeyEvent(evt);
            }
            return false;
        });

        try {
            recover();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    //==========================================================================
    //(remote) args
    private void initServer() {
        System.out.println("editor server runs at localhost:" + PORT);
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        server.createContext("/", new EditorHttpHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private class EditorHttpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange ex) throws IOException {
            System.out.println(ex.getRequestMethod() + " " + ex.getRequestURI());

            //pass arguments from remote
            if (ex.getRequestMethod().equals("POST") && ex.getRequestURI().getPath().equals("/args")) {
                String body = IOUtils.toString(ex.getRequestBody(), "UTF-8");
                JSONArray array = new JSONArray(body);
                String[] args = array.toList().toArray(new String[0]);

                CommandLine cmd;
                try {
                    cmd = parser.parse(options, args);
                } catch (ParseException ex1) {
                    throw new RuntimeException(ex1);
                }
                interpreteCommandLine(cmd);

                //OK
                ex.sendResponseHeaders(200, 0);
                return;
            }

            //not found
            ex.sendResponseHeaders(404, 0);
        }
    }

    //when new instance is created or via remote call
    private void interpreteCommandLine(CommandLine cmd) {
        System.out.println("options: " + Arrays.toString(cmd.getOptions()));
        System.out.println("args: " + cmd.getArgList());

        //open files in tabs
        for (String path : cmd.getArgList()) {
            File file = new File(path);
            if (!file.exists()) {
                continue;
            }

            if (file.length() > 200 * 1000 * 1000) {
                //TODO too big
                continue;
            }

            EditorPanel panel = createOrFocusTabFor(file);
            panel.requestFocus();
        }
    }

    //file = null also possible
    public EditorPanel createOrFocusTabFor(File file) {
        if (file == null) {
            EditorPanel panel = new EditorPanel(this);
            jTabbedPaneMain.add("untitled", panel);
            unsavedState(panel);
            return panel;
        }

        for (EditorPanel panel : getEditorPanels()) {
            if (panel.hasFile()) {
                continue;
            }

            if (panel.getFile().equals(file)) {
                jTabbedPaneMain.setSelectedComponent(panel);
                return panel;
            }
        }

        EditorPanel panel = new EditorPanel(this);
        jTabbedPaneMain.add(file.getName(), panel);
        //order important
        panel.setFile(file, true);
        return panel;
    }

    //==========================================================================
    //plugin management
    //need to do add path to Classpath with reflection since the URLClassLoader.addURL(URL url) method is protected:
    public static void addPluginClassPath(String s) throws Exception {
        File f = new File(s);
        URI u = f.toURI();
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> urlClass = URLClassLoader.class;
        Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(urlClassLoader, new Object[]{u.toURL()});
    }

    private void renderPlugin(JLabel label, EditorPlugin plugin) {
        String text = plugin.getName();
        if (plugin.hasShortcut()) {
            text += " <font color=\"gray\">(" + plugin.getShortcut().toString() + ")</font>";
        }
        label.setText("<html>" + text + "</html>");
    }

    private void searchForPlugin() {
        List<EditorPlugin> plugins = EditorPluginRegistry.getInstance().getPlugins();

        String text = jTextFieldSearch.getText();

        if (text.trim().isEmpty()) {
            jListPlugins.setListData(plugins.toArray(new EditorPlugin[0]));
            return;
        }

        //filter by string
        String query = jTextFieldSearch.getText().toLowerCase();
        FuzzyScore fs = new FuzzyScore(Locale.getDefault());
        Map<EditorPlugin, Integer> plugin2score = new HashMap<>();
        for (EditorPlugin plugin : plugins) {
            int maxScore = 0;
            List<String> names = new ArrayList<>();
            names.add(plugin.getName());
            names.addAll(plugin.getAltNames());
            for (String name : names) {
                int score = fs.fuzzyScore(name.toLowerCase(), query);
                maxScore = Math.max(maxScore, score);
            }
            plugin2score.put(plugin, maxScore);
        }
        EditorPlugin[] filtered
                = MapUtility
                        .toCountEntries(plugin2score, false, MapUtility.Sort.Descending)
                        .stream()
                        .map(e -> e.getKey())
                        .toArray(i -> new EditorPlugin[i]);
        jListPlugins.setListData(filtered);
        if (filtered.length > 0) {
            jListPlugins.setSelectedIndex(0);
        }
    }

    private void updatePluginDetails(EditorPlugin plugin) {
        StringBuilder sb = new StringBuilder();
        sb.append(plugin.getDesc());

        if (!plugin.getAltNames().isEmpty()) {
            sb.append("\n");
            sb.append("\n");
            sb.append("Alternative Names:\n");
            for (String altName : plugin.getAltNames()) {
                sb.append("  ").append(altName).append("\n");
            }
        }

        jTextAreaHelp.setText(sb.toString().trim());
        jTextAreaHelp.setCaretPosition(0);
    }

    private void runPlugin(EditorPlugin plugin) {
        EditorPluginContext ctx = new EditorPluginContext();
        ctx.setEditorFrame(this);
        ctx.setEditorPanel(this.getSelectedEditorPanel());
        if (this.getSelectedEditorPanel() != null) {
            ctx.setTextArea(this.getSelectedEditorPanel().getEditorTextArea());
        }

        System.out.println("run " + plugin.getName());
        plugin.run(ctx);
    }

    private void editPlugin(EditorPlugin plugin) {
        disableShortcutListening = true;
        EditorPluginDialog.showGUI(this, plugin);
        EditorPluginRegistry.getInstance().save();
        searchForPlugin();
        disableShortcutListening = false;
    }

    //==========================================================================
    //editor management
    public boolean processEditorKeyEvent(KeyEvent evt) {
        //search for plugin
        for (EditorPlugin p : EditorPluginRegistry.getInstance().getPlugins()) {
            if (p.isShortcutMatching(evt)) {
                evt.consume();
                runPlugin(p);
                return true;
            }
        }
        return false;
    }

    public void unsavedState(EditorPanel src) {
        jTabbedPaneMain.setForegroundAt(getIndexOf(src), editedTabColor);
    }

    public void savedState(EditorPanel src) {
        int index = getIndexOf(src);
        jTabbedPaneMain.setTitleAt(index, src.getFile().getName());
        jTabbedPaneMain.setForegroundAt(index, savedTabColor);
    }

    public int getIndexOf(EditorPanel panel) {
        return getEditorPanels().indexOf(panel);
    }

    public JTabbedPane getTabbedPaneMain() {
        return jTabbedPaneMain;
    }

    public JTextField getPluginSearch() {
        return jTextFieldSearch;
    }

    private EditorPanel getSelectedEditorPanel() {
        if (jTabbedPaneMain.getTabCount() == 0) {
            return null;
        }
        return (EditorPanel) jTabbedPaneMain.getSelectedComponent();
    }

    private List<EditorPanel> getEditorPanels() {
        List<EditorPanel> l = new ArrayList<>();
        for (int i = 0; i < jTabbedPaneMain.getTabCount(); i++) {
            l.add((EditorPanel) jTabbedPaneMain.getComponentAt(i));
        }
        return l;
    }

    public static DocumentListener withLambda(Consumer<DocumentEvent> c) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                c.accept(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                c.accept(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                c.accept(e);
            }
        };
    }

    //==========================================================================
    //recovery management
    
    private void backup() throws IOException {
        long begin = System.currentTimeMillis();

        File backup = new File(configFolder, "backup");

        FileUtils.deleteQuietly(backup);
        backup.mkdir();

        JSONArray order = new JSONArray();

        List<EditorPanel> editorPanels = getEditorPanels();
        for (EditorPanel panel : editorPanels) {

            JSONObject meta = new JSONObject();
            meta.put("file", panel.hasFile() ? panel.getFile().getAbsoluteFile() : null);
            meta.put("encoding", panel.getEncoding());
            meta.put("lineEnding", panel.getLineEnding());
            meta.put("id", panel.getId());
            meta.put("mimeType", panel.getMimeType());

            //store content
            File contentFile = new File(backup, panel.getId() + ".txt");
            FileUtils.writeStringToFile(contentFile, panel.getEditorTextArea().getText(), panel.getEncoding());

            //store meta
            File metaFile = new File(backup, panel.getId() + ".json");
            FileUtils.writeStringToFile(metaFile, meta.toString(2), StandardCharsets.UTF_8);

            //store order
            order.put(panel.getId());
        }

        FileUtils.writeStringToFile(new File(backup, "order.json"), order.toString(2), StandardCharsets.UTF_8);

        long end = System.currentTimeMillis();
        long duration = end - begin;
        System.out.println("backup in " + duration + " ms");
    }

    private void recover() throws IOException {
        long begin = System.currentTimeMillis();

        File backup = new File(configFolder, "backup");
        if (!backup.exists()) {
            return;
        }

        File orderFile = new File(backup, "order.json");
        if (!orderFile.exists()) {
            return;
        }

        JSONArray order = new JSONArray(FileUtils.readFileToString(orderFile, StandardCharsets.UTF_8));
        for (int i = 0; i < order.length(); i++) {
            String id = order.getString(i);

            File metaFile = new File(backup, id + ".json");
            JSONObject meta = new JSONObject(FileUtils.readFileToString(metaFile, StandardCharsets.UTF_8));
            File filefile = null;
            boolean useFile = false;
            if (meta.has("file")) {
                String filePath = meta.getString("file");
                if (filePath != null) {
                    filefile = new File(filePath);
                    useFile = filefile.exists();
                }
            }

            EditorPanel panel = createOrFocusTabFor(null);
            panel.setId(id);
            panel.setEncoding(meta.getString("encoding"));
            panel.setMimetype(meta.getString("mimeType"));
            panel.setLineEnding(meta.getString("lineEnding"));
            if (useFile) {
                panel.setFile(filefile, true);
            }

            File contentFile = new File(backup, id + ".txt");
            if (!useFile && contentFile.exists()) {
                String text = FileUtils.readFileToString(contentFile, StandardCharsets.UTF_8);
                panel.getEditorTextArea().setText(text);
            }
        }

        long end = System.currentTimeMillis();
        long duration = end - begin;

        System.out.println("recovered in " + duration + " ms");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jTextFieldSearch = new JTextFieldPlaceholder("Search ...");
        jSplitPanePlugin = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListPlugins = new javax.swing.JList<>();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextAreaHelp = new javax.swing.JTextArea();
        jTabbedPaneMain = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Neat");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jSplitPane1.setDividerLocation(300);

        jTextFieldSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldSearchKeyPressed(evt);
            }
        });

        jSplitPanePlugin.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jListPlugins.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListPlugins.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListPluginsMouseClicked(evt);
            }
        });
        jListPlugins.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jListPluginsKeyPressed(evt);
            }
        });
        jScrollPane2.setViewportView(jListPlugins);

        jSplitPanePlugin.setTopComponent(jScrollPane2);

        jTextAreaHelp.setEditable(false);
        jTextAreaHelp.setColumns(20);
        jTextAreaHelp.setLineWrap(true);
        jTextAreaHelp.setRows(5);
        jTextAreaHelp.setWrapStyleWord(true);
        jScrollPane4.setViewportView(jTextAreaHelp);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
        );

        jSplitPanePlugin.setBottomComponent(jPanel2);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTextFieldSearch)
            .addComponent(jSplitPanePlugin, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jTextFieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPanePlugin, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel1);
        jSplitPane1.setLeftComponent(jTabbedPaneMain);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        invokeLater(() -> {
            jSplitPane1.setDividerLocation(0.8);
            jSplitPanePlugin.setDividerLocation(0.8);
        });
    }//GEN-LAST:event_formWindowOpened

    private void jListPluginsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListPluginsMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() >= 2) {
            if (!jListPlugins.isSelectionEmpty()) {
                runPlugin(jListPlugins.getSelectedValue());
            }
        } else if (SwingUtilities.isRightMouseButton(evt) && evt.getClickCount() >= 2) {
            int index = jListPlugins.locationToIndex(evt.getPoint());
            jListPlugins.setSelectedIndex(index);
            if (!jListPlugins.isSelectionEmpty()) {
                editPlugin(jListPlugins.getSelectedValue());
            }
        }
    }//GEN-LAST:event_jListPluginsMouseClicked

    private void jListPluginsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListPluginsKeyPressed
        if (evt.getKeyCode() == VK_ENTER) {
            if (!jListPlugins.isSelectionEmpty()) {
                runPlugin(jListPlugins.getSelectedValue());
            }
        }
    }//GEN-LAST:event_jListPluginsKeyPressed

    private void jTextFieldSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldSearchKeyPressed
        int s = jListPlugins.getModel().getSize();
        int i = jListPlugins.getSelectedIndex();

        //activate selected
        if (i >= 0 && evt.getKeyCode() == VK_ENTER) {
            runPlugin(jListPlugins.getSelectedValue());
            return;
        }

        if (evt.getKeyCode() == VK_ESCAPE) {
            jTextFieldSearch.setText("");
            searchForPlugin();
            EditorPanel panel = getSelectedEditorPanel();
            if (panel != null) {
                panel.getEditorTextArea().requestFocus();
            }
            return;
        }

        //navigate
        if (s > 0) {
            if (i == -1) {
                jListPlugins.setSelectedIndex(0);
                return;
            }

            if (evt.getKeyCode() == VK_DOWN) {
                i++;
            } else if (evt.getKeyCode() == VK_UP) {
                i--;
            }

            i %= s;

            if (i < 0) {
                i = s + i;
            }

            jListPlugins.setSelectedIndex(i);
        }
    }//GEN-LAST:event_jTextFieldSearchKeyPressed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            backup();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }//GEN-LAST:event_formWindowClosing

    public static void sendArgs(String[] args) throws ProtocolException, MalformedURLException, IOException {
        URL url = new URL("http://localhost:" + PORT + "/args");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        byte[] out = new JSONArray(args).toString(2).getBytes("UTF-8");
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
        http.disconnect();
    }

    public static void main(String[] args) throws ParseException {
        //parse it
        CommandLine cmd = parser.parse(options, args);

        //help
        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(APPNAME, options);
            System.exit(0);
        }

        //send args to maybe running instance
        boolean newInst = false;
        try {
            sendArgs(args);
        } catch (IOException ex) {
            newInst = true;
        }

        //create new instance
        if (newInst) {
            java.awt.EventQueue.invokeLater(() -> {
                new EditorFrame(cmd).setVisible(true);
            });
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<EditorPlugin> jListPlugins;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPanePlugin;
    private javax.swing.JTabbedPane jTabbedPaneMain;
    private javax.swing.JTextArea jTextAreaHelp;
    private javax.swing.JTextField jTextFieldSearch;
    // End of variables declaration//GEN-END:variables
}
