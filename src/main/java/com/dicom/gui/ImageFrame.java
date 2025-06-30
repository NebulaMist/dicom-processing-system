package com.dicom.gui;

import com.dicom.data.DCMFile;
import com.dicom.data.DCMDataElement;
import com.dicom.dictionary.DicomTags;
import com.dicom.application.ThreadTester;
import com.dicom.application.CustomMouseListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * ImageFrame类用于显示DICOM医学图像
 * 提供文件加载、图像显示和元数据查看功能
 */
public class ImageFrame extends JFrame {
    private JPanel imagePanel;
    private JTextArea metadataArea;
    private JLabel imageLabel;
    private DCMFile dcmFile;
    private BufferedImage currentImage;
    
    // 窗宽窗位控制
    private JSlider windowCenterSlider;
    private JSlider windowWidthSlider;
    private int defaultWindowCenter = 128;
    private int defaultWindowWidth = 256;
    
    // 新增字段：窗宽窗位的文本框和线程控制
    public JTextField textWindow;  // 窗宽文本框，设为public以供鼠标监听器访问
    public JTextField textCenter;  // 窗位文本框，设为public以供鼠标监听器访问
    public Integer window = 256;   // 窗宽值，设为public以供鼠标监听器访问
    public Integer center = 128;   // 窗位值，设为public以供鼠标监听器访问
      private JButton startThreadButton;  // 启动线程按钮
    private JTextArea threadOutputArea; // 线程输出文本区域
    private ThreadTester thread1;       // 线程1
    private ThreadTester thread2;       // 线程2
    private Thread monitorThread;       // 监控线程

    public ImageFrame() {
        initializeComponents();
        setupLayout();
        setupMenuBar();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("DICOM图像查看器");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // 添加鼠标监听器
        CustomMouseListener mouseListener = new CustomMouseListener(this);
        imageLabel.addMouseListener(mouseListener);
        imageLabel.addMouseMotionListener(mouseListener);
    }

    /**
     * 构造函数，接受预解析的DCMFile对象
     * @param dcmFile 已解析的DICOM文件对象
     */
    public ImageFrame(DCMFile dcmFile) {
        this.dcmFile = dcmFile;
        initializeComponents();
        setupLayout();
        setupMenuBar();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭时不退出整个程序
        setTitle("DICOM图像查看器");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // 添加鼠标监听器
        CustomMouseListener mouseListener = new CustomMouseListener(this);
        imageLabel.addMouseListener(mouseListener);
        imageLabel.addMouseMotionListener(mouseListener);
        
        // 如果已有DICOM文件，立即显示
        if (dcmFile != null) {
            loadFromDCMFile();
        }
    }

    private void initializeComponents() {
        // 图像显示面板
        imagePanel = new JPanel(new BorderLayout());
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imagePanel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
        
        // 元数据显示区域
        metadataArea = new JTextArea(10, 30);
        metadataArea.setEditable(false);
        metadataArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // 窗宽窗位控制滑块
        windowCenterSlider = new JSlider(0, 4096, defaultWindowCenter);
        windowCenterSlider.addChangeListener(e -> {
            center = windowCenterSlider.getValue();
            textCenter.setText(center.toString());
            updateImageDisplay();
        });
        
        windowWidthSlider = new JSlider(1, 4096, defaultWindowWidth);
        windowWidthSlider.addChangeListener(e -> {
            window = windowWidthSlider.getValue();
            textWindow.setText(window.toString());
            updateImageDisplay();
        });
        
        // 窗宽窗位文本框
        textWindow = new JTextField(window.toString(), 8);
        textWindow.addActionListener(e -> {
            try {
                window = Integer.parseInt(textWindow.getText());
                windowWidthSlider.setValue(window);
                updateImageDisplay();
            } catch (NumberFormatException ex) {
                textWindow.setText(window.toString());
            }
        });
        
        textCenter = new JTextField(center.toString(), 8);
        textCenter.addActionListener(e -> {
            try {
                center = Integer.parseInt(textCenter.getText());
                windowCenterSlider.setValue(center);
                updateImageDisplay();
            } catch (NumberFormatException ex) {
                textCenter.setText(center.toString());
            }
        });
        
        // 线程控制组件
        startThreadButton = new JButton("启动线程测试");
        startThreadButton.addActionListener(new StartThreadListener());
        
        threadOutputArea = new JTextArea(5, 20);
        threadOutputArea.setEditable(false);
        threadOutputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        threadOutputArea.setBorder(BorderFactory.createTitledBorder("线程输出"));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        add(imagePanel, BorderLayout.CENTER);
        
        // 右侧面板
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JScrollPane(metadataArea), BorderLayout.CENTER);
        
        // 窗宽窗位控制面板
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("窗宽窗位调节"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("窗位:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(windowCenterSlider, gbc);
        gbc.gridx = 2;
        controlPanel.add(textCenter, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("窗宽:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(windowWidthSlider, gbc);
        gbc.gridx = 2;
        controlPanel.add(textWindow, gbc);
        
        // 线程控制面板
        JPanel threadPanel = new JPanel(new BorderLayout());
        threadPanel.setBorder(BorderFactory.createTitledBorder("多线程测试"));
        threadPanel.add(startThreadButton, BorderLayout.NORTH);
        threadPanel.add(new JScrollPane(threadOutputArea), BorderLayout.CENTER);
        
        // 将控制面板和线程面板组合
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(threadPanel, BorderLayout.CENTER);
        
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);
        rightPanel.setPreferredSize(new Dimension(350, 0));
        
        add(rightPanel, BorderLayout.EAST);
    }
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("文件");
        JMenuItem openItem = new JMenuItem("打开DICOM文件");
        openItem.addActionListener(new OpenFileListener());
        fileMenu.add(openItem);
        
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private class OpenFileListener implements ActionListener {
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
            
            if (fileChooser.showOpenDialog(ImageFrame.this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                loadDicomFile(selectedFile);
            }
        }
    }
    
    /**
     * 启动线程按钮监听器
     */
    private class StartThreadListener implements ActionListener {        @Override
        public void actionPerformed(ActionEvent e) {
            // 停止之前的监控线程和工作线程
            if (monitorThread != null && monitorThread.isAlive()) {
                monitorThread.interrupt();
            }
            if (thread1 != null && thread1.isAlive()) {
                thread1.interrupt();
            }
            if (thread2 != null && thread2.isAlive()) {
                thread2.interrupt();
            }
            
            // 清空输出区域并重置同步状态
            threadOutputArea.setText("");
            ThreadTester.resetSync();
            
            // 创建并启动新线程
            thread1 = new ThreadTester(threadOutputArea, "1");
            thread2 = new ThreadTester(threadOutputArea, "2");
            
            thread1.start();
            thread2.start();
            
            // 更新按钮状态
            startThreadButton.setText("线程运行中...");
            startThreadButton.setEnabled(false);              // 启动一个监控线程，在5秒后停止测试线程
            monitorThread = new Thread(() -> {
                try {
                    Thread.sleep(5000); // 运行5秒
                    
                    // 中断线程
                    if (thread1 != null) thread1.interrupt();
                    if (thread2 != null) thread2.interrupt();
                      // 等待线程真正结束，最多等待1秒
                    long stopTime = System.currentTimeMillis() + 1000;
                    while ((thread1.isAlive() || thread2.isAlive()) && 
                           System.currentTimeMillis() < stopTime) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                    
                    // 在UI线程中更新按钮状态
                    SwingUtilities.invokeLater(() -> {
                        startThreadButton.setText("启动线程测试");
                        startThreadButton.setEnabled(true);
                        // 显示线程停止信息
                        threadOutputArea.append("\n--- 线程已停止 ---\n");
                    });
                } catch (InterruptedException ignored) {
                    // 监控线程被中断，也要恢复按钮状态
                    SwingUtilities.invokeLater(() -> {
                        startThreadButton.setText("启动线程测试");
                        startThreadButton.setEnabled(true);
                    });                }
            }, "ThreadMonitor");
            monitorThread.start();
        }
    }

    private void loadDicomFile(File file) {
        try {
            System.out.println("开始加载DICOM文件: " + file.getAbsolutePath());
            dcmFile = new DCMFile(file.getAbsolutePath());
            boolean parseSuccess = dcmFile.Parse();
            System.out.println("DICOM文件解析结果: " + parseSuccess);
            
            if (parseSuccess) {
                loadFromDCMFile();
                setTitle("DICOM图像查看器 - " + file.getName());
            } else {
                JOptionPane.showMessageDialog(this, 
                    "DICOM文件解析失败", 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "加载DICOM文件失败: " + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * 从已解析的DCMFile对象加载数据
     */
    private void loadFromDCMFile() {
        try {
            System.out.println("开始从DCMFile对象加载数据");
            displayMetadata();
            displayImage();
            System.out.println("数据加载完成");
        } catch (Exception e) {
            System.err.println("从DCMFile加载数据时出错: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "显示DICOM数据失败: " + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayMetadata() {
        if (dcmFile == null) {
            System.out.println("displayMetadata: dcmFile为null");
            return;
        }
        
        System.out.println("开始显示元数据");
        StringBuilder metadata = new StringBuilder();
        
        try {
            // 患者信息
            metadata.append("=== 患者信息 ===\n");
            String patientName = dcmFile.getPatientName();
            String patientID = dcmFile.getPatientID();
            System.out.println("患者姓名: '" + patientName + "'");
            System.out.println("患者ID: '" + patientID + "'");
            metadata.append("患者姓名: ").append(patientName).append("\n");
            metadata.append("患者ID: ").append(patientID).append("\n");
            
            // 研究信息
            metadata.append("\n=== 研究信息 ===\n");
            String studyDate = dcmFile.getStudyDate();
            String modality = dcmFile.getModality();
            System.out.println("研究日期: '" + studyDate + "'");
            System.out.println("检查方式: '" + modality + "'");
            metadata.append("研究日期: ").append(studyDate).append("\n");
            metadata.append("检查方式: ").append(modality).append("\n");
            
            // 图像信息
            metadata.append("\n=== 图像信息 ===\n");
            int rows = dcmFile.getRows();
            int columns = dcmFile.getColumns();
            System.out.println("图像尺寸: " + rows + " x " + columns);
            metadata.append("行数: ").append(rows).append("\n");
            metadata.append("列数: ").append(columns).append("\n");
            
            // 获取位深度
            Object bitsAllocated = dcmFile.GetValue(DicomTags.BitsAllocated);
            if (bitsAllocated != null) {
                System.out.println("位深度: " + bitsAllocated);
                metadata.append("位深度: ").append(bitsAllocated).append(" bits\n");
            } else {
                System.out.println("未找到位深度信息");
            }
            
            // 获取光度解释
            Object photometricInterpretation = dcmFile.GetValue(DicomTags.PhotometricInterpretation);
            if (photometricInterpretation != null) {
                System.out.println("光度解释: " + photometricInterpretation);
                metadata.append("光度解释: ").append(photometricInterpretation).append("\n");
            }
            
            // 获取窗宽窗位信息
            Object windowCenter = dcmFile.GetValue(DicomTags.WindowCenter);
            Object windowWidth = dcmFile.GetValue(DicomTags.WindowWidth);
            
            metadata.append("\n=== 显示参数 ===\n");
            if (windowCenter != null) {
                System.out.println("建议窗位: " + windowCenter);
                metadata.append("建议窗位: ").append(windowCenter).append("\n");
                // 更新滑块默认值和文本框
                try {
                    center = convertToInt(windowCenter);
                    windowCenterSlider.setValue(center);
                    textCenter.setText(center.toString());
                } catch (Exception ignored) {}
            }
            if (windowWidth != null) {
                System.out.println("建议窗宽: " + windowWidth);
                metadata.append("建议窗宽: ").append(windowWidth).append("\n");
                try {
                    window = convertToInt(windowWidth);
                    windowWidthSlider.setValue(window);
                    textWindow.setText(window.toString());
                } catch (Exception ignored) {}
            }
            
            // 检查像素数据
            Object pixelData = dcmFile.GetValue(DicomTags.PixelData);
            metadata.append("\n=== 像素数据 ===\n");
            if (pixelData != null) {
                System.out.println("像素数据类型: " + pixelData.getClass().getName());
                metadata.append("像素数据: 存在\n");
                metadata.append("数据类型: ").append(pixelData.getClass().getSimpleName()).append("\n");
            } else {
                System.out.println("警告: 未找到像素数据");
                metadata.append("像素数据: 未找到\n");
            }
            
        } catch (Exception e) {
            System.err.println("获取元数据时出错: " + e.getMessage());
            e.printStackTrace();
            metadata.append("获取元数据时出错: ").append(e.getMessage());
        }
        
        System.out.println("设置元数据文本到显示区域");
        metadataArea.setText(metadata.toString());
    }

    private void displayImage() {
        if (dcmFile == null) {
            System.out.println("displayImage: dcmFile为null");
            return;
        }
        
        System.out.println("开始显示图像");
        
        try {
            BufferedImage image = convertDicomToImage();
            if (image != null) {
                System.out.println("图像转换成功，尺寸: " + image.getWidth() + "x" + image.getHeight());
                currentImage = image;
                updateImageDisplay();
                System.out.println("图像显示完成");
            } else {
                System.out.println("警告: 图像转换失败，返回null");
                imageLabel.setText("无法显示图像：图像转换失败");
            }
        } catch (Exception e) {
            System.err.println("显示图像失败: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "显示图像失败: " + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
            imageLabel.setText("图像显示错误：" + e.getMessage());
        }
    }

    private BufferedImage convertDicomToImage() throws Exception {
        System.out.println("开始DICOM到图像的转换");
        
        // 获取图像尺寸
        int rows = dcmFile.getRows();
        int columns = dcmFile.getColumns();
        
        System.out.println("图像尺寸: " + rows + " x " + columns);
        
        if (rows <= 0 || columns <= 0) {
            throw new Exception("无效的图像尺寸: " + rows + "x" + columns);
        }
          
        // 获取像素数据
        Object pixelData = dcmFile.GetValue(DicomTags.PixelData);
        System.out.println("像素数据获取结果: " + (pixelData != null ? "成功" : "失败"));
        
        if (pixelData == null) {
            throw new Exception("未找到像素数据");
        }
        
        System.out.println("像素数据类型: " + pixelData.getClass().getName());
          
        // 获取位深度
        Object bitsAllocatedObj = dcmFile.GetValue(DicomTags.BitsAllocated);
        int bitsAllocated = 16; // 默认16位
        if (bitsAllocatedObj != null) {
            bitsAllocated = convertToInt(bitsAllocatedObj);
        }
        System.out.println("位深度: " + bitsAllocated + " bits");
            
        byte[] pixelBytes;
        if (pixelData instanceof DCMDataElement) {
            // 直接从DCMDataElement的value字段获取像素数据
            DCMDataElement pixelElement = (DCMDataElement) pixelData;
            pixelBytes = pixelElement.value;
            if (pixelBytes == null) {
                throw new Exception("像素数据为空");
            }
            System.out.println("像素数据长度: " + pixelBytes.length + " bytes");
        } else if (pixelData instanceof byte[]) {
            // 直接处理字节数组类型的像素数据
            pixelBytes = (byte[]) pixelData;
            System.out.println("像素数据长度: " + pixelBytes.length + " bytes");
        } else {
            throw new Exception("不支持的像素数据类型: " + pixelData.getClass());
        }
        
        // 检查数据长度是否合理
        int expectedLength = rows * columns * (bitsAllocated / 8);
        System.out.println("期望像素数据长度: " + expectedLength + " bytes, 实际长度: " + pixelBytes.length + " bytes");
        
        // 创建BufferedImage
        System.out.println("创建BufferedImage...");
        BufferedImage image = new BufferedImage(columns, rows, BufferedImage.TYPE_BYTE_GRAY);
        
        // 转换像素数据
        if (bitsAllocated == 8) {
            System.out.println("处理8位像素数据");
            // 8位数据直接使用
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < columns; x++) {
                    int index = y * columns + x;
                    if (index < pixelBytes.length) {
                        int grayValue = pixelBytes[index] & 0xFF;
                        int rgb = (grayValue << 16) | (grayValue << 8) | grayValue;
                        image.setRGB(x, y, rgb);
                    }
                }
            }
        } else {
            System.out.println("处理16位像素数据，转换为8位显示");
            // 16位数据需要转换为8位显示
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < columns; x++) {
                    int index = (y * columns + x) * 2;
                    if (index + 1 < pixelBytes.length) {
                        int pixelValue = ((pixelBytes[index + 1] & 0xFF) << 8) | (pixelBytes[index] & 0xFF);
                        // 简单的线性映射到8位
                        int grayValue = Math.max(0, Math.min(255, pixelValue >> 4));
                        int rgb = (grayValue << 16) | (grayValue << 8) | grayValue;
                        image.setRGB(x, y, rgb);
                    }
                }
            }
        }
        
        System.out.println("图像转换完成");
        return image;
    }
    
    private void updateImageDisplay() {
        if (currentImage == null) return;
        
        // 应用窗宽窗位调整，使用当前的center和window值
        BufferedImage adjustedImage = applyWindowLevel(currentImage, center, window);
        
        // 缩放图像以适应显示区域
        ImageIcon icon = new ImageIcon(adjustedImage);
        imageLabel.setIcon(icon);
        imageLabel.revalidate();
    }
    
    private BufferedImage applyWindowLevel(BufferedImage source, int windowCenter, int windowWidth) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        double windowMin = windowCenter - windowWidth / 2.0;
        double windowMax = windowCenter + windowWidth / 2.0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = source.getRGB(x, y);
                int gray = rgb & 0xFF; // 获取灰度值
                
                // 应用窗宽窗位
                double normalizedValue;
                if (gray <= windowMin) {
                    normalizedValue = 0;
                } else if (gray >= windowMax) {
                    normalizedValue = 255;
                } else {
                    normalizedValue = ((gray - windowMin) / (windowMax - windowMin)) * 255;
                }
                
                int adjustedGray = (int) Math.round(normalizedValue);
                int adjustedRgb = (adjustedGray << 16) | (adjustedGray << 8) | adjustedGray;
                result.setRGB(x, y, adjustedRgb);
            }
        }
        
        return result;
    }
    
    /**
     * 将DICOM数据元素的值转换为整数
     * 处理不同类型的数据：字节数组、字符串等
     * @param value DICOM数据元素的值
     * @return 转换后的整数值
     */
    private int convertToInt(Object value) {
        if (value == null) {
            return 0;
        }
        
        if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return 0;
            } else if (bytes.length == 1) {
                // 8位无符号整数
                return bytes[0] & 0xFF;
            } else if (bytes.length == 2) {
                // 16位无符号整数（小端序）
                return (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8);
            } else if (bytes.length == 4) {
                // 32位整数（小端序）
                return (bytes[0] & 0xFF) |
                       ((bytes[1] & 0xFF) << 8) |
                       ((bytes[2] & 0xFF) << 16) |
                       ((bytes[3] & 0xFF) << 24);
            } else {
                // 对于长度超过4的字节数组，尝试解析前4个字节
                return (bytes[0] & 0xFF) |
                       ((bytes[1] & 0xFF) << 8) |
                       ((bytes[2] & 0xFF) << 16) |
                       ((bytes[3] & 0xFF) << 24);
            }
        } else if (value instanceof String) {
            String str = (String) value;
            try {
                // 尝试直接解析字符串
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException e) {
                // 如果包含小数点，取整数部分
                try {
                    double d = Double.parseDouble(str.trim());
                    return (int) Math.round(d);
                } catch (NumberFormatException e2) {
                    System.err.println("无法将字符串转换为整数: " + str);
                    return 0;
                }
            }
        } else {
            // 其他类型，尝试转换为字符串再解析
            String str = value.toString();
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException e) {
                try {
                    double d = Double.parseDouble(str.trim());
                    return (int) Math.round(d);
                } catch (NumberFormatException e2) {
                    System.err.println("无法将对象转换为整数: " + value + " (类型: " + value.getClass() + ")");
                    return 0;
                }
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 使用默认外观
            new ImageFrame().setVisible(true);
        });
    }
}
