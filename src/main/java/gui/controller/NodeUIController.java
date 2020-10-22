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
import model.AstarNode;
import model.NodeType;
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

    private NodeType nodeType = NodeType.EMPTY;

    final private Color EMPTY_COLOR = Color.rgb(112, 111, 211);
    final private Color START_COLOR = Color.rgb(46, 204, 113);
    final private Color DESTINATION_COLOR = Color.rgb(231, 76, 60);
    final private Color OPEN_COLOR = Color.rgb(82, 82, 160);
    final private Color CLOSED_COLOR = Color.rgb(44, 44, 84);
    final private Color BLOCK_COLOR = Color.rgb(52, 73, 94);
    final private Color PATH_COLOR = Color.rgb(9, 132, 227);

    @FXML
    public void initialize() {
        setGCost("");
        setHCost("");
        setFCost("");
        showCosts(false);
        container.setStyle("-fx-border-color: rgba(99, 110, 114, 0.1); -fx-text-fill: antiquewhite");
        setColor(EMPTY_COLOR);
    }

    public void setNodeClosed() {
        nodeType = NodeType.CLOSED;
        setColor(CLOSED_COLOR);
    }

    public void setNodeOpen() {
        nodeType = NodeType.OPEN;
        setColor(OPEN_COLOR);
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

        if(!root.getMainUIController().isAstarRunning()) {
            if (root.getMainUIController().settingStart()) {

                setAsStart();

            } else if (root.getMainUIController().settingDestination()) {

                setAsDestination();

            } else if (root.getMainUIController().settingBlocks()) {
                if (e.getButton() == MouseButton.PRIMARY) {

                    setAsBlock();

                } else if (e.getButton() == MouseButton.SECONDARY) {

                    removeBlock();
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
    }

    public void setAsStart() {
        nodeType = NodeType.START;
        removeBlock();
        root.getMainUIController().getNodeUI(root.getMainUIController().getAstar().getFrom().getPos()).getUiController().setColor(EMPTY_COLOR);
        root.getMainUIController().getAstar().setFrom(getPos());
        root.getMainUIController().setStartSet(true);
        setColor(START_COLOR);
    }

    public void setAsDestination() {
        nodeType = NodeType.DESTINATION;
        removeBlock();
        root.getMainUIController().getNodeUI(root.getMainUIController().getAstar().getTo().getPos()).getUiController().setColor(EMPTY_COLOR);
        root.getMainUIController().getAstar().setTo(getPos());
        setColor(DESTINATION_COLOR);
        root.getMainUIController().setDestinationSet(true);
    }

    public void removeBlock() {
        root.getMainUIController().setBlock(root, false);
        setColor(EMPTY_COLOR);
    }


    public void setAsBlock() {
        nodeType = NodeType.BLOCK;
        if (!root.getMainUIController().getAstar().getFrom().getPos().equals(getPos()) &&
                !root.getMainUIController().getAstar().getTo().getPos().equals(getPos())) {
            root.getMainUIController().setBlock(root, true);
            setColor(BLOCK_COLOR);
        }

    }

    public Vector2 getPos() {
        return new Vector2(GridPane.getColumnIndex(root), GridPane.getRowIndex(root));
    }

    public void setPath() {
        nodeType = NodeType.PATH;
        setColor(PATH_COLOR);
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

    public Color getEMPTY_COLOR() {
        return EMPTY_COLOR;
    }

    public NodeType getNodeType() {
        return this.nodeType;
    }

}
