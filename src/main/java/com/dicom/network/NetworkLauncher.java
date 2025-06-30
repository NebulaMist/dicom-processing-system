package com.dicom.network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 网络通信程序启动器
 * 提供统一的入口启动不同的网络通信程序
 */
public class NetworkLauncher extends JFrame {
    
    public NetworkLauncher() {
        initializeComponents();
        setupLayout();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Socket通信程序启动器");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private void initializeComponents() {
        // 设置窗口图标（如果有的话）
        // setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(64, 128, 255));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Socket通信程序集", JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // 主要内容面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // TCP服务端按钮
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JButton serverButton = createLaunchButton(
            "TCP服务端程序", 
            "启动TCP服务端，监听指定端口等待客户端连接",
            new Color(76, 175, 80),
            () -> new TcpServerWindow().setVisible(true)
        );
        mainPanel.add(serverButton, gbc);
        
        // TCP客户端按钮
        gbc.gridy = 1;
        JButton clientButton = createLaunchButton(
            "TCP客户端程序", 
            "启动TCP客户端，连接到指定的服务端进行通信",
            new Color(33, 150, 243),
            () -> new TcpClientWindow().setVisible(true)
        );
        mainPanel.add(clientButton, gbc);
        
        // 简易聊天程序按钮
        gbc.gridy = 2;
        JButton chatButton = createLaunchButton(
            "简易聊天程序", 
            "启动聊天程序，支持点对点实时聊天通信",
            new Color(156, 39, 176),
            () -> new SimpleChatWindow().setVisible(true)
        );
        mainPanel.add(chatButton, gbc);
        
        // 全部启动按钮
        gbc.gridy = 3;
        JButton allButton = createLaunchButton(
            "启动全部程序", 
            "同时启动服务端、客户端和聊天程序进行测试",
            new Color(255, 152, 0),
            this::launchAllPrograms
        );
        mainPanel.add(allButton, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 底部信息面板
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        bottomPanel.setBackground(new Color(245, 245, 245));
        
        JLabel infoLabel = new JLabel(
            "<html><center>" +
            "<b>实验八：Socket通信程序设计</b><br>" +
            "包含TCP服务端、客户端和简易聊天程序<br>" +
            "支持多线程并发通信和实时消息传输" +
            "</center></html>", 
            JLabel.CENTER
        );
        infoLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        infoLabel.setForeground(Color.DARK_GRAY);
        
        bottomPanel.add(infoLabel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建启动按钮
     */
    private JButton createLaunchButton(String title, String description, Color color, Runnable action) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // 标题标签
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        // 描述标签
        JLabel descLabel = new JLabel(description, JLabel.CENTER);
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        descLabel.setForeground(Color.WHITE);
        
        button.add(titleLabel, BorderLayout.CENTER);
        button.add(descLabel, BorderLayout.SOUTH);
        
        // 鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        button.addActionListener(e -> action.run());
        
        return button;
    }
    
    /**
     * 启动所有程序
     */
    private void launchAllPrograms() {
        // 确认对话框
        int result = JOptionPane.showConfirmDialog(
            this,
            "这将启动TCP服务端、客户端和聊天程序三个窗口。\n" +
            "建议的测试步骤：\n" +
            "1. 先启动TCP服务端（端口8888）\n" +
            "2. 再启动TCP客户端连接到服务端\n" +
            "3. 启动两个聊天程序实例进行聊天测试\n\n" +
            "是否继续？",
            "启动确认",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // 延迟启动，避免窗口重叠
            Timer timer = new Timer(500, null);
            timer.addActionListener(new ActionListener() {
                private int step = 0;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    switch (step) {
                        case 0:
                            new TcpServerWindow().setVisible(true);
                            break;
                        case 1:
                            new TcpClientWindow().setVisible(true);
                            break;
                        case 2:
                            new SimpleChatWindow().setVisible(true);
                            timer.stop();
                            
                            // 显示使用提示
                            Timer hintTimer = new Timer(2000, evt -> {
                                JOptionPane.showMessageDialog(
                                    NetworkLauncher.this,
                                    "所有程序已启动完成！\n\n" +
                                    "测试建议：\n" +
                                    "• TCP程序：先在服务端启动监听，再用客户端连接\n" +
                                    "• 聊天程序：可以启动多个实例进行点对点聊天\n" +
                                    "• 支持的命令：ping, time, hello, bye等",
                                    "启动完成",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                                ((Timer)evt.getSource()).stop();
                            });
                            hintTimer.setRepeats(false);
                            hintTimer.start();
                            break;
                    }
                    step++;
                }
            });
            timer.start();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 使用默认外观
            }
            
            new NetworkLauncher().setVisible(true);
        });
    }
}
