package client.Admin.view.styling;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Centralized theme and styling for the Admin application
 */
public class AdminTheme {

    // Color scheme - Dark modern theme
    public static final Color BACKGROUND_DARK = new Color(26, 32, 44);
    public static final Color SURFACE_DARK = new Color(45, 55, 72);
    public static final Color SURFACE_LIGHT = new Color(74, 85, 104);
    public static final Color ACCENT_BLUE = new Color(56, 178, 172);
    public static final Color ACCENT_GREEN = new Color(72, 187, 120);
    public static final Color ACCENT_RED = new Color(245, 101, 101);
    public static final Color ACCENT_ORANGE = new Color(237, 137, 54);
    public static final Color TEXT_PRIMARY = new Color(237, 242, 247);
    public static final Color TEXT_SECONDARY = new Color(160, 174, 192);
    public static final Color BORDER_COLOR = new Color(74, 85, 104);

    // Font definitions
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE_CONTENT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_MONOSPACE = new Font("JetBrains Mono", Font.PLAIN, 12);
    public static final Font FONT_MENU = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_STATUS = new Font("Segoe UI", Font.BOLD, 12);

    /**
     * Sets up modern look and feel
     */
    public static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Set modern UI properties
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumb", SURFACE_LIGHT);
            UIManager.put("ScrollBar.track", SURFACE_DARK);
            UIManager.put("TabbedPane.selected", ACCENT_BLUE);
            UIManager.put("TabbedPane.selectedForeground", Color.BLACK);
            UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
            UIManager.put("TabbedPane.background", SURFACE_DARK);
        } catch (Exception ignored) {
            System.err.println("Could not set look and feel");
        }
    }

    /**
     * Creates a styled text field
     */
    public static JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(SURFACE_LIGHT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(FONT_SUBTITLE);
        return field;
    }

    /**
     * Creates a styled password field
     */
    public static JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setBackground(SURFACE_LIGHT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(FONT_SUBTITLE);
        return field;
    }

    /**
     * Creates a styled button with hover effects
     */
    public static JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(text.equals("Login") ? Color.BLACK : Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(FONT_BUTTON);

        // Add hover effect
        addHoverEffect(button, color);

        return button;
    }

    /**
     * Adds hover effect to button
     */
    private static void addHoverEffect(JButton button, Color originalColor) {
        Color originalForeground = button.getForeground();

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalColor.brighter());
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(originalColor.brighter().brighter(), 1),
                        BorderFactory.createEmptyBorder(9, 19, 9, 19)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
                button.setForeground(originalForeground);
                button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            }
        });
    }

    /**
     * Styles a table with modern dark theme
     */
    public static void styleTable(JTable table) {
        table.setBackground(SURFACE_DARK);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(ACCENT_BLUE);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(35);
        table.setFont(FONT_TABLE_CONTENT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(SURFACE_LIGHT);
        header.setForeground(TEXT_PRIMARY);
        header.setFont(FONT_TABLE_HEADER);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        header.setPreferredSize(new Dimension(0, 40));
    }

    /**
     * Styles a scroll pane with modern dark theme
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(SURFACE_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getVerticalScrollBar().setBackground(SURFACE_DARK);
        scrollPane.getHorizontalScrollBar().setBackground(SURFACE_DARK);
    }

    /**
     * Creates a styled label
     */
    public static JLabel createStyledLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Creates a styled panel with border
     */
    public static JPanel createStyledPanel(Color backgroundColor) {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        return panel;
    }

    /**
     * Creates a bordered panel
     */
    public static JPanel createBorderedPanel(Color backgroundColor) {
        JPanel panel = createStyledPanel(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    /**
     * Creates the application icon
     */
    public static Image createApplicationIcon() {
        int size = 32;
        java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = icon.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ACCENT_BLUE);
        g2.fillRoundRect(4, 4, size-8, size-8, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("A", 12, 22);
        g2.dispose();
        return icon;
    }

    /**
     * Creates a styled text area
     */
    public static JTextArea createStyledTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(FONT_MONOSPACE);
        textArea.setBackground(SURFACE_DARK);
        textArea.setForeground(TEXT_PRIMARY);
        textArea.setCaretColor(ACCENT_BLUE);
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return textArea;
    }

    /**
     * Creates a standard insets object
     */
    public static Insets getStandardInsets() {
        return new Insets(10, 10, 10, 10);
    }

    /**
     * Creates padding insets
     */
    public static Insets getPaddingInsets(int padding) {
        return new Insets(padding, padding, padding, padding);
    }
}