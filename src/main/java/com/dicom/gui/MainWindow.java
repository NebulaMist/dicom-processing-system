package com.dicom.gui;

import com.dicom.data.DCMFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * DICOM医学图像处理系统主窗口
 * 提供文件选择和解析功能，然后启动ImageFrame显示图像
 */
public class MainWindow extends JFrame {
    private JLabel statusLabel;
    private JButton selectFileButton;
    private JButton parseButton;
    private JTextArea fileInfoArea;
    private File selectedFile;
    private DCMFile parsedDCMFile;
    
    public MainWindow() {
        initializeComponents();
        setupLayout();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("DICOM医学图像处理系统");
        setSize(600, 400);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        // 状态标签
        statusLabel = new JLabel("请选择DICOM文件");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        // 选择文件按钮
        selectFileButton = new JButton("选择DICOM文件");
        selectFileButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        selectFileButton.addActionListener(new SelectFileListener());
        
        // 解析按钮
        parseButton = new JButton("解析并显示");
        parseButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        parseButton.setEnabled(false);
        parseButton.addActionListener(new ParseButtonListener());
        
        // 文件信息显示区域
        fileInfoArea = new JTextArea(10, 40);
        fileInfoArea.setEditable(false);
        fileInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        fileInfoArea.setBackground(getBackground());
        fileInfoArea.setText("请选择DICOM文件来查看文件信息...");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部面板 - 状态和按钮
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        topPanel.add(statusLabel, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(selectFileButton);
        buttonPanel.add(parseButton);
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 中央面板 - 文件信息
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("文件信息"));
        centerPanel.add(new JScrollPane(fileInfoArea), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        
        // 底部面板 - 帮助信息
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        JLabel helpLabel = new JLabel("选择DICOM文件 → 点击解析并显示 → 在新窗口中查看图像和元数据");
        helpLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        helpLabel.setForeground(Color.GRAY);
        bottomPanel.add(helpLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 文件选择监听器
     */
    private class SelectFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || 
                           f.getName().toLowerCase().endsWith(".dcm") ||
                           f.getName().toLowerCase().endsWith(".dicom");
                }
                
                @Override
                public String getDescription() {
                    return "DICOM文件 (*.dcm, *.dicom)";
                }
            });
            
            // 设置默认目录为项目根目录
            fileChooser.setCurrentDirectory(new File("e:\\桌面\\垃圾测试TDD - 副本"));
            
            if (fileChooser.showOpenDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                statusLabel.setText("已选择文件: " + selectedFile.getName());
                
                // 显示文件基本信息
                displayFileBasicInfo();
                
                // 启用解析按钮
                parseButton.setEnabled(true);
            }
        }
    }
    
    /**
     * 解析按钮监听器
     */
    private class ParseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(MainWindow.this, 
                    "请先选择DICOM文件", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 显示进度
            statusLabel.setText("正在解析DICOM文件...");
            parseButton.setEnabled(false);
            
            // 在后台线程中解析文件
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    try {
                        parsedDCMFile = new DCMFile(selectedFile.getAbsolutePath());
                        return parsedDCMFile.Parse();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return false;
                    }
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            statusLabel.setText("解析成功! 即将打开图像查看器...");
                            
                            // 显示解析后的详细信息
                            displayParsedInfo();
                            
                            // 创建并显示ImageFrame，传入解析后的DCMFile对象
                            SwingUtilities.invokeLater(() -> {
                                ImageFrame imageFrame = new ImageFrame(parsedDCMFile);
                                imageFrame.setVisible(true);
                            });
                            
                        } else {
                            statusLabel.setText("解析失败");
                            JOptionPane.showMessageDialog(MainWindow.this, 
                                "DICOM文件解析失败，请检查文件格式", 
                                "解析错误", 
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        statusLabel.setText("解析出错");
                        JOptionPane.showMessageDialog(MainWindow.this, 
                            "解析过程中出现错误: " + ex.getMessage(), 
                            "错误", 
                            JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                    parseButton.setEnabled(true);
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * 显示文件基本信息
     */
    private void displayFileBasicInfo() {
        if (selectedFile == null) return;
        
        StringBuilder info = new StringBuilder();
        info.append("=== 文件基本信息 ===\n");
        info.append("文件名: ").append(selectedFile.getName()).append("\n");
        info.append("文件路径: ").append(selectedFile.getAbsolutePath()).append("\n");
        info.append("文件大小: ").append(selectedFile.length()).append(" 字节\n");
        info.append("文件存在: ").append(selectedFile.exists() ? "是" : "否").append("\n");
        info.append("可读取: ").append(selectedFile.canRead() ? "是" : "否").append("\n");
        info.append("\n点击 '解析并显示' 按钮来解析DICOM文件...");
        
        fileInfoArea.setText(info.toString());
    }
    
    /**
     * 显示解析后的详细信息
     */
    private void displayParsedInfo() {
        if (parsedDCMFile == null) return;
        
        StringBuilder info = new StringBuilder();
        info.append("=== DICOM解析结果 ===\n");
        info.append("患者姓名: ").append(parsedDCMFile.getPatientName()).append("\n");
        info.append("患者ID: ").append(parsedDCMFile.getPatientID()).append("\n");
        info.append("检查日期: ").append(parsedDCMFile.getStudyDate()).append("\n");
        info.append("检查模态: ").append(parsedDCMFile.getModality()).append("\n");
        info.append("图像尺寸: ").append(parsedDCMFile.getRows()).append(" x ").append(parsedDCMFile.getColumns()).append("\n");
        
        // 检查像素数据
        Object pixelData = parsedDCMFile.GetValue(0x7FE00010); // PixelData
        if (pixelData != null) {
            info.append("像素数据: 存在\n");
            info.append("像素数据类型: ").append(pixelData.getClass().getSimpleName()).append("\n");
        } else {
            info.append("像素数据: 未找到\n");
        }
        
        info.append("数据元素总数: ").append(parsedDCMFile.getItemCount()).append("\n");
        info.append("\n图像查看器即将打开...");
        
        fileInfoArea.setText(info.toString());
    }
      public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 使用默认外观
            }
            
            new MainWindow().setVisible(true);
        });
    }
}
