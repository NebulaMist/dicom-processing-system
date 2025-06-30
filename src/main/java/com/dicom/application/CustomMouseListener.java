package com.dicom.application;

import com.dicom.gui.ImageFrame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 自定义鼠标监听器类，用于处理图像显示的鼠标交互
 * 水平拖动调整窗宽，垂直拖动调整窗位
 */
public class CustomMouseListener extends MouseAdapter {
    private boolean pressed;
    private int oldX;
    private int oldY;
    private ImageFrame iframe;

    /**
     * 构造函数，传入ImageFrame窗体引用
     * @param frame ImageFrame窗体对象
     */
    public CustomMouseListener(ImageFrame frame) {
        iframe = frame;
    }

    /**
     * 重写鼠标按下事件处理程序
     */
    @Override
    public void mousePressed(MouseEvent e) {
        pressed = true;    // 记录按下状态，x，y坐标
        oldX = e.getX();
        oldY = e.getY();
    }

    /**
     * 重写鼠标释放事件处理程序
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        pressed = false;   // 清除按下状态
    }

    /**
     * 重写鼠标拖动事件处理程序
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (pressed) {  // 如果按下时拖动
            iframe.window += (e.getX() - oldX) * 10;  // 水平方向拖动调整窗宽
            iframe.center += (e.getY() - oldY) * 10;   // 垂直方向拖动调整窗位
            iframe.textWindow.setText(iframe.window.toString());
            iframe.textCenter.setText(iframe.center.toString());
            iframe.repaint();			// 刷新显示
            oldX = e.getX();			// 更新x,y坐标，为下次计算增量使用
            oldY = e.getY();
        }
    }
}
