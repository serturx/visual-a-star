package gui.controller;

import gui.java.NodeUI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.NodeType;
import model.Vector2;
import util.AStar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


//TODO allow rectangle grid
//TODO set icon
//TODO multithread grid render


public class MainUIController {
    final private int DEFAULT_GRID_SIZE = 30;
    private AStar astar;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private ToggleButton btnSetStart;
    @FXML
    private ToggleButton btnSetDestination;
    @FXML
    private ToggleButton btnEditBlocks;
    @FXML
    private Slider sldrSpeed;
    @FXML
    private CheckBox chkboxShowCosts;
    @FXML
    private Spinner spnrGridSize;
    @FXML
    private CheckBox chkboxAllowDiagonals;

    private int gridSize;
    private GridPane astarGridPane;
    private boolean astarRunning;
    private boolean settingStart;
    private boolean settingDestination;
    private boolean settingBlock;
    private Thread astarThread;
    private boolean astarThreadSleeping;
    private CountDownLatch astarWaitForPlay;
    private boolean startSet;
    private boolean destinationSet;

    public MainUIController() {
        this.gridSize = DEFAULT_GRID_SIZE;
        this.settingBlock = false;
        this.settingStart = false;
        this.settingDestination = false;
    }

    @FXML
    public void initialize() {
        spnrGridSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 200));
        ToggleGroup group = new ToggleGroup();
        btnSetStart.setToggleGroup(group);
        btnSetDestination.setToggleGroup(group);
        btnEditBlocks.setToggleGroup(group);
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

        //grid "anchored" to the origin as the grid needs to be stretched properly when the windows gets resized
        AnchorPane.setTopAnchor(astarGrid, 0d);
        AnchorPane.setLeftAnchor(astarGrid, 0d);
        AnchorPane.setRightAnchor(astarGrid, 0d);
        AnchorPane.setBottomAnchor(astarGrid, 40d);
        rootPane.getChildren().add(astarGrid);
    }

    private void initGrid(GridPane grid) {
        grid.maxHeight(Double.POSITIVE_INFINITY);
        grid.maxWidth(Double.POSITIVE_INFINITY);
        grid.minHeight(200);
        grid.minWidth(200);
    }

    private List<NodeUI> createGridNodes(int amount) {

        ExecutorService threadPool = Executors.newFixedThreadPool(20);

        final List<NodeUI> syncList = Collections.synchronizedList(new ArrayList<>(amount));

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
                }

            });

            new HashSet<>(astar.getClosedSet()).forEach(n -> {

                if (!n.getPos().equals(astar.getTo().getPos()) && !n.getPos().equals(astar.getFrom().getPos())) {
                    NodeUIController nodeUI = getNodeUI(n.getPos()).getUiController();
                    nodeUI.setNodeClosed();
                }
            });
        });
    }

    /**
     * Updates the UI Grid based on the astar Grid
     */

    public void updateAstarGridPath() {
        new Thread(() -> {
            astar.getPath().forEach(n -> {

                Platform.runLater(() -> getNodeUI(n.getPos()).getUiController().setPath());

                try {
                    Thread.sleep((long) getUpdateRate());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            Platform.runLater(() -> {
                getNodeUI(astar.getFrom().getPos()).getUiController().setAsStart();
                getNodeUI(astar.getTo().getPos()).getUiController().setAsDestination();
            });
        }).start();
    }

    /**
     * Sets/Removes a block on the specified UI node
     *
     * @param node  node to set
     * @param block whether to set a block
     */
    public void setBlock(NodeUI node, boolean block) {
        try {
            astar.setBlock(new Vector2(GridPane.getColumnIndex(node), GridPane.getRowIndex(node)), block);
        } catch (Exception ignored) {
        }

    }

    @FXML
    private void onCalcPath() {
        if (astarRunning || !startSet || !destinationSet) return;
        astar.setAllowDiagonal(chkboxAllowDiagonals.isSelected());
        astarThreadSleeping = false;
        astarRunning = true;
        astarThread = new Thread(astar::calcPath);
        astarThread.start();

    }

    @FXML
    private void onSetDestination() {
        destinationSet = true;
        settingDestination = !settingDestination;
        settingStart = false;
        settingBlock = false;
    }

    @FXML
    private void onSetStart() {
        destinationSet = true;
        settingStart = !settingStart;
        settingDestination = false;
        settingBlock = false;
    }

    @FXML
    public void onSetBlock() {
        settingBlock = !settingBlock;
        settingStart = false;
        settingDestination = false;
    }

    @FXML
    public void onPrintGrid() {

        astar.updateNodeCostStatus();
        System.out.println(astar.toString());
    }

    @FXML
    public void onShowCosts() {
        new Thread(() -> astarGridPane.getChildren().forEach(n -> {
            NodeUIController nodeUIController = ((NodeUI) n).getUiController();
        })).start();
    }

    @FXML
    public void onReset() {
        if (!astar.getClosedSet().isEmpty()) {
            destinationSet = false;
            startSet = false;
            astarThread = null;
            astarRunning = false;
            astarThreadSleeping = false;
            this.astar = new AStar(gridSize, new Vector2(0, 0), new Vector2(1, 1), this);

            astarGridPane.getChildren().forEach((node -> {
                NodeUI nodeUI = (NodeUI) node;
                nodeUI.getUiController().removeBlock();
            }));
        }
    }

    @FXML
    public void onPausePlay() {
        if (astarRunning) {
            if (astarThreadSleeping) {
                astarWaitForPlay.countDown();
                astarThreadSleeping = false;
            } else {
                astarWaitForPlay = new CountDownLatch(1);
                astar.setWaiting(astarWaitForPlay);
                astarThreadSleeping = true;
            }
        }
    }

    @FXML
    public void onApplyGridSize() {
        onReset();
        setGridSize((Integer) spnrGridSize.getValue());
    }

    @FXML
    public void onClearBlocks() {
        if (isAstarRunning()) return;

        astarGridPane.getChildren().forEach(n -> {
            NodeUI nodeUI = (NodeUI) n;
            if (nodeUI.getUiController().getNodeType() == NodeType.BLOCK) {
                nodeUI.getUiController().removeBlock();
            }
        });

    }

    public boolean isAstarRunning() {
        return astarRunning;
    }

    public void setAstarRunning(boolean astarRunning) {
        this.astarRunning = astarRunning;
    }

    public boolean settingBlocks() {
        return settingBlock;
    }

    public boolean settingStart() {
        return settingStart;
    }

    public void setSettingStart(boolean settingStart) {
        this.settingStart = settingStart;
    }

    public boolean settingDestination() {
        return settingDestination;
    }

    public void setSettingDestination(boolean settingDestination) {
        this.settingDestination = settingDestination;
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

    public void calcPathFinished() {
        this.astarRunning = false;
    }

    public boolean isStartSet() {
        return startSet;
    }

    public void setStartSet(boolean startSet) {
        this.startSet = startSet;
    }

    public boolean isDestinationSet() {
        return destinationSet;
    }

    public void setDestinationSet(boolean destinationSet) {
        this.destinationSet = destinationSet;
    }
}
