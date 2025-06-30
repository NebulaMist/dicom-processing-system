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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TCP服务端窗口程序
 * 监听指定端口，接收客户端连接并处理消息
 */
public class TcpServerWindow extends JFrame {
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;
    private JLabel statusLabel;
    
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private Thread serverThread;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final AtomicInteger clientCounter = new AtomicInteger(0);
    
    public TcpServerWindow() {
        initializeComponents();
        setupLayout();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("TCP服务端");
        setSize(600, 500);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        // 端口输入
        portField = new JTextField("8888", 8);
        
        // 控制按钮
        startButton = new JButton("启动服务");
        startButton.addActionListener(new StartServerListener());
        
        stopButton = new JButton("停止服务");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new StopServerListener());
        
        // 状态标签
        statusLabel = new JLabel("服务未启动");
        statusLabel.setForeground(Color.RED);
        
        // 日志显示区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部控制面板
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("服务器控制"));
        topPanel.add(new JLabel("监听端口:"));
        topPanel.add(portField);
        topPanel.add(startButton);
        topPanel.add(stopButton);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(new JLabel("状态:"));
        topPanel.add(statusLabel);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 中央日志面板
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("服务器日志"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        add(logPanel, BorderLayout.CENTER);
        
        // 底部信息面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(new JLabel("提示: 启动服务后，客户端可连接到此服务器进行通信"));
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 启动服务监听器
     */
    private class StartServerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int port = Integer.parseInt(portField.getText().trim());
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException("端口号范围: 1-65535");
                }
                
                startServer(port);
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(TcpServerWindow.this,
                    "请输入有效的端口号 (1-65535)", "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(TcpServerWindow.this,
                    "启动服务失败: " + ex.getMessage(), "启动错误", JOptionPane.ERROR_MESSAGE);
                appendLog("启动服务失败: " + ex.getMessage());
            }
        }
    }
    
    /**
     * 停止服务监听器
     */
    private class StopServerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            stopServer();
        }
    }
    
    /**
     * 启动TCP服务器
     */
    private void startServer(int port) throws IOException {
        if (isRunning) {
            return;
        }
        
        serverSocket = new ServerSocket(port);
        isRunning = true;
        
        // 更新UI状态
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        portField.setEnabled(false);
        statusLabel.setText("服务运行中 - 端口: " + port);
        statusLabel.setForeground(Color.GREEN);
        
        appendLog("=== TCP服务器启动 ===");
        appendLog("监听地址: " + InetAddress.getLocalHost().getHostAddress() + ":" + port);
        appendLog("等待客户端连接...");
        
        // 启动服务器线程
        serverThread = new Thread(this::serverLoop, "ServerThread");
        serverThread.start();
    }
    
    /**
     * 停止TCP服务器
     */
    private void stopServer() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        try {
            // 关闭所有客户端连接
            for (ClientHandler client : clients.values()) {
                client.close();
            }
            clients.clear();
            
            // 关闭服务器Socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // 等待服务器线程结束
            if (serverThread != null && serverThread.isAlive()) {
                serverThread.interrupt();
                serverThread.join(1000);
            }
            
        } catch (Exception e) {
            appendLog("停止服务时出错: " + e.getMessage());
        }
        
        // 更新UI状态
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        portField.setEnabled(true);
        statusLabel.setText("服务已停止");
        statusLabel.setForeground(Color.RED);
        
        appendLog("=== TCP服务器已停止 ===");
    }
    
    /**
     * 服务器主循环
     */
    private void serverLoop() {
        try {
            while (isRunning && !serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    
                    // 为每个客户端创建处理线程
                    String clientId = "Client-" + clientCounter.incrementAndGet();
                    ClientHandler clientHandler = new ClientHandler(clientSocket, clientId);
                    clients.put(clientId, clientHandler);
                    
                    Thread clientThread = new Thread(clientHandler, clientId + "-Thread");
                    clientThread.start();
                    
                    String clientInfo = clientSocket.getRemoteSocketAddress().toString();
                    appendLog("新客户端连接: " + clientId + " (" + clientInfo + ")");
                    
                } catch (SocketException e) {
                    if (isRunning) {
                        appendLog("Socket异常: " + e.getMessage());
                    }
                    break;
                } catch (IOException e) {
                    if (isRunning) {
                        appendLog("接受连接时出错: " + e.getMessage());
                    }
                }
            }
        } finally {
            appendLog("服务器循环结束");
        }
    }
    
    /**
     * 客户端处理器
     */
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final String clientId;
        private BufferedReader reader;
        private PrintWriter writer;
        private volatile boolean running = true;
        
        public ClientHandler(Socket socket, String clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }
        
        @Override
        public void run() {
            try {
                // 创建输入输出流
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                
                String clientInfo = socket.getRemoteSocketAddress().toString();
                appendLog(clientId + " 通信开始 (" + clientInfo + ")");
                
                // 发送欢迎消息
                String welcomeMsg = "欢迎连接到TCP服务器！你的客户端ID是: " + clientId;
                writer.println(welcomeMsg);
                appendLog("发送给 " + clientId + ": " + welcomeMsg);
                
                // 处理客户端消息
                String receivedMessage;
                while (running && (receivedMessage = reader.readLine()) != null) {
                    appendLog("接收自 " + clientId + ": " + receivedMessage);
                    
                    // 生成回复消息
                    String replyMessage = generateReply(receivedMessage);
                    writer.println(replyMessage);
                    appendLog("发送给 " + clientId + ": " + replyMessage);
                    
                    // 如果客户端发送"bye"，断开连接
                    if ("bye".equalsIgnoreCase(receivedMessage.trim())) {
                        break;
                    }
                }
                
            } catch (IOException e) {
                if (running) {
                    appendLog(clientId + " 通信异常: " + e.getMessage());
                }
            } finally {
                close();
                clients.remove(clientId);
                appendLog(clientId + " 连接已断开");
            }
        }
        
        /**
         * 生成回复消息
         */
        private String generateReply(String message) {
            if (message == null || message.trim().isEmpty()) {
                return "服务器收到空消息";
            }
            
            message = message.trim();
            
            if ("ping".equalsIgnoreCase(message)) {
                return "pong";
            } else if ("time".equalsIgnoreCase(message)) {
                return "当前服务器时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            } else if ("bye".equalsIgnoreCase(message)) {
                return "再见！连接即将关闭。";
            } else if (message.toLowerCase().contains("hello")) {
                return "Hello! 很高兴收到你的问候！";
            } else {
                return "服务器回复: 已收到你的消息 \"" + message + "\" (消息长度: " + message.length() + " 字符)";
            }
        }
        
        /**
         * 关闭客户端连接
         */
        public void close() {
            running = false;
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                appendLog(clientId + " 关闭连接时出错: " + e.getMessage());
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
            stopServer();
        }
        super.processWindowEvent(e);
    }
    
    public static void main(String[] args) {        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 使用默认外观
            }
            new TcpServerWindow().setVisible(true);
        });
    }
}
