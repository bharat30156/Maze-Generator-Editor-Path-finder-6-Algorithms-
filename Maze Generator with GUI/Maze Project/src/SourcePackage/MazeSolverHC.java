package SourcePackage;

import java.util.ArrayList;
/**
 * 
 * @author bharat
 *
 */
public final class MazeSolverHC extends MazeSearch{

    private final ArrayList<MazeBox> front;
    private final boolean hc;

    MazeSolverHC(int mazeInput[][],boolean hc, Maze mazeData) {
        super(mazeInput, mazeData);
        front = new ArrayList<>();
        this.hc = hc;
        addFront(x, y);
    }
    
    @Override
    public boolean nextStep(int speed) throws InterruptedException{
        if(speed>0) Thread.sleep(speed);
        if(!front.isEmpty()){
            MazeBox box = front.get(0);
            front.remove(0);
            if(box.isVisited){
                return nextStep(0);
            }
            
            visit(box.x, box.y);
            
            if(isSolved()){
                return false;
            }
            
            double minDistance = width*height;
            double distance;
            
            if(hc){
                distance = manhattanDistance(x+1,y);
            }else{
                distance = euclideanDistance(x+1,y);
            }
            int next_x = -1, next_y = -1;
            if(distance<minDistance && validPosition(x+1,y)){
                minDistance = distance;
                next_x = x+1;
                next_y = y;
            }
            if(hc){
                distance = manhattanDistance(x,y-1);
            }else{
                distance = euclideanDistance(x,y-1);
            }
            if(distance<minDistance && validPosition(x,y-1)){
                minDistance = distance;
                next_x = x;
                next_y = y-1;
            }
            if(hc){
                distance = manhattanDistance(x,y+1);
            }else{
                distance = euclideanDistance(x,y+1);
            }
            if(distance<minDistance && validPosition(x,y+1)){
                minDistance = distance;
                next_x = x;
                next_y = y+1;
            }
            if(hc){
               distance = manhattanDistance(x-1,y); 
            }else{
                distance = euclideanDistance(x-1,y);
            }
            if(distance<minDistance && validPosition(x-1,y)){
                next_x = x-1;
                next_y = y;
            }
            addFront(next_x, next_y); 
            
            return true;
        }
        return false;
    }
 
    private int manhattanDistance(int x, int y){
        return Math.abs(x-end_x)+Math.abs(y-end_y);
    }

    private double euclideanDistance(int x, int y){
        return Math.sqrt(Math.pow(x-end_x,2)+Math.pow(y-end_y,2));
    }
    
    @Override
    protected void addFront(int x, int y){
        if(validPosition(x, y)){
            if(maze[y][x].isAdded){
                return;
            }
            maze[y][x].isAdded = true;
            if(step>0){
                maze[y][x].previous = maze[this.y][this.x];
            }
            front.add(maze[y][x]);
            int fsize = front.size();
            if(fsize>maxFront){
                maxFront = fsize;
            }
            super.addFront(x, y);
        }
    }
}