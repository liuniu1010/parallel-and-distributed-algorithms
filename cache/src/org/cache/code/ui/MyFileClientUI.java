package org.cache.code.ui;

import java.util.List;
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
import javax.swing.ListSelectionModel;
import org.cache.code.MyFileClient;
import org.cache.code.exception.MyException;
import org.cache.code.util.ConfigUtil;
import java.io.IOException;
import java.io.File;


public class MyFileClientUI extends JFrame {
    private JButton refreshButton;
    private JButton downloadButton;
    private JList serverFileList;
    private DefaultListModel serverFileListModel;
    private JList clientFileList;
    private DefaultListModel clientFileListModel;
    private JButton showContentButton;
    private JTextPane contentPane;

    public MyFileClientUI() {
        setTitle("File Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final Toolkit toolkit = getToolkit();
        Dimension size = toolkit.getScreenSize();
        setLocation(size.width/8 - getWidth()/8, size.height/8 - getHeight()/8);
        setSize((int)(size.width/1.5), (int)(size.height/1.5));

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(null);

        // add button
        refreshButton = new JButton("refresh");
        refreshButton.setBounds(50, 30, 150, 30);
        refreshButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                try {
                    List<String> fileList = MyFileClient.getInstance().getFileList();
                    serverFileListModel.clear();
                    if(fileList != null && fileList.size() > 0) {
                        for(String fileName: fileList) {
                            serverFileListModel.addElement(fileName);
                        }
                    }
                }
                catch(MyException me) {
                    me.printStackTrace();
                }
                catch(IOException ioe) {
                    ioe.printStackTrace();
                }
                catch(ClassNotFoundException cne) {
                    cne.printStackTrace();
                }
            }
        });

        downloadButton = new JButton("download");
        downloadButton.setBounds(280, 30, 150, 30);
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    int selectedIndex = serverFileList.getSelectedIndex();
                    if(selectedIndex != -1) {
                        String fileName = (String)serverFileListModel.getElementAt(selectedIndex);
                        fileName = MyFileClient.getInstance().getFile(fileName);
                        if(!clientFileListModel.contains(fileName)) {
                            clientFileListModel.addElement(fileName);
                        }
                    }
                }
                catch(MyException me) {
                    me.printStackTrace();
                }
                catch(IOException ioe) {
                    ioe.printStackTrace();
                }
                catch(ClassNotFoundException cne) {
                    cne.printStackTrace();
                }
            }
        });

        // add JList
        serverFileListModel = new DefaultListModel();
        serverFileList = new JList(serverFileListModel);
        serverFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane serverFileListPane = new JScrollPane(serverFileList);
        serverFileListPane.setBounds(50, 80, 200, 380);
        
        clientFileListModel = new DefaultListModel();
        clientFileList = new JList(clientFileListModel);
        clientFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane clientFileListPane = new JScrollPane(clientFileList);
        clientFileListPane.setBounds(280, 80, 200, 380);

        // show content button
        showContentButton = new JButton("show file content");
        showContentButton.setBounds(530, 30, 180, 30);
        showContentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int selectedIndex = clientFileList.getSelectedIndex();
                if(selectedIndex != -1) {
                    String fileName = (String)clientFileListModel.getElementAt(selectedIndex);
                    String fileNameWithPath = ConfigUtil.getClientFilePath() + File.separator + fileName;
                    List<String> lineContent = ConfigUtil.getTextFileContent(fileNameWithPath);
                    String fileContent = null;
                    for(String line: lineContent) {
                        if(fileContent == null) {
                            fileContent = line;
                        }
                        else {
                            fileContent += "\n" + line;
                        }
                    }
                    contentPane.setText(fileContent);
                }
            }
        });

        // add text pane
        contentPane = new JTextPane();
        JScrollPane scrollContentPane = new JScrollPane(contentPane);
        scrollContentPane.setBounds(530, 80, 350, 380);

        panel.add(refreshButton);
        panel.add(downloadButton);
        panel.add(serverFileListPane);
        panel.add(clientFileListPane);
        panel.add(showContentButton);
        panel.add(scrollContentPane);
    }

    public static void main(String[] args) {
        MyFileClientUI myFileClientUI = new MyFileClientUI();
        myFileClientUI.setVisible(true);
    }
}
