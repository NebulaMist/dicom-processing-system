package com.dicom.network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TCP客户端窗口程序
 * 连接到指定服务器，发送消息并接收回复
 */
public class TcpClientWindow extends JFrame {
    private JTextField serverField;
    private JTextField portField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JTextArea logArea;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel statusLabel;
    
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean isConnected = false;
    private Thread receiveThread;
    
    public TcpClientWindow() {
        initializeComponents();
        setupLayout();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("TCP客户端");
        setSize(600, 500);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        // 服务器连接配置
        serverField = new JTextField("localhost", 12);
        portField = new JTextField("8888", 8);
        
        // 连接控制按钮
        connectButton = new JButton("连接");
        connectButton.addActionListener(new ConnectListener());
        
        disconnectButton = new JButton("断开");
        disconnectButton.setEnabled(false);
        disconnectButton.addActionListener(new DisconnectListener());
        
        // 状态标签
        statusLabel = new JLabel("未连接");
        statusLabel.setForeground(Color.RED);
        
        // 通信日志显示区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.CYAN);
        
        // 消息输入和发送
        messageField = new JTextField();
        messageField.setEnabled(false);
        messageField.addActionListener(new SendListener()); // 回车发送
        
        sendButton = new JButton("发送");
        sendButton.setEnabled(false);
        sendButton.addActionListener(new SendListener());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部连接面板
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("服务器连接"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("服务器地址:"), gbc);
        gbc.gridx = 1;
        topPanel.add(serverField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        topPanel.add(new JLabel("端口:"), gbc);
        gbc.gridx = 3;
        topPanel.add(portField, gbc);
        
        gbc.gridx = 4; gbc.gridy = 0;
        topPanel.add(connectButton, gbc);
        gbc.gridx = 5;
        topPanel.add(disconnectButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(new JLabel("状态:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        topPanel.add(statusLabel, gbc);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 中央通信日志面板
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("通信日志"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        add(logPanel, BorderLayout.CENTER);
        
        // 底部消息发送面板
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("消息发送"));
        
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(new JLabel("消息内容: "), BorderLayout.WEST);
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        
        bottomPanel.add(messagePanel, BorderLayout.CENTER);
        
        JLabel hintLabel = new JLabel("提示: 可以发送 'ping'、'time'、'hello'、'bye' 等命令");
        hintLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        hintLabel.setForeground(Color.GRAY);
        bottomPanel.add(hintLabel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 连接服务器监听器
     */
    private class ConnectListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String server = serverField.getText().trim();
                int port = Integer.parseInt(portField.getText().trim());
                
                if (server.isEmpty()) {
                    throw new IllegalArgumentException("请输入服务器地址");
                }
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException("端口号范围: 1-65535");
                }
                
                connectToServer(server, port);
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(TcpClientWindow.this,
                    "请输入有效的端口号 (1-65535)", "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(TcpClientWindow.this,
                    "连接失败: " + ex.getMessage(), "连接错误", JOptionPane.ERROR_MESSAGE);
                appendLog("连接失败: " + ex.getMessage());
            }
        }
    }
    
    /**
     * 断开连接监听器
     */
    private class DisconnectListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            disconnectFromServer();
        }
    }
    
    /**
     * 发送消息监听器
     */
    private class SendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sendMessage();
        }
    }
    
    /**
     * 连接到TCP服务器
     */
    private void connectToServer(String server, int port) throws IOException {
        if (isConnected) {
            return;
        }
        
        appendLog("=== 正在连接到服务器 ===");
        appendLog("目标地址: " + server + ":" + port);
        
        // 创建Socket连接
        socket = new Socket();
        socket.connect(new InetSocketAddress(server, port), 5000); // 5秒超时
        
        // 创建输入输出流
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        
        isConnected = true;
        
        // 更新UI状态
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(true);
        serverField.setEnabled(false);
        portField.setEnabled(false);
        messageField.setEnabled(true);
        sendButton.setEnabled(true);
        statusLabel.setText("已连接到 " + server + ":" + port);
        statusLabel.setForeground(Color.GREEN);
        
        appendLog("连接成功！");
        
        // 启动接收线程
        receiveThread = new Thread(this::receiveLoop, "ReceiveThread");
        receiveThread.start();
        
        // 设置焦点到消息输入框
        messageField.requestFocus();
    }
    
    /**
     * 断开服务器连接
     */
    private void disconnectFromServer() {
        if (!isConnected) {
            return;
        }
        
        isConnected = false;
        
        try {
            // 发送断开消息
            if (writer != null) {
                writer.println("bye");
                appendLog("发送断开请求...");
            }
            
            // 关闭连接
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
            
            // 等待接收线程结束
            if (receiveThread != null && receiveThread.isAlive()) {
                receiveThread.interrupt();
                receiveThread.join(1000);
            }
            
        } catch (Exception e) {
            appendLog("断开连接时出错: " + e.getMessage());
        }
        
        // 更新UI状态
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        serverField.setEnabled(true);
        portField.setEnabled(true);
        messageField.setEnabled(false);
        sendButton.setEnabled(false);
        statusLabel.setText("已断开连接");
        statusLabel.setForeground(Color.RED);
        
        appendLog("=== 连接已断开 ===");
    }
    
    /**
     * 发送消息到服务器
     */
    private void sendMessage() {
        if (!isConnected || writer == null) {
            JOptionPane.showMessageDialog(this, "未连接到服务器", "发送失败", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入要发送的消息", "输入提示", JOptionPane.INFORMATION_MESSAGE);
            messageField.requestFocus();
            return;
        }
        
        try {
            writer.println(message);
            appendLog("发送: " + message);
            messageField.setText("");
            messageField.requestFocus();
            
            // 如果发送了bye命令，准备断开连接
            if ("bye".equalsIgnoreCase(message)) {
                SwingUtilities.invokeLater(() -> {
                    Timer timer = new Timer(1000, e -> disconnectFromServer());
                    timer.setRepeats(false);
                    timer.start();
                });
            }
            
        } catch (Exception e) {
            appendLog("发送消息失败: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "发送消息失败: " + e.getMessage(), "发送错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 接收消息循环
     */
    private void receiveLoop() {
        try {
            String receivedMessage;
            while (isConnected && (receivedMessage = reader.readLine()) != null) {
                final String message = receivedMessage;
                SwingUtilities.invokeLater(() -> appendLog("接收: " + message));
            }
        } catch (IOException e) {
            if (isConnected) {
                SwingUtilities.invokeLater(() -> {
                    appendLog("接收消息时出错: " + e.getMessage());
                    disconnectFromServer();
                });
            }
        }
    }
    
    /**
     * 添加日志信息
     */
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * 窗口关闭时的清理工作
     */
    @Override
    protected void processWindowEvent(java.awt.event.WindowEvent e) {
        if (e.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING) {
            disconnectFromServer();
        }
        super.processWindowEvent(e);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 使用默认外观
            }
            new TcpClientWindow().setVisible(true);
        });
    }
}
