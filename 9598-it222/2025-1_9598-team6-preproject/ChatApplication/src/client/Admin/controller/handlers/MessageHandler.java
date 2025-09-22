package client.Admin.controller.handlers;

import client.Admin.model.AdminModel;
import client.Admin.view.AdminView;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles message log operations (clear and export)
 */
public class MessageHandler {

    private final AdminModel model;
    private final AdminView view;

    public MessageHandler(AdminModel model, AdminView view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Handles clear messages action
     */
    public void handleClearMessages(ActionEvent e) {
        if (view.showConfirmation("Are you sure you want to clear the message log?")) {
            view.clearMessageLog();
            view.setStatusBar("Message log cleared");
        }
    }

    /**
     * Handles export messages action
     */
    public void handleExportMessages(ActionEvent e) {
        JFileChooser fileChooser = setupFileChooser();

        int result = fileChooser.showSaveDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            exportToFile(fileChooser.getSelectedFile());
        }
    }

    /**
     * Sets up the file chooser for message export
     */
    private JFileChooser setupFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Message Log");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String defaultFileName = "admin_log_" + sdf.format(new Date()) + ".txt";
        fileChooser.setSelectedFile(new File(defaultFileName));

        return fileChooser;
    }

    /**
     * Exports message log to specified file
     */
    private void exportToFile(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(view.getMessageLog());
            view.showSuccess("Message log exported successfully");
            view.setStatusBar("Log exported to: " + file.getName());
        } catch (IOException ex) {
            view.showError("Failed to export log: " + ex.getMessage());
            view.setStatusBar("Export failed: " + ex.getMessage());
        }
    }
}