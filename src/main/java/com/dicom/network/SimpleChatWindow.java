package com.dicom.network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易网上聊天程序
 * 既可以作为服务端（监听连接），也可以作为客户端（连接到其他用户）
 * 支持一对一聊天和简单的群聊功能
 */
public class SimpleChatWindow extends JFrame {
    // UI组件
    private JTextField nicknameField;
    private JTextField portField;
    private JTextField targetHostField;
    private JTextField targetPortField;
    private JButton startServerButton;
    private JButton connectButton;
    private JButton disconnectButton;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel statusLabel;
    
    // 网络相关
    private String nickname = "用户";
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private boolean isServerMode = false;
    private boolean isConnected = false;
    private Thread serverThread;
    private Thread receiveThread;
    private final ConcurrentHashMap<String, ChatConnection> connections = new ConcurrentHashMap<>();
    
    // 通信流
    private BufferedReader reader;
    private PrintWriter writer;
    
    public SimpleChatWindow() {
        initializeComponents();
        setupLayout();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("简易聊天程序");
        setSize(700, 600);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        // 用户配置
        nicknameField = new JTextField("用户" + System.currentTimeMillis() % 1000, 10);
        
        // 服务端配置
        portField = new JTextField("9999", 8);
        startServerButton = new JButton("启动服务端");
        startServerButton.addActionListener(new StartServerListener());
        
        // 客户端配置
        targetHostField = new JTextField("localhost", 12);
        targetPortField = new JTextField("9999", 8);
        connectButton = new JButton("连接");
        connectButton.addActionListener(new ConnectListener());
        
        // 连接控制
        disconnectButton = new JButton("断开连接");
        disconnectButton.setEnabled(false);
        disconnectButton.addActionListener(new DisconnectListener());
        
        // 状态显示
        statusLabel = new JLabel("未连接");
        statusLabel.setForeground(Color.RED);
        
        // 聊天显示区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        chatArea.setBackground(new Color(248, 248, 255));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        
        // 消息输入和发送
        messageField = new JTextField();
        messageField.setEnabled(false);
        messageField.addActionListener(new SendListener());
        
        sendButton = new JButton("发送");
        sendButton.setEnabled(false);
        sendButton.addActionListener(new SendListener());
        
        // 添加系统消息
        appendChatMessage("系统", "欢迎使用简易聊天程序！", Color.BLUE);
        appendChatMessage("系统", "你可以启动服务端等待连接，或者连接到其他用户", Color.BLUE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部配置面板
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("聊天配置"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        
        // 昵称配置
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("昵称:"), gbc);
        gbc.gridx = 1;
        topPanel.add(nicknameField, gbc);
        
        // 状态显示
        gbc.gridx = 2; gbc.gridy = 0;
        topPanel.add(new JLabel("状态:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 2;
        topPanel.add(statusLabel, gbc);
        
        // 服务端配置
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        topPanel.add(new JLabel("服务端口:"), gbc);
        gbc.gridx = 1;
        topPanel.add(portField, gbc);
        gbc.gridx = 2;
        topPanel.add(startServerButton, gbc);
        
        // 客户端配置
        gbc.gridx = 0; gbc.gridy = 2;
        topPanel.add(new JLabel("目标主机:"), gbc);
        gbc.gridx = 1;
        topPanel.add(targetHostField, gbc);
        gbc.gridx = 2;
        topPanel.add(new JLabel("端口:"), gbc);
        gbc.gridx = 3;
        topPanel.add(targetPortField, gbc);
        gbc.gridx = 4;
        topPanel.add(connectButton, gbc);
        
        // 断开连接按钮
        gbc.gridx = 2; gbc.gridy = 3; gbc.gridwidth = 3;
        topPanel.add(disconnectButton, gbc);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 中央聊天显示面板
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("聊天记录"));
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(chatPanel, BorderLayout.CENTER);
        
        // 底部消息输入面板
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("消息输入"));
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("消息: "), BorderLayout.WEST);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        
        JLabel hintLabel = new JLabel("提示: 按回车键快速发送消息");
        hintLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        hintLabel.setForeground(Color.GRAY);
        bottomPanel.add(hintLabel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 启动服务端监听器
     */
    private class StartServerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                nickname = nicknameField.getText().trim();
                if (nickname.isEmpty()) {
                    nickname = "服务端用户";
                }
                
                int port = Integer.parseInt(portField.getText().trim());
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException("端口号范围: 1-65535");
                }
                
                startServer(port);
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(SimpleChatWindow.this,
                    "请输入有效的端口号 (1-65535)", "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(SimpleChatWindow.this,
                    "启动服务端失败: " + ex.getMessage(), "启动错误", JOptionPane.ERROR_MESSAGE);
                appendChatMessage("系统", "启动服务端失败: " + ex.getMessage(), Color.RED);
            }
        }
    }
    
    /**
     * 连接监听器
     */
    private class ConnectListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                nickname = nicknameField.getText().trim();
                if (nickname.isEmpty()) {
                    nickname = "客户端用户";
                }
                
                String host = targetHostField.getText().trim();
                int port = Integer.parseInt(targetPortField.getText().trim());
                
                if (host.isEmpty()) {
                    throw new IllegalArgumentException("请输入目标主机地址");
                }
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException("端口号范围: 1-65535");
                }
                
                connectToServer(host, port);
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(SimpleChatWindow.this,
                    "请输入有效的端口号 (1-65535)", "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(SimpleChatWindow.this,
                    "连接失败: " + ex.getMessage(), "连接错误", JOptionPane.ERROR_MESSAGE);
                appendChatMessage("系统", "连接失败: " + ex.getMessage(), Color.RED);
            }
        }
    }
    
    /**
     * 断开连接监听器
     */
    private class DisconnectListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            disconnect();
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
     * 启动服务端模式
     */
    private void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        isServerMode = true;
        
        // 更新UI状态
        updateUIForServerMode(true);
        
        appendChatMessage("系统", "聊天服务端已启动，监听端口: " + port, Color.GREEN);
        appendChatMessage("系统", "等待其他用户连接...", Color.BLUE);
        
        // 启动服务端线程
        serverThread = new Thread(this::serverLoop, "ServerThread");
        serverThread.start();
    }
    
    /**
     * 连接到服务端
     */
    private void connectToServer(String host, int port) throws IOException {
        clientSocket = new Socket();
        clientSocket.connect(new InetSocketAddress(host, port), 5000);
        
        setupCommunication(clientSocket);
        isConnected = true;
        
        // 更新UI状态
        updateUIForClientMode(true);
        
        appendChatMessage("系统", "已连接到 " + host + ":" + port, Color.GREEN);
        
        // 发送加入消息
        sendChatMessage("*** " + nickname + " 加入了聊天 ***");
        
        // 启动接收线程
        startReceiveThread();
    }
    
    /**
     * 服务端主循环
     */
    private void serverLoop() {
        try {
            while (isServerMode && !serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    
                    if (isConnected) {
                        // 如果已有连接，拒绝新连接
                        try {
                            PrintWriter tempWriter = new PrintWriter(
                                new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
                            tempWriter.println("服务器忙碌，请稍后再试");
                            clientSocket.close();
                        } catch (Exception ignored) {}
                        continue;
                    }
                    
                    setupCommunication(clientSocket);
                    this.clientSocket = clientSocket;
                    isConnected = true;
                    
                    SwingUtilities.invokeLater(() -> {
                        String clientInfo = clientSocket.getRemoteSocketAddress().toString();
                        appendChatMessage("系统", "用户已连接: " + clientInfo, Color.GREEN);
                        updateUIForServerMode(true);
                    });
                    
                    // 启动接收线程
                    startReceiveThread();
                    
                } catch (SocketException e) {
                    if (isServerMode) {
                        SwingUtilities.invokeLater(() -> 
                            appendChatMessage("系统", "服务端异常: " + e.getMessage(), Color.RED));
                    }
                    break;
                } catch (IOException e) {
                    if (isServerMode) {
                        SwingUtilities.invokeLater(() -> 
                            appendChatMessage("系统", "接受连接时出错: " + e.getMessage(), Color.RED));
                    }
                }
            }
        } finally {
            SwingUtilities.invokeLater(() -> appendChatMessage("系统", "服务端循环结束", Color.GRAY));
        }
    }
    
    /**
     * 设置通信流
     */
    private void setupCommunication(Socket socket) throws IOException {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
    }
    
    /**
     * 启动接收消息线程
     */
    private void startReceiveThread() {
        receiveThread = new Thread(() -> {
            try {
                String receivedMessage;
                while (isConnected && (receivedMessage = reader.readLine()) != null) {
                    final String message = receivedMessage;
                    SwingUtilities.invokeLater(() -> processChatMessage(message));
                }
            } catch (IOException e) {
                if (isConnected) {
                    SwingUtilities.invokeLater(() -> {
                        appendChatMessage("系统", "接收消息时出错: " + e.getMessage(), Color.RED);
                        disconnect();
                    });
                }
            }
        }, "ReceiveThread");
        receiveThread.start();
    }
    
    /**
     * 处理接收到的聊天消息
     */
    private void processChatMessage(String message) {
        if (message.startsWith("USER:")) {
            // 用户消息格式: USER:昵称:消息内容
            String[] parts = message.split(":", 3);
            if (parts.length >= 3) {
                String senderNickname = parts[1];
                String content = parts[2];
                appendChatMessage(senderNickname, content, Color.BLACK);
            } else {
                appendChatMessage("未知用户", message, Color.GRAY);
            }
        } else {
            // 系统消息或其他格式
            appendChatMessage("对方", message, Color.DARK_GRAY);
        }
    }
    
    /**
     * 发送聊天消息
     */
    private void sendMessage() {
        if (!isConnected || writer == null) {
            JOptionPane.showMessageDialog(this, "未建立连接", "发送失败", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            messageField.requestFocus();
            return;
        }
        
        // 发送格式化的用户消息
        sendChatMessage("USER:" + nickname + ":" + message);
        
        // 在本地显示
        appendChatMessage(nickname + " (我)", message, new Color(0, 100, 0));
        
        messageField.setText("");
        messageField.requestFocus();
    }
    
    /**
     * 发送原始消息
     */
    private void sendChatMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }
    
    /**
     * 断开连接
     */
    private void disconnect() {
        if (isConnected) {
            // 发送离开消息
            sendChatMessage("*** " + nickname + " 离开了聊天 ***");
        }
        
        isConnected = false;
        isServerMode = false;
        
        try {
            // 关闭通信流
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            
            // 等待线程结束
            if (receiveThread != null && receiveThread.isAlive()) {
                receiveThread.interrupt();
                receiveThread.join(1000);
            }
            if (serverThread != null && serverThread.isAlive()) {
                serverThread.interrupt();
                serverThread.join(1000);
            }
            
        } catch (Exception e) {
            appendChatMessage("系统", "断开连接时出错: " + e.getMessage(), Color.RED);
        }
        
        // 更新UI状态
        updateUIForDisconnected();
        
        appendChatMessage("系统", "已断开连接", Color.GRAY);
    }
    
    /**
     * 添加聊天消息
     */
    private void appendChatMessage(String sender, String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            
            // 创建格式化的消息
            String formattedMessage = String.format("[%s] %s: %s\n", timestamp, sender, message);
            
            chatArea.append(formattedMessage);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    /**
     * 更新UI状态 - 服务端模式
     */
    private void updateUIForServerMode(boolean connected) {
        startServerButton.setEnabled(false);
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(true);
        nicknameField.setEnabled(false);
        portField.setEnabled(false);
        targetHostField.setEnabled(false);
        targetPortField.setEnabled(false);
        
        if (connected) {
            messageField.setEnabled(true);
            sendButton.setEnabled(true);
            statusLabel.setText("服务端模式 - 已连接");
            statusLabel.setForeground(Color.GREEN);
        } else {
            statusLabel.setText("服务端模式 - 等待连接");
            statusLabel.setForeground(Color.ORANGE);
        }
    }
    
    /**
     * 更新UI状态 - 客户端模式
     */
    private void updateUIForClientMode(boolean connected) {
        startServerButton.setEnabled(false);
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(true);
        nicknameField.setEnabled(false);
        portField.setEnabled(false);
        targetHostField.setEnabled(false);
        targetPortField.setEnabled(false);
        messageField.setEnabled(true);
        sendButton.setEnabled(true);
        
        statusLabel.setText("客户端模式 - 已连接");
        statusLabel.setForeground(Color.GREEN);
    }
    
    /**
     * 更新UI状态 - 断开连接
     */
    private void updateUIForDisconnected() {
        startServerButton.setEnabled(true);
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        nicknameField.setEnabled(true);
        portField.setEnabled(true);
        targetHostField.setEnabled(true);
        targetPortField.setEnabled(true);
        messageField.setEnabled(false);
        sendButton.setEnabled(false);
        
        statusLabel.setText("未连接");
        statusLabel.setForeground(Color.RED);
    }
    
    /**
     * 窗口关闭时的清理工作
     */
    @Override
    protected void processWindowEvent(java.awt.event.WindowEvent e) {
        if (e.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING) {
            disconnect();
        }
        super.processWindowEvent(e);
    }
    
    /**
     * 聊天连接类（为将来扩展群聊功能保留）
     */
    private static class ChatConnection {
        private final Socket socket;
        private final String nickname;
        private final BufferedReader reader;
        private final PrintWriter writer;
        
        public ChatConnection(Socket socket, String nickname) throws IOException {
            this.socket = socket;
            this.nickname = nickname;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        }
        
        public void sendMessage(String message) {
            writer.println(message);
        }
        
        public void close() throws IOException {
            reader.close();
            writer.close();
            socket.close();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 使用默认外观
            }
            new SimpleChatWindow().setVisible(true);
        });
    }
}
