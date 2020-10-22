package util;

import gui.controller.MainUIController;
import model.AstarNode;
import model.Vector2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * This clas is an implementation of the A* Algorithm which is an extension of the Dijkstra Algorithm. A* finds the
 * short path between two nodes in a graph. In contrast to the Dijkstra Algorithm, it has an additonal cost to
 * calculate: the H Cost, a heuristic which is the distance between the current node and the destination node in this
 * example. For more information: https://en.wikipedia.org/wiki/A*_search_algorithm
 *
 * @author serturx
 */
public class AStar {

    /**
     * Default diagonal moving cost
     */
    private static final int DIAG_COST = 14;
    /**
     * Default horizontal and vertical move cost
     */
    private static final int DEF_COST = 10;

    private static final double H_COST_WEIGHT = 1.01;
    /**
     * List with all found nodes, the one with the lowest f cost is at the top
     */
    private final PriorityBlockingQueue<AstarNode> openList;
    /**
     * All closed nodes (closed meaning all it's neighbours have been added)
     */
    private final Set<AstarNode> closedSet;
    /**
     * Quadratic Grid containing all nodes
     */
    private final AstarNode[][] grid;
    /**
     * List containing the best path after it has been calculated
     */
    private final ArrayList<AstarNode> path;
    private final MainUIController uiController;
    /**
     * Starting node
     */
    private AstarNode from;
    /**
     * Destination node
     */
    private AstarNode to;
    /**
     * Total cost of the calculated path
     */
    private int totalCost;
    /**
     * Whether to keep track of the algorithms steps
     */
    private boolean trackSteps;
    /**
     * Contains all algorithm steps
     */
    private ArrayList<String> steps;
    private boolean allowDiagonal = true;
    private CountDownLatch waiting;

    /**
     * AStar Constructor
     *
     * @param size Size of the grid
     * @param from Starting node coordinates
     * @param to   Destination node coordinates
     */
    public AStar(int size, Vector2 from, Vector2 to, MainUIController uiController) {

        // Checks whether the given coordinates are in the grid
        if (!from.allInRange(0, size) || !to.allInRange(0, size)) {
            throw new IllegalArgumentException("Start or Destination Node out of Bounds");
        }

        this.grid = new AstarNode[size][size];

        // Fills the array with nodes
        for (int i = 0; i < this.grid.length; i++) {
            for (int j = 0; j < this.grid.length; j++) {
                this.grid[i][j] = new AstarNode(new Vector2(j, i));
            }
        }

        // Sets the rest of the members up
        this.from = grid[from.getY()][from.getX()];
        this.to = grid[to.getY()][to.getX()];
        this.path = new ArrayList<>();
        this.openList =
                new PriorityBlockingQueue<>(1, Comparator.comparingInt(AstarNode::getFCost));
        this.closedSet = ConcurrentHashMap.newKeySet();
        this.trackSteps = false;
        this.totalCost = Integer.MAX_VALUE;
        this.uiController = uiController;
        setStartDestStatus();
    }

    /**
     * Additional Constructor if steps should be tracked
     *
     * @param size       Size of the grid
     * @param from       Starting node coordinates
     * @param to         Destionation node coordinates
     * @param trackSteps Whether to track steps
     */
    public AStar(int size, Vector2 from, Vector2 to, boolean trackSteps, MainUIController uiController) {
        this(size, from, to, uiController);
        this.trackSteps = trackSteps;
        this.steps = new ArrayList<>();
    }

    public static int getDiagCost() {
        return DIAG_COST;
    }

    public static int getDefCost() {
        return DEF_COST;
    }

    public static double gethCostWeight() {
        return H_COST_WEIGHT;
    }

    /**
     * Calculates the shortest path between the two given nodes
     */
    public void calcPath() {

        this.from.setFCost(0);
        this.from.setGCost(0);
        this.from.setHCost(0);

        boolean found = false;
        // add the starting node
        openList.add(from);

        // While there are nodes to discover
        while (!openList.isEmpty()) {
            if (trackSteps) {
                updateNodeCostStatus();
            }

            // takes the node with the lower f Cost
            AstarNode current = openList.poll();
            closedSet.add(current);

            if (current == null) {
                throw new NullPointerException("Fatal Error: Null pointer on current node");
            }
            // if the taken node is the destination node, a path has been found
            if (current.equals(getTo())) {

                found = true;
                totalCost = current.getFCost();
                backTracePath(current);
                break;
            } else {
                // otherwise just add all neighbouring nodes to the open list
                addNeighbours(current);

                try {
                    Thread.sleep((long) uiController.getUpdateRate());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                uiController.updateAstarGrid();

                if (waiting != null) {
                    try {
                        waiting.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (found) {
            setFinalPathStatus();
            uiController.updateAstarGridPath();
        } else if (uiController.isAstarRunning()) {
            System.out.println("\n No Path Found!");
        }
    }

    /**
     * Adds all neighbouring nodes to the open list
     *
     * @param current opened node
     */
    public void addNeighbours(AstarNode current) {
        int currentX = current.getX();
        int currentY = current.getY();

        if (allowDiagonal) {
            for (int i = currentY - 1; i <= currentY + 1; i++) {
                if (i < 0 || i >= grid.length) {
                    continue;
                }

                for (int j = currentX - 1; j <= currentX + 1; j++) {
                    if (j < 0 || j >= grid.length) {
                        continue;
                    }

                    if (!(currentX == j && currentY == i)) {
                        addNeighbourNode(
                                current, grid[i][j], isDiagonal(current, grid[i][j]) ? DIAG_COST : DEF_COST);
                    }
                }
            }
        } else {

            if (isInBounds(currentX + 1, currentY)) addNeighbourNode(current, grid[currentY][currentX + 1], DEF_COST);
            if (isInBounds(currentX - 1, currentY)) addNeighbourNode(current, grid[currentY][currentX - 1], DEF_COST);
            if (isInBounds(currentX, currentY + 1)) addNeighbourNode(current, grid[currentY + 1][currentX], DEF_COST);
            if (isInBounds(currentX, currentY - 1)) addNeighbourNode(current, grid[currentY - 1][currentX], DEF_COST);

        }
    }

    /**
     * Traces the path back
     *
     * @param current opened node
     */
    private void backTracePath(AstarNode current) {

        if (current != null) {
            path.add(current);
            backTracePath(current.getPrevious());
        }
    }

    /**
     * Adds a neighbouring node or updates its cost if its already in the list
     *
     * @param current   opened node
     * @param neighbour neighbouring node to add
     * @param cost      cost of moving from the opened node to the neighouring node
     */
    public void addNeighbourNode(AstarNode current, AstarNode neighbour, int cost) {
        if (neighbour.isWalkable() && !closedSet.contains(neighbour)) {
            // if the neighbouring node hasn't been added yet add it and calculate its cost
            // otherwise if the found path to the node is shorter than the previous one,
            // update the cost and set its parent node to the just opened node
            if (!openList.contains(neighbour) || neighbour.checkShorterPath(current, cost)) {
                neighbour.setPrevious(current);
                neighbour.calcAllCosts(to, cost);
                if (!openList.contains(neighbour)) {
                    openList.add(neighbour);
                }
            }
        }
    }

    /**
     * Checks whether the neighbouring node is diagonal relative to the other node
     *
     * @param o1 Node
     * @param o2 other Node
     * @return Whether the node are diagonal to eachother
     */
    public boolean isDiagonal(AstarNode o1, AstarNode o2) {
        return Math.abs(o1.getX() - o2.getX()) == 1 && Math.abs(o1.getY() - o2.getY()) == 1;
    }

    /**
     * Sets the status of all nodes on the path to reflect that the path goes through them and adds it to the steps list
     */
    public void setFinalPathStatus() {
        path.forEach(astarNode -> astarNode.setStatus("~~"));

        setStartDestStatus();

        if (trackSteps) {
            steps.add(this.toString());
        }

    }

    /**
     * Updates all status on found nodes, shows their cost if they haven't been closed yet and adds it to the steps list
     */
    public void updateNodeCostStatus() {
        closedSet.forEach(astarNode -> astarNode.setStatus(String.valueOf(astarNode.getFCost())));

        openList.forEach(astarNode -> astarNode.setStatus(String.valueOf(astarNode.getFCost())));

        setStartDestStatus();

        if (trackSteps) steps.add(this.toString());

    }

    public void setStartDestStatus() {
        grid[to.getY()][to.getX()].setStatus("FI");
        grid[from.getY()][from.getX()].setStatus("ST");
    }

    /**
     * Sets an impassable node
     *
     * @param v coordinate of the block to set
     */
    public void setBlock(Vector2 v, boolean block) {
        if (block && ((v.getX() == from.getX() && v.getY() == from.getY()) || (v.getX() == to.getX() && v.getY() == to.getY()))) {
            throw new IllegalArgumentException("Node to set as block is either the start or destination node");
        } else {
            if (v.getX() >= grid.length || v.getY() >= grid.length) {
                throw new IllegalArgumentException("Node to set as block is out of bounds");
            }
        }

        grid[v.getY()][v.getX()].setWalkable(!block);
        grid[v.getY()][v.getX()].setStatus(block ? "||" : "  ");

    }

    public void toggleBlock(Vector2 v) {
        setBlock(v, !grid[v.getY()][v.getX()].isWalkable());
    }

    /**
     * Sets a given amount of nodes impassable randomly
     *
     * @param amount amount of blocks to add
     */
    public void setRandomBlocks(int amount) {
        Random r = new Random();

        if (amount >= Math.pow(grid.length, 2)) {
            throw new IllegalArgumentException("Exception: Amount >= Total Grid Nodes");
        }

        while (amount > 0) {

            int x = r.nextInt(grid.length - 1);
            int y = r.nextInt(grid.length - 1);
            if (!(grid[y][x].equals(from) || grid[y][x].equals(to)) && grid[y][x].isWalkable()) {
                grid[y][x].setWalkable(false);
                grid[y][x].setStatus("||");
                amount--;
            }
        }
    }

    /**
     * Sets blocks according to a 2D boolean array true meaning the underlying node should not be walkable e.g. if
     * input[2][3] is true the node at x: 3 and y: 2 will be set as unwalkable
     *
     * @param input input array
     */
    public void setBlocks(boolean[][] input) {
        if (input.length != grid.length || Arrays.stream(input).anyMatch(i -> i.length != grid.length)) {
            throw new IllegalArgumentException(String.format("Input array must be size %dx%d", grid.length, grid.length));
        }

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input.length; j++) {
                if (input[i][j]) {
                    setBlock(new Vector2(j, i), false);
                }
            }
        }
    }

    private boolean isInBounds(int x, int y) {
        return (x > 0 && x < grid[0].length && y > 0 && y < grid.length);
    }

    /**
     * Returns a simple visualization of the Grid
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        // Column indeces
        for (int i = 0; i < grid.length; i++) {
            if (i == 0) {
                sb.append("      00 ");
            } else {
                if (i > 9) {
                    sb.append(String.format("%d ", i));
                } else {
                    sb.append(String.format("0%d ", i));
                }
            }
        }

        sb.append("\n");

        sb.append("-".repeat(grid.length * 3 + 5));

        sb.append("\n");

        // Node prints
        for (int i = 0; i < grid.length; i++) {

            sb.append(String.format(" %s |", i < 10 ? "0" + i : i));

            for (int j = 0; j < grid[0].length; j++) {
                String status = grid[i][j].getStatus();
                sb.append(" ").append(status);
            }

            sb.append("\n");
        }

        sb.append("-".repeat(grid.length * 3 + 5));

        return sb.toString();
    }

    public int getTotalCost() {
        return this.totalCost;
    }

    public AstarNode[][] getGrid() {
        return this.grid;
    }

    public AstarNode getFrom() {
        return this.from;
    }

    public void setFrom(AstarNode from) {
        this.from = from;
    }

    public void setFrom(Vector2 v) {
        this.from.setStatus("  ");
        this.from = grid[v.getY()][v.getX()];
        setStartDestStatus();
    }

    public AstarNode getTo() {
        return this.to;
    }

    public void setTo(AstarNode to) {
        this.to = to;
    }

    public void setTo(Vector2 v) {
        this.to.setStatus("  ");
        this.to = grid[v.getY()][v.getX()];
        setStartDestStatus();
    }

    public ArrayList<String> getSteps() {
        return this.steps;
    }

    public void setNode(AstarNode toSet) {
        grid[toSet.getY()][toSet.getX()] = toSet;
    }

    public AstarNode getNode(Vector2 coordinates) {
        return grid[coordinates.getY()][coordinates.getX()];
    }

    public PriorityBlockingQueue<AstarNode> getOpenList() {
        return openList;
    }

    public AstarNode getNode(int x, int y) {
        return grid[y][x];
    }

    public Set<AstarNode> getClosedSet() {
        return closedSet;
    }

    public ArrayList<AstarNode> getPath() {
        return path;
    }

    public CountDownLatch getWaiting() {
        return waiting;
    }

    public void setWaiting(CountDownLatch waiting) {
        this.waiting = waiting;
    }

    public boolean getAllowDiagonal() {
        return allowDiagonal;
    }

    public void setAllowDiagonal(boolean allowDiagonal) {
        this.allowDiagonal = allowDiagonal;
    }
}
