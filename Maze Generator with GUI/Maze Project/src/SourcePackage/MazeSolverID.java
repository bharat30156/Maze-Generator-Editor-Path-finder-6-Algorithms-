package SourcePackage;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
/**
 * 
 * @author Bharat
 *
 */
public final class MazeSolverID extends MazeSearch{

    private final ArrayList<MazeBox> front;
    private final boolean randomStep;
    private final int depthStep; // search depth step >=1
    private final int start_x, start_y; // start position
    private int depth; // current maximum search depth
    private int[][] depths; // MazeBoxes depths
    private boolean hasMore; // to check if there are more boxes in more depth

    MazeSolverID(int mazeInput[][], boolean randomStep, int depth, Maze aMaze) {
        super(mazeInput,aMaze);
        front = new ArrayList<>();
        this.randomStep = randomStep;
        this.depthStep = depth;
        start_x = x;
        start_y = y;
        if(depth<1){
            return;
        }
    }

    @Override

    public boolean nextStep(int speed) throws InterruptedException{
        if(speed>0) Thread.sleep(speed);
        if(isSolved()){
            return false;
        }
        if(!front.isEmpty()){
            MazeBox box = front.get(front.size()-1);
            front.remove(front.size()-1);
            while(box.isVisited && !front.isEmpty()){
                box = front.get(front.size()-1);
                front.remove(front.size()-1);
            }
            
            visit(box.x, box.y);
            
            ArrayList<Integer> directions = new ArrayList<>();
            directions.add(0); // right
            directions.add(1); // top
            directions.add(2); // bottom
            directions.add(3); // left
            if(randomStep){
                Collections.shuffle(directions);
            }
            
            for(int i=0;i<4;i++){
                int direction = directions.get(i);
                if(direction==0) addFront(x+1, y);
                else if(direction==1) addFront(x, y-1);
                else if(direction==2) addFront(x, y+1);
                else addFront(x-1, y);
            }
            
            return !front.isEmpty() || hasMore;
        }else{
            hasMore = false;
            depth += depthStep;
            depths = new int[height][width];
            for(int i=0;i<height;i++){
                for(int j=0;j<width;j++){
                    maze[i][j].isVisited = false;
                    maze[i][j].isAdded = false;
                    // code for GUI - make i, j MazeBox not visited
                    mazeData.getMazeLogic()[i][j].setIsFront(false);
                    mazeData.getMazeLogic()[i][j].isVisited = false;
                    maze[i][j].previous = null;
                }
            }
            addFront(start_x, start_y);
            return nextStep(speed);
        }
    }

    @Override
    protected boolean visit(int x, int y){
        if(!validPosition(x, y)){
            return false;
        }
        if(maze[y][x].previous!=null){
            depths[y][x] = depths[maze[y][x].previous.y][maze[y][x].previous.x]+1;
        }
        this.x = x;
        this.y = y;
        maze[y][x].isVisited = true;
        step++;
        // Code for GUI
        mazeData.setCurrent(new Point(y, x));
        mazeData.getMazeLogic()[y][x].isVisited = true;
        mazeData.getMazeLogic()[y][x].setIsFront(false);
        return true;
    }
    
     @Override
    protected void addFront(int x, int y){
        if(validPosition(x, y)){
            if(maze[y][x].isAdded){
                return;
            }
            maze[y][x].isAdded = true;
            if(depths[this.y][this.x]<depth){
                if(x!=start_x||y!=start_y){
                    maze[y][x].previous = maze[this.y][this.x];
                }
                front.add(maze[y][x]);
                int fsize = front.size();
                if(fsize>maxFront){
                    maxFront = fsize;
                }
                super.addFront(x, y);
            }else{
                hasMore = true;
            }
        }
    }
}

