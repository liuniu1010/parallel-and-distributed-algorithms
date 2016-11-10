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
import javax.swing.JList;
import javax.swing.DefaultListModel;
import java.util.List;

import org.cache.code.MyFileServer;
import org.cache.code.util.MyUtil;
import org.cache.code.util.ConfigUtil;

public class MyFileServerUI extends JFrame {
    private JButton startServerButton;
    private JButton stopServerButton;
    private JButton clearMessageButton;
    private JTextPane textPane;
    private DefaultListModel serverFileListModel;
    private JList serverFileList;
    private JButton refreshButton;

    public MyFileServerUI() {
        setTitle("File Server");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final Toolkit toolkit = getToolkit();
        Dimension size = toolkit.getScreenSize();
        setLocation(size.width/4 - getWidth()/4, size.height/4 - getHeight()/4);
        setSize(size.width/2, size.height/2);

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(null);

        // add button
        startServerButton = new JButton("start server");
        startServerButton.setBounds(50, 30, 150, 30);
        startServerButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                // start file server
                MyFileServer.startServer();
            }
        });

        stopServerButton = new JButton("stop server");
        stopServerButton.setBounds(220, 30, 150, 30);
        stopServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // stop file server
                MyFileServer.stopServer();
            }
        });

        clearMessageButton = new JButton("clear message");
        clearMessageButton.setBounds(50, 80, 150, 30);
        clearMessageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // clear the message;
                textPane.setText("");
            }
        });


        // add text pane
        textPane = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBounds(50, 120, 320, 220);
        MessageFeedback messageFeedback = new MessageFeedback();
        messageFeedback.setTextPane(textPane);
        MyFileServer.registerMessageFeedback(messageFeedback);

        // add the file list pane
        serverFileListModel = new DefaultListModel();
        serverFileList = new JList(serverFileListModel);
        JScrollPane serverFileListPane = new JScrollPane(serverFileList);
        serverFileListPane.setBounds(400, 120, 260, 220);

        // add refresh button
        refreshButton = new JButton("refresh");
        refreshButton.setBounds(400, 80, 150, 30);
        refreshButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                serverFileListModel.clear();
                List<String> fileList = MyUtil.getFileList(ConfigUtil.getServerFilePath());
                for(String fileName: fileList) {
                    serverFileListModel.addElement(fileName);
                } 
            }
        });

        panel.add(startServerButton);
        panel.add(stopServerButton);
        panel.add(clearMessageButton);
        panel.add(scrollPane);
        panel.add(serverFileListPane);
        panel.add(refreshButton);
    }

    public static void main(String[] args) {
        MyFileServerUI myFileServerUI = new MyFileServerUI();
        myFileServerUI.setVisible(true);
    }
}
