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

    final private Color DEF_COLOR = Color.rgb(112, 111, 211);

    @FXML
    public void initialize() {
        setGCost("");
        setHCost("");
        setFCost("");
        showCosts(false);
        //container.setStyle("-fx-border-color: #535c68;");
        setColor(DEF_COLOR);
    }

    public void setNodeClosed() {
        setColor(Color.rgb(44, 44, 84));
    }

    public void setNodeOpen() {
        setColor(Color.rgb(82, 82, 160));
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
            if (root.getMainUIController().isSetStart()) {

                setAsStart();

            } else if (root.getMainUIController().isSetDestination()) {

                setAsDestination();

            } else if (root.getMainUIController().isSetBlocks()) {
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




    }

    public void setAsStart() {
        root.getMainUIController().getNodeUI(root.getMainUIController().getAstar().getFrom().getPos()).getUiController().setColor(DEF_COLOR);
        root.getMainUIController().getAstar().setFrom(getPos());
        setColor(Color.rgb(46, 204, 113));
    }

    public void setAsDestination() {
        root.getMainUIController().getNodeUI(root.getMainUIController().getAstar().getTo().getPos()).getUiController().setColor(DEF_COLOR);
        root.getMainUIController().getAstar().setTo(getPos());
        setColor(Color.rgb(231, 76, 60));
    }

    public void removeBlockNode() {
        root.getMainUIController().setBlockUI(root, false);
        setColor(DEF_COLOR);
    }


    public void setBlockNode() {
        if (!root.getMainUIController().getAstar().getFrom().getPos().equals(getPos()) &&
                !root.getMainUIController().getAstar().getTo().getPos().equals(getPos())) {
            root.getMainUIController().setBlockUI(root, true);
            setColor(Color.rgb(52, 73, 94));
        }

    }

    public Vector2 getPos() {
        return new Vector2(GridPane.getColumnIndex(root), GridPane.getRowIndex(root));
    }

    public void setPath() {
        setColor(Color.rgb(9, 132, 227));
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

    public Color getDEF_COLOR() {
        return DEF_COLOR;
    }
}
