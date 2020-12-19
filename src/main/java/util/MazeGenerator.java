package util;

import model.Vector2;

import java.util.ArrayList;
import java.util.Stack;
import java.util.Random;

public class MazeGenerator {

    Vector2 size;
    Vector2 start;
    boolean[][] grid;

    public MazeGenerator(Vector2 size) {
        this.size = size;
        this.grid = new boolean[size.getY()][size.getX()];
    }

    public boolean[][] generate() {
        Random random = new Random();
        initMaze();

        /*if (size.getX() % 2 != 1 || size.getY() % 2 != 1) {
            throw new IllegalArgumentException("Maze Size must be uneven");
        }*/

        start = new Vector2(1, 1);

        Vector2 current;
        Vector2 chosen;

        ArrayList<Vector2> neighbours;
        ArrayList<Vector2> visited = new ArrayList<>();
        Stack<Vector2> stack = new Stack<>();

        stack.push(start);
        visited.add(start);

        while(!stack.isEmpty()) {
            //System.out.println("1");
            current = stack.pop();
            if((neighbours = getUnvistedNeighbours(visited, current)) != null) {
                stack.push(current);
                chosen = neighbours.get(random.nextInt(neighbours.size()));
                removeWall(current, chosen);
                visited.add(chosen);
                stack.push(chosen);
            }
        }

        return grid;
    }

    private void initMaze() {
        for(int i = 0; i < size.getY(); i++) {
            for(int j = 0; j < size.getX(); j++)  {
                if(i % 2 == 0 || j % 2 == 0 || i == size.getY() - 1 || j == size.getX() - 1) {
                    grid[i][j] = true;
                }
            }
        }
    }

    private ArrayList<Vector2> getUnvistedNeighbours(ArrayList<Vector2> visited, Vector2 v) {
        ArrayList<Vector2> neighbours = new ArrayList<>();
        if(v.getX() - 2 > 0) neighbours.add(new Vector2(v.getX() - 2, v.getY()));
        if(v.getX() + 2 < size.getX() - 1) neighbours.add(new Vector2(v.getX() + 2, v.getY()));
        if(v.getY() + 2 < size.getY() - 1) neighbours.add(new Vector2(v.getX(), v.getY() + 2));
        if(v.getY() - 2 > 0) neighbours.add(new Vector2(v.getX(), v.getY() - 2));
        neighbours.removeIf(visited::contains);
        return neighbours.size() == 0 ? null : neighbours;
    }

    private void removeWall(Vector2 v1, Vector2 v2) {
        grid[(v1.getY() + v2.getY()) / 2][(v1.getX() + v2.getX()) / 2] = false;
    }


}
