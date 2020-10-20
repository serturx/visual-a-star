package model;

/**
 * This class represents a node in graph specific for this implementation of the A* Algorithm
 */
public class AstarNode {

    /**
     * Coordinate (or position) of the node in the grid
     */
    Vector2 pos;
    /**
     * String shown in the CLI (exactly 2 chars long)
     */
    private String status;
    /**
     * G Cost and H Cost combined
     */
    private int fCost;
    /**
     * Heuristic: Distance to the end node
     */
    private int hCost;
    /**
     * Distance to the start node
     */
    private int gCost;
    /**
     * Whether the node is traversable
     */
    private boolean walkable;
    /**
     * Parent (or previous) node
     */
    private AstarNode previous;

    /**
     * Node constructor
     *
     * @param pos Coordinate of the node in the grid
     */
    public AstarNode(Vector2 pos) {
        this.pos = pos;
        this.status = "  ";
        this.fCost = Integer.MAX_VALUE;
        this.hCost = Integer.MAX_VALUE;
        this.gCost = Integer.MAX_VALUE;
        this.walkable = true;
        this.previous = null;
    }

    /**
     * Checks whether a shorter path is found
     *
     * @param openedAstarNode Node which called this method
     * @param cost            Cost to move from the calling node to this
     * @return whether the path is shorter
     */
    public boolean checkShorterPath(AstarNode openedAstarNode, int cost) {
        return openedAstarNode.getGCost() + cost < gCost;
    }

    /**
     * Calculates all Costs
     *
     * @param to   Destination node
     * @param cost Cost to move to this node from the calling node
     */
    public void calcAllCosts(AstarNode to, int cost) {
        calcHCost(to);
        calcGCost(cost);
        fCost = gCost + hCost;
    }

    /**
     * Calculates the H cost
     *
     * @param to Destination node
     */
    public void calcHCost(AstarNode to) {
        hCost = (int) Math.sqrt(Math.pow(pos.getX() - to.getX(), 2) + Math.pow(pos.getY() - to.getY(), 2)) * 10;
    }

    /**
     * Calculates the G cost
     *
     * @param cost Cost to move to this node from the calling node
     */
    public void calcGCost(int cost) {

        gCost = previous.getGCost() + cost;
    }

    @Override
    public boolean equals(Object o) {
        AstarNode oAstarNode = (AstarNode) o;
        return (this.pos.getX() == oAstarNode.getX() && this.pos.getY() == oAstarNode.getY());
    }

    public AstarNode getPrevious() {
        return this.previous;
    }

    public void setPrevious(AstarNode previous) {
        this.previous = previous;
    }

    public boolean isWalkable() {
        return this.walkable;
    }

    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }

    public int getX() {
        return this.pos.getX();
    }

    public void setX(int x) {
        this.pos.setX(x);
    }

    public int getY() {
        return this.pos.getY();
    }

    public void setY(int y) {
        this.pos.setY(y);
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getFCost() {
        return this.fCost;
    }

    public void setFCost(int fCost) {
        this.fCost = fCost;
    }

    public int getHCost() {
        return this.hCost;
    }

    public void setHCost(int hCost) {
        this.hCost = hCost;
    }

    public int getGCost() {
        return this.gCost;
    }

    public void setGCost(int gCost) {
        this.gCost = gCost;
    }

    public Vector2 getPos() {
        return pos;
    }
}
