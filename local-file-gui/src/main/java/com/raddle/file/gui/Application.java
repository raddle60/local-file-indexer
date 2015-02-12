package com.raddle.file.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.raddle.config.file.JsonFileConfigManager;
import com.raddle.file.gui.index.IndexManagerDialog;
import com.raddle.file.gui.option.OptionDialog;
import com.raddle.file.gui.search.SearchPane;
import com.raddle.file.gui.search.TabTitlePanel;
import com.raddle.index.config.GlobalConfig;

public class Application {

    {
        // Set Look & Feel
        try {
            javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JFrame frame;
    private IndexManagerDialog indexManagerFrame;
    private JsonFileConfigManager configManager;
    private JTabbedPane tabbedPane;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    Application window = new Application();
                    window.frame.setVisible(true);
                    window.frame.setExtendedState(Frame.MAXIMIZED_BOTH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public Application(){
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle("本地文件索引");
        frame.setBounds(100, 100, 818, 492);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Image image=Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icon.png"));
        frame.setIconImage(image);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu mnNewMenu = new JMenu("索引管理");
        menuBar.add(mnNewMenu);

        JMenuItem mntmNewMenuItem = new JMenuItem("管理...");
        mntmNewMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (indexManagerFrame == null) {
                    indexManagerFrame = new IndexManagerDialog();
                    indexManagerFrame.setLocationRelativeTo(frame);
                    indexManagerFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                    indexManagerFrame.setConfigManager(configManager);
                    indexManagerFrame.setModal(true);
                    indexManagerFrame.initData();
                }
                indexManagerFrame.setVisible(true);
            }
        });
        mnNewMenu.add(mntmNewMenuItem);

        JMenu menu_1 = new JMenu("搜索");
        menuBar.add(menu_1);

        JMenuItem menuItem_1 = new JMenuItem("新增");
        menuItem_1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addSearchPane();
            }
        });
        menu_1.add(menuItem_1);

        JMenu menu = new JMenu("配置");
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem("选项...");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                OptionDialog dialog = new OptionDialog();
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.setTitle("全局选项");
                dialog.setConfigManager(configManager);
                dialog.initData();
                dialog.setLocationRelativeTo(frame);
                dialog.setModal(true);
                dialog.setVisible(true);
                dialog.dispose();
            }
        });
        menu.add(menuItem);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        tabbedPane = new JTabbedPane(SwingConstants.TOP);
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    addSearchPane();
                }
            }
        });
        tabbedPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedIndex() != -1) {
                    Component selectedComponent = tabbedPane.getSelectedComponent();
                    if (selectedComponent instanceof SearchPane) {
                        SearchPane searchPane = (SearchPane) selectedComponent;
                        searchPane.updateOutFrameTitle();
                    }
                }
            }
        });
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        configManager = new JsonFileConfigManager();
        configManager.setConfigFile(System.getProperty("user.home") + "/local-file-search/config.json");
        configManager.init();
        initConfig();
        addSearchPane();
    }

    private void initConfig() {
        GlobalConfig globalConfig = configManager.getGlobalConfig();
        if (globalConfig.getSyntaxStyles() == null || globalConfig.getSyntaxStyles().size() == 0) {
            // 说明没有配置过，创建个默认的
            globalConfig.setTxtFileExts(new ArrayList<String>(Arrays.asList(StringUtils.split("txt,java,xml,properties,sql,jsp,html,htm,vm,ftl", ","))));
            globalConfig.setIgnoreFiles(new ArrayList<String>(Arrays.asList(StringUtils.split(".*,#*,~*,*.class,*.log", ","))));
            globalConfig.setIgnoreDirs(new ArrayList<String>(Arrays.asList(StringUtils.split(".*,#*,~*,target,work", ","))));
            Map<String, List<String>> styleMap = new HashMap<String, List<String>>();
            styleMap.put("text/c", new ArrayList<String>(Arrays.asList(StringUtils.split("c", ","))));
            styleMap.put("text/cpp", new ArrayList<String>(Arrays.asList(StringUtils.split("h,cpp", ","))));
            styleMap.put("text/cs", new ArrayList<String>(Arrays.asList(StringUtils.split("cs", ","))));
            styleMap.put("text/css", new ArrayList<String>(Arrays.asList(StringUtils.split("css", ","))));
            styleMap.put("text/dtd", new ArrayList<String>(Arrays.asList(StringUtils.split("dtd", ","))));
            styleMap.put("text/html", new ArrayList<String>(Arrays.asList(StringUtils.split("html,htm,vm,ftl", ","))));
            styleMap.put("text/java", new ArrayList<String>(Arrays.asList(StringUtils.split("java", ","))));
            styleMap.put("text/javascript", new ArrayList<String>(Arrays.asList(StringUtils.split("js", ","))));
            styleMap.put("text/json", new ArrayList<String>(Arrays.asList(StringUtils.split("json", ","))));
            styleMap.put("text/jsp", new ArrayList<String>(Arrays.asList(StringUtils.split("jsp", ","))));
            styleMap.put("text/php", new ArrayList<String>(Arrays.asList(StringUtils.split("php", ","))));
            styleMap.put("text/properties", new ArrayList<String>(Arrays.asList(StringUtils.split("properties", ","))));
            styleMap.put("text/python", new ArrayList<String>(Arrays.asList(StringUtils.split("py", ","))));
            styleMap.put("text/ruby", new ArrayList<String>(Arrays.asList(StringUtils.split("rb", ","))));
            styleMap.put("text/sql", new ArrayList<String>(Arrays.asList(StringUtils.split("sql", ","))));
            styleMap.put("text/bat", new ArrayList<String>(Arrays.asList(StringUtils.split("bat", ","))));
            styleMap.put("text/unix", new ArrayList<String>(Arrays.asList(StringUtils.split("sh", ","))));
            styleMap.put("text/xml", new ArrayList<String>(Arrays.asList(StringUtils.split("xml,xsd", ","))));
            globalConfig.setSyntaxStyles(styleMap);
            configManager.saveGlobalConfig(globalConfig);
        }
    }

    private void addSearchPane() {
        SearchPane searchPane = new SearchPane();
        searchPane.setOutFrame(frame);
        searchPane.setConfigManager(configManager);
        TabTitlePanel tabTitlePanel = new TabTitlePanel("新搜索", tabbedPane);
        searchPane.setTabTitlePanel(tabTitlePanel);
        searchPane.afterPropertySet();
        tabbedPane.addTab(null, searchPane);
        tabbedPane.setTabComponentAt(tabbedPane.indexOfComponent(searchPane), tabTitlePanel);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }
}
