package gui.controller;

import gui.java.NodeUI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.AstarNode;
import model.Vector2;
import util.AStar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//TODO add pause play
//TODO add stop
//TODO adjust colors
//TODO add readjust grid size
//TODO fix set block on start/destination
//TODO fix togglebutton unselect when edit grid
//TODO enable/disable diagonal
//TODO fix show costs (size 50)
//TODO fix java.util.ConcurrentModificationException on calcPath
//TODO set title
//TODO set icon

public class MainUIController {
    final private int DEFAULT_GRID_SIZE = 50;
    private AStar astar;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private ToggleButton btnSetStart;
    @FXML
    private ToggleButton btnSetDestination;
    @FXML
    private Slider sldrSpeed;
    @FXML
    private CheckBox chkboxShowCosts;

    //need to reduce memory usage, takes god damn 2GB when gridsize is 100
    //probably need to use tableview or something idk
    private int gridSize;
    private GridPane astarGridPane;
    private boolean astarRunning;
    private boolean setStart;
    private boolean setDestination;
    private boolean setBlock;


    public MainUIController() {
        this.gridSize = DEFAULT_GRID_SIZE;
        this.setBlock = false;
        this.setStart = false;
        this.setDestination = false;
    }

    @FXML
    public void initialize() {
        setGridSize(gridSize);
    }

    public void setGridSize(int size) {
        this.astar = new AStar(size, new Vector2(0, 0), new Vector2(1, 1), this);
        //remove the gridpane only to re-render it with the correct size
        rootPane.getChildren().removeIf(n -> n instanceof GridPane);
        gridSize = size;
        GridPane astarGrid = new GridPane();
        astarGridPane = astarGrid;
        initGrid(astarGrid);
        //as the grid is quadratic, the total amount of nodes is equal to size^2
        int totalNodeAmount = (int) Math.pow(gridSize, 2);

        System.out.println("Setting up: Grid Nodes");
        astarGrid.getChildren().addAll(createGridNodes(totalNodeAmount));
        astarGrid.getChildren().forEach(n -> {
            NodeUI nodeUI = (NodeUI) n;
            nodeUI.getUiController().setColor(Color.WHITE);
        });

        //constraints set, so that each row and column is of the same width and height
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        cc.setPercentWidth(100);
        cc.setFillWidth(true);
        cc.setMinWidth(0);

        RowConstraints rc = new RowConstraints();
        rc.setVgrow(Priority.ALWAYS);
        rc.setPercentHeight(100);
        rc.setFillHeight(true);
        rc.setMinHeight(0);

        System.out.println("Setting up: Grid Constraints");

        for (int i = 0; i < gridSize; i++) {
            astarGrid.getRowConstraints().add(rc);
            astarGrid.getColumnConstraints().add(cc);
        }


        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                GridPane.setColumnIndex(astarGrid.getChildren().get(i * gridSize + j), j);
                GridPane.setRowIndex(astarGrid.getChildren().get(i * gridSize + j), i);
            }
        }

        //astarGrid.setGridLinesVisible(true);


        System.out.printf("Total amount of Nodes: %d", astarGrid.getChildren().size());

        //grid "anchored" to the origin as it needs to be stretched properly when the windows gets resized
        AnchorPane.setTopAnchor(astarGrid, 0d);
        AnchorPane.setLeftAnchor(astarGrid, 0d);
        AnchorPane.setRightAnchor(astarGrid, 0d);
        AnchorPane.setBottomAnchor(astarGrid, 40d);
        rootPane.getChildren().add(astarGrid);
    }

    private void initGrid(GridPane grid) {
        grid.maxHeight(Double.POSITIVE_INFINITY);
        grid.maxWidth(Double.POSITIVE_INFINITY);
        grid.minHeight(0);
        grid.minWidth(0);
    }

    private List<NodeUI> createGridNodes(int amount) {
        final List<NodeUI> syncList = Collections.synchronizedList(new ArrayList<>(amount));
        ExecutorService threadPool = Executors.newFixedThreadPool(20);

        for (int i = 0; i < amount; i++) {
            if (i % gridSize == 0) System.out.printf("Setting up: Row %d\n", i / gridSize);
            threadPool.execute(() -> {
                NodeUI nodeUI = new NodeUI(this);
                GridPane.setHalignment(nodeUI, HPos.CENTER);
                GridPane.setValignment(nodeUI, VPos.CENTER);
                syncList.add(nodeUI);
            });
        }

        try {
            threadPool.shutdown();
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return syncList;
    }

    public void updateAstarGrid() {
        Platform.runLater(() -> {
            astar.getOpenList().forEach(n -> {
                NodeUIController nodeUI = getNodeUI(n.getPos()).getUiController();

                if (!n.getPos().equals(astar.getTo().getPos()) && !n.getPos().equals(astar.getFrom().getPos())) {
                    nodeUI.setNodeOpen();
                    nodeUI.setFCost(String.valueOf(n.getFCost()));
                    nodeUI.setGCost(String.valueOf(n.getGCost()));
                    nodeUI.setHCost(String.valueOf(n.getHCost()));
                }
            });

            astar.getClosedSet().forEach(n -> {

                if (!n.getPos().equals(astar.getTo().getPos()) && !n.getPos().equals(astar.getFrom().getPos())) {
                    NodeUIController nodeUI = getNodeUI(n.getPos()).getUiController();
                    nodeUI.setNodeClosed();
                }
            });
        });

    }

    public void updateAstarGridPath() {
        Platform.runLater(() -> {
            astar.getPath().forEach(n -> getNodeUI(n.getPos()).getUiController().setPath());
            getNodeUI(astar.getFrom().getPos()).getUiController().setAsStart();
            getNodeUI(astar.getTo().getPos()).getUiController().setAsDestination();
        });


    }

    public void setNode(AstarNode node) {

        NodeUI gridNode = getNodeUI(node.getPos());
        gridNode.getUiController().setFCost(String.valueOf(node.getFCost()));
        gridNode.getUiController().setHCost(String.valueOf(node.getHCost()));
        gridNode.getUiController().setGCost(String.valueOf(node.getGCost()));

    }

    public void setBlockUI(NodeUI node, boolean block) {
        try {
            astar.setBlock(new Vector2(GridPane.getColumnIndex(node), GridPane.getRowIndex(node)), block);
        } catch (Exception ignored) {
        }

    }

    @FXML
    private void onCalcPath() {
        astarRunning = true;
        new Thread(astar::calcPath).start();
    }

    @FXML
    private void onSetDestination() {
        btnSetStart.setSelected(false);
        setStart = false;
        setDestination = !setDestination;

    }

    @FXML
    private void onSetStart() {

        btnSetDestination.setSelected(false);
        setDestination = false;
        setStart = !setStart;
    }

    @FXML
    public void onSetBlock() {
        setBlock = !setBlock;
    }

    @FXML
    public void onRemoveAllBlocks() {
        astarGridPane.getChildren().forEach((node -> {
            NodeUI nodeUI = (NodeUI) node;
            nodeUI.getUiController().removeBlockNode();
        }));
    }

    @FXML
    public void onPrintGrid() {
        System.out.println(astar.toString());
    }

    @FXML
    public void onShowCosts() {
        final boolean show = chkboxShowCosts.isSelected();
        new Thread(() -> astarGridPane.getChildren().forEach(n -> {
            NodeUIController nodeUIController = ((NodeUI) n).getUiController();
            nodeUIController.showCosts(show);
        })).start();
    }

    @FXML
    public void onReset() {
        onRemoveAllBlocks();
        astar.getClosedSet().clear();
        astar.getOpenList().clear();
        astar.getPath().clear();
        astarGridPane.getChildren().forEach(n -> {
            NodeUIController nodeUIController = ((NodeUI) n).getUiController();
            nodeUIController.setHCost("");
            nodeUIController.setFCost("");
            nodeUIController.setGCost("");
        });

        astarRunning = false; //TODO set automatically

    }

    public boolean isAstarRunning() {
        return astarRunning;
    }

    public void setAstarRunning(boolean astarRunning) {
        this.astarRunning = astarRunning;
    }

    public boolean isSetBlocks() {
        return setBlock;
    }

    public boolean isSetStart() {
        return setStart;
    }

    public void setSetStart(boolean setStart) {
        this.setStart = setStart;
    }

    public boolean isSetDestination() {
        return setDestination;
    }

    public void setSetDestination(boolean setDestination) {
        this.setDestination = setDestination;
    }

    public AStar getAstar() {
        return astar;
    }

    public void setAstar(AStar astar) {
        this.astar = astar;
    }

    public GridPane getAstarGridPane() {
        return astarGridPane;
    }

    public void setAstarGridPane(GridPane astarGridPane) {
        this.astarGridPane = astarGridPane;
    }

    public NodeUI getNodeUI(Vector2 v) {
        return (NodeUI) astarGridPane.getChildren().get(v.getY() * gridSize + v.getX());
    }

    public double getUpdateRate() {
        return sldrSpeed.getValue();
    }


}
