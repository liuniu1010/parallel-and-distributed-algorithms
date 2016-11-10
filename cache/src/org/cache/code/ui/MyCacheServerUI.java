package org.cache.code.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import org.cache.code.MyCacheServer;
import org.cache.code.CachedFileRefreshIFC;
import org.cache.code.FileCache;
import org.cache.code.BlockCache;

public class MyCacheServerUI extends JFrame {
    private JButton startServerButton;
    private JButton stopServerButton;
    private JButton clearMessageButton;
    private JTextPane textPane;
    private JRadioButton cacheWithFileButton;
    private JRadioButton cacheWithBlockButton;
    private JLabel cachedFileLabel;
    private JButton clearCacheButton;
    private JList cachedFileList;
    private DefaultListModel cachedFileListModel;

    public MyCacheServerUI() {
        setTitle("Cache Server");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final Toolkit toolkit = getToolkit();
        Dimension size = toolkit.getScreenSize();
        setLocation(size.width/8 - getWidth()/8, size.height/8 - getHeight()/8);
        setSize((int)(size.width/1.5), (int)(size.height/1.5));

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(null);

        // add button
        startServerButton = new JButton("start server");
        startServerButton.setBounds(50, 30, 150, 30);
        startServerButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                // start cache server
                MyCacheServer.startServer();
            }
        });

        stopServerButton = new JButton("stop server");
        stopServerButton.setBounds(230, 30, 150, 30);
        stopServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // stop cache server
                MyCacheServer.stopServer();
            }
        });

        clearMessageButton = new JButton("clear message");
        clearMessageButton.setBounds(420, 80, 150, 30);
        clearMessageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // clear the message;
                textPane.setText("");
            }
        });


        // add text pane
        textPane = new JTextPane();
        JScrollPane messagePane = new JScrollPane(textPane);
        messagePane.setBounds(50, 80, 350, 380);
        MessageFeedback messageFeedback = new MessageFeedback();
        messageFeedback.setTextPane(textPane);
        MyCacheServer.registerMessageFeedback(messageFeedback);

        // add radio button
        cacheWithFileButton = new JRadioButton("cache by file");
        cacheWithFileButton.setSelected(true);
        cacheWithFileButton.setBounds(420, 200, 150, 20);
        cacheWithFileButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == 1) {
                    MyCacheServer.setCachePolicy(MyCacheServer.POLICY_FILE);
                }
            }
        });

        cacheWithBlockButton = new JRadioButton("cache by block");
        cacheWithBlockButton.setSelected(false);
        cacheWithBlockButton.setBounds(420, 230, 150, 20);
        cacheWithBlockButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == 1) {
                    MyCacheServer.setCachePolicy(MyCacheServer.POLICY_BLOCK);
                }
            }
        });

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(cacheWithFileButton);
        buttonGroup.add(cacheWithBlockButton);

        // add the cached list
        cachedFileLabel = new JLabel("cached files");
        cachedFileLabel.setBounds(620, 40, 150, 30);

        cachedFileListModel = new DefaultListModel();
        cachedFileList = new JList(cachedFileListModel);
        JScrollPane cachedFileListPane = new JScrollPane(cachedFileList);
        cachedFileListPane.setBounds(620, 80, 260, 380);

        CachedFileRefreshIFC cachedFileRefresh = new CachedFileRefresh(cachedFileListModel);
        MyCacheServer.registerCachedFileRefresh(cachedFileRefresh);

        clearCacheButton = new JButton("clear cache");
        clearCacheButton.setBounds(730, 30, 150, 30);
        clearCacheButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // clear the cache;
                FileCache.clearCache();
                BlockCache.clearCache();
                MyCacheServer.refreshCachedFile();
            }
        });


        panel.add(startServerButton);
        panel.add(stopServerButton);
        panel.add(clearMessageButton);
        panel.add(messagePane);
//        panel.add(cacheWithFileButton);
//        panel.add(cacheWithBlockButton);
        panel.add(cachedFileLabel);
        panel.add(cachedFileListPane);
        panel.add(clearCacheButton);
    }

    public static void main(String[] args) {
        MyCacheServerUI myCacheServerUI = new MyCacheServerUI();
        myCacheServerUI.setVisible(true);
        if(args.length >= 1) {
            String lastArg = args[args.length - 1];
            if(lastArg.equals("part1")) {
                MyCacheServer.setCachePolicy(MyCacheServer.POLICY_FILE);
            }
            else if(lastArg.equals("part2")) {
                MyCacheServer.setCachePolicy(MyCacheServer.POLICY_BLOCK);
            }
        }
    }
}
