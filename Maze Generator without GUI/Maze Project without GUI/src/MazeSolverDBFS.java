
import java.util.ArrayList;
import java.util.Collections;
/**
 * 
 * @author Bharat
 *
 */
public final class MazeSolverDBFS extends MazeSearch{

    private final ArrayList<MazeBox> front;
    private final boolean randomStep;
    private final boolean dfs;

    MazeSolverDBFS(int mazeInput[][], boolean randomStep, boolean dfs) {
        super(mazeInput);
        front = new ArrayList<>();
        this.randomStep = randomStep;
        this.dfs = dfs;
        addFront(x, y);
    }
    
    @Override
    public boolean nextStep(int speed) throws InterruptedException{
        if(speed>0) Thread.sleep(speed);
        if(!front.isEmpty()){
            MazeBox box;
            if(dfs){
                box = front.get(front.size()-1);
                front.remove(front.size()-1);
            }else{
                box = front.get(0);
                front.remove(0);
            }
            
            if(box.isVisited){
                return nextStep(0);
            }
            
            visit(box.x, box.y);
            
            if(isSolved()){
                return false;
            }
            
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
            return true;
        }
        return false;
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
