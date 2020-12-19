package gui.uiparts;

import gui.controller.MainUIController;
import gui.controller.NodeUIController;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import util.AStar;

import java.io.IOException;

public class NodeUI extends GridPane {

    final private MainUIController mainUIController;
    private NodeUIController uiController;
    private AStar astar;

    public NodeUI(MainUIController mainUIController) {
        this.mainUIController = mainUIController;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NodeUI.fxml"));
        loader.setRoot(this);
        try {
            loader.load();
            uiController = loader.getController();
            uiController.setRoot(this);

            this.addEventHandler(MouseEvent.DRAG_DETECTED, e -> {
                e.consume();
                uiController.getRoot().startFullDrag();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public NodeUIController getUiController() {
        return uiController;
    }

    public MainUIController getMainUIController() {
        return mainUIController;
    }
}
