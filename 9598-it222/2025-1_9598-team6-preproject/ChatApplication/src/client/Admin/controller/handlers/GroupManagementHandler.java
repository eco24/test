package client.Admin.controller.handlers;

import client.Admin.model.AdminModel;
import client.Admin.view.AdminView;
import java.awt.event.ActionEvent;

/**
 * Handles group management operations
 */
public class GroupManagementHandler {

    private final AdminModel model;
    private final AdminView view;

    public GroupManagementHandler(AdminModel model, AdminView view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Handles refresh groups action
     */
    public void handleRefreshGroups(ActionEvent e) {
        if (model.isAuthenticated()) {
            view.setStatusBar("Refreshing groups...");
            model.requestAllGroups();
        }
    }

    /**
     * Handles delete group action
     */
    public void handleDeleteGroup(ActionEvent e) {
        if (!model.isAuthenticated()) return;

        String selectedGroupId = view.getSelectedGroupId();
        if (!validateGroupSelection(selectedGroupId)) {
            return;
        }

        if (view.showConfirmation("Are you sure you want to delete group: " + selectedGroupId + "?")) {
            model.deleteGroup(selectedGroupId);
            view.setStatusBar("Deleting group: " + selectedGroupId);
        }
    }

    /**
     * Handles view group members action
     */
    public void handleViewGroupMembers(ActionEvent e) {
        if (!model.isAuthenticated()) return;

        String selectedGroupId = view.getSelectedGroupId();
        if (!validateGroupSelection(selectedGroupId)) {
            return;
        }

        model.getGroupMembers(selectedGroupId);
        view.setStatusBar("Fetching members for group: " + selectedGroupId);
    }

    /**
     * Validates group selection for operations
     */
    private boolean validateGroupSelection(String selectedGroupId) {
        if (selectedGroupId == null) {
            view.showError("Please select a group");
            return false;
        }
        return true;
    }
}