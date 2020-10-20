package gui.controller;

import gui.java.NodeUI;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import model.Vector2;

public class NodeUIController {

    @FXML
    private Label lblGCost;
    @FXML
    private Label lblHCost;
    @FXML
    private Label lblFCost;
    @FXML
    private GridPane container;

    private NodeUI root;

    @FXML
    public void initialize() {
        setGCost("");
        setHCost("");
        setFCost("");
        showCosts(false);
        container.setStyle("-fx-border-color: rgba(88,88,88,0.5)");
    }

    public void setNodeClosed() {
        setColor(Color.rgb(219, 86, 86));
    }

    public void setNodeOpen() {
        setColor(Color.rgb(123, 208, 147));
    }

    public void showCosts(boolean show) {
        lblGCost.setVisible(show);
        lblHCost.setVisible(show);
        lblFCost.setVisible(show);
    }

    public void showOnlyFCost() {
        lblGCost.setVisible(false);
        lblHCost.setVisible(false);
        lblFCost.setVisible(true);
    }

    @FXML
    public void onClick(MouseEvent e) {

        if (root.getMainUIController().isSetStart()) {

            setAsStart();

        } else if (root.getMainUIController().isSetDestination()) {

            setAsDestination();

        } else if (!root.getMainUIController().isAstarRunning() && root.getMainUIController().isSetBlocks()) {
            if (e.getButton() == MouseButton.PRIMARY) {

                setBlockNode();

            } else if (e.getButton() == MouseButton.SECONDARY) {

                removeBlockNode();
            }
        } else {
            /*Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);

            Label lbl = new Label(String.format("Col: %d, Row: %d\nCost: %s", GridPane.getColumnIndex(container),
                    GridPane.getRowIndex(container), lblFCost.getText()));
            HBox hBox = new HBox(20);
            hBox.getChildren().add(lbl);
            Scene scene = new Scene(hBox);
            stage.setScene(scene);
            stage.show();*/
        }


    }

    public void setAsStart() {
        root.getMainUIController().getNodeUI(root.getMainUIController().getAstar().getFrom().getPos()).getUiController().setColor(Color.WHITE);
        root.getMainUIController().getAstar().setFrom(new Vector2(GridPane.getColumnIndex(root), GridPane.getRowIndex(root)));
        setColor(Color.rgb(0, 255, 0));
    }

    public void setAsDestination() {
        root.getMainUIController().getNodeUI(root.getMainUIController().getAstar().getTo().getPos()).getUiController().setColor(Color.WHITE);
        root.getMainUIController().getAstar().setTo(new Vector2(GridPane.getColumnIndex(root), GridPane.getRowIndex(root)));
        setColor(Color.rgb(255, 0, 0));
    }

    public void removeBlockNode() {
        root.getMainUIController().setBlockUI(root, false);
        setColor(Color.WHITE);
    }


    public void setBlockNode() {
        root.getMainUIController().setBlockUI(root, true);
        setColor(Color.BLACK);

    }

    public void setPath() {
        System.out.println("is path");
        setColor(Color.ORANGE);
    }

    public void setColor(Color fill) {
        container.setBackground(new Background(new BackgroundFill(
                fill,
                CornerRadii.EMPTY,
                Insets.EMPTY)));
    }

    public NodeUI getRoot() {
        return root;
    }

    public void setRoot(NodeUI root) {
        this.root = root;
    }

    public void setGCost(String value) {
        lblGCost.setText(value);
    }

    public void setHCost(String value) {
        lblHCost.setText(value);
    }

    public void setFCost(String value) {
        lblFCost.setText(value);
    }

}
