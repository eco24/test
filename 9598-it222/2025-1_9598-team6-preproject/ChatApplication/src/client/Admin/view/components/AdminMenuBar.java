package client.Admin.view.components;

import client.Admin.view.styling.AdminTheme;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Menu bar component for the Admin application
 */
public class AdminMenuBar extends JMenuBar {

    private JMenuItem refreshAllMenuItem;
    private JMenuItem logoutMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem aboutMenuItem;

    /**
     * Constructor
     */
    public AdminMenuBar() {
        setBackground(AdminTheme.SURFACE_DARK);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AdminTheme.BORDER_COLOR));

        initializeMenus();
    }

    /**
     * Initializes all menus and menu items
     */
    private void initializeMenus() {
        // File menu
        JMenu fileMenu = createStyledMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        refreshAllMenuItem = createStyledMenuItem("Refresh All", KeyEvent.VK_R);
        refreshAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        fileMenu.add(refreshAllMenuItem);

        fileMenu.addSeparator();

        logoutMenuItem = createStyledMenuItem("Logout", KeyEvent.VK_L);
        logoutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        fileMenu.add(logoutMenuItem);

        exitMenuItem = createStyledMenuItem("Exit", KeyEvent.VK_X);
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        fileMenu.add(exitMenuItem);

        add(fileMenu);

        // View menu
        JMenu viewMenu = createStyledMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        add(viewMenu);

        // Help menu
        JMenu helpMenu = createStyledMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        aboutMenuItem = createStyledMenuItem("About", KeyEvent.VK_A);
        helpMenu.add(aboutMenuItem);

        add(helpMenu);
    }

    /**
     * Creates styled menu
     */
    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(AdminTheme.TEXT_PRIMARY);
        menu.setFont(AdminTheme.FONT_MENU);
        return menu;
    }

    /**
     * Creates styled menu item
     */
    private JMenuItem createStyledMenuItem(String text, int mnemonic) {
        JMenuItem item = new JMenuItem(text, mnemonic);
        item.setBackground(AdminTheme.SURFACE_DARK);
        item.setForeground(AdminTheme.TEXT_PRIMARY);
        item.setFont(AdminTheme.FONT_SUBTITLE);
        return item;
    }

    // ==================== EVENT LISTENER METHODS ====================

    public void addRefreshAllListener(ActionListener listener) {
        refreshAllMenuItem.addActionListener(listener);
    }

    public void addLogoutListener(ActionListener listener) {
        logoutMenuItem.addActionListener(listener);
    }

    public void addExitListener(ActionListener listener) {
        exitMenuItem.addActionListener(listener);
    }

    public void addAboutListener(ActionListener listener) {
        aboutMenuItem.addActionListener(listener);
    }
}