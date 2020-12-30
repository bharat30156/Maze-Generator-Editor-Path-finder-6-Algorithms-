package SourcePackage;





import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 
 * @author Bharat
 *
 */
public abstract class MazeSearch {
    
    protected int x, y; // current position
    protected int end_x, end_y; // end position
    protected MazeBox[][] maze; // the maze boxes
    protected int width, height; // maze dimensions
    protected int step; // solver step
    protected ArrayList<MazeBox> solution; // maze solution
    protected int maxFront; // max front set size
    protected Maze mazeData;
    
    
    MazeSearch(int[][] mazeInput, Maze mazeData){
        x= -1;
        y = -1;
        end_x = -1;
        end_y = -1;
        maxFront = 0;
        solution = new ArrayList<>();
        step = 0;
        width = mazeInput[0].length;
        height = mazeInput.length;
        maze = new MazeBox[height][width];
        this.mazeData = mazeData;
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                maze[i][j] = new MazeBox();
                maze[i][j].setIsObstacle(mazeInput[i][j]==3);
                maze[i][j].x = j;
                maze[i][j].y = i;
                if(mazeInput[i][j]==1){
                    x = j;
                    y = i;
                }else if(mazeInput[i][j]==2){
                    end_x = j;
                    end_y = i;
                }
            }
        }
    }
    
    
    public abstract boolean nextStep(int speed) throws InterruptedException;
    
    
    public int getSteps(){
        return step;
    }
    
    
    public int getMaxFront(){
        return maxFront;
    }
    
    
    protected boolean visit(int x, int y){
        if(!validPosition(x, y)){
            return false;
        }
        this.x = x;
        this.y = y;
        maze[y][x].isVisited = true;
        step++;
        //GUI
        mazeData.getMazeLogic()[y][x].isVisited = true;
        mazeData.getMazeLogic()[y][x].setIsFront(false);
        mazeData.setCurrent(new Point(y, x));
        return true;
    }
    
  
    protected boolean validPosition(int x, int y){
        return x>=0 && x<width && y>=0 && y<height && !maze[y][x].isObstacle();
    }
    
   
    protected void addFront(int x, int y){
        //GUI
        mazeData.getMazeLogic()[y][x].setIsFront(true);
    }
    
  
    public ArrayList<MazeBox> getSolution(){
        solution.clear();
        if(step==0) return null;
        MazeBox box = maze[y][x];
        int c = 0;
        while(c<2){
            solution.add(box);
            if(box!=null) box = box.previous;
            if(box==null || box.previous==null){
                c++;
            }
        }
        Collections.reverse(solution);
        return solution;
    }
    
    
    public ArrayList<MazeBox> solve(int speed) throws InterruptedException{
        while(nextStep(speed)){
            // continue tree search
        }
        if(isSolved()) return getSolution();
        return null;
    }
    
 
    public boolean isSolved(){     
        return x==end_x && y==end_y;
    }
}
