package com.raddle.file.gui.search;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

public class TabTitlePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JLabel titleLebel;
    private CloseButton closebutton;
    private final JTabbedPane tabbedPane;

    public TabTitlePanel(String s, JTabbedPane tabbedPane){
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.tabbedPane = tabbedPane;
        titleLebel = new JLabel(s);
        closebutton = new CloseButton();
        add(titleLebel);
        add(closebutton);
        titleLebel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setOpaque(false);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int ret = JOptionPane.showConfirmDialog(TabTitlePanel.this.tabbedPane, "你确定关闭标签[" + titleLebel.getText() + "]吗？");
                    if (ret == JOptionPane.YES_OPTION) {
                        TabTitlePanel.this.tabbedPane.remove(TabTitlePanel.this.tabbedPane.indexOfTabComponent(TabTitlePanel.this));
                    }
                } else {
                    TabTitlePanel.this.tabbedPane.setSelectedIndex(TabTitlePanel.this.tabbedPane.indexOfTabComponent(TabTitlePanel.this));
                }
            }
        });
    }

    public void init() {

    }

    private class CloseButton extends JButton {

        private static final long serialVersionUID = 1L;

        //        private ImageIcon icon;

        public CloseButton(){
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("关闭");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            //Making nice rollover effect
            setRolloverEnabled(true);
            setText("关闭");
            //Close the proper tab by clicking the button
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    int ret = JOptionPane.showConfirmDialog(tabbedPane, "你确定关闭标签[" + titleLebel.getText() + "]吗？");
                    if (ret == JOptionPane.YES_OPTION) {
                        tabbedPane.remove(tabbedPane.indexOfTabComponent(TabTitlePanel.this));
                    }
                }
            });
        }

        //we don't want to update UI for this button
        @Override
        public void updateUI() {
        }

        //paint the cross
        @Override
        protected void paintComponent(Graphics g) {
            //super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 3;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }

    }

    public void setTabTitle(String title) {
        titleLebel.setText(title);
    }

    public String getTabTitle() {
        return titleLebel.getText();
    }
}
