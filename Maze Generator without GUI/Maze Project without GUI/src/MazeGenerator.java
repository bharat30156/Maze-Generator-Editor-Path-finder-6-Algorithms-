
import java.util.ArrayList;
import java.util.Collections;

public final class MazeGenerator{

    private int x, y; // current position
    private int start_x, start_y; // start position
    private int end_x, end_y; // end position
    private MazeBox[][] maze; // the maze boxes
    private final ArrayList<MazeBox> front;
    private final int width, height; // maze dimensions
    private int step; // generator step
    private final boolean classic; // if the generated maze is classic
    private final int vstep; // visit step, 2: classic, 1: randomized

    MazeGenerator(int width, int height, boolean classic) {
        this.classic = classic;
        if(classic){
            vstep = 2;
        }
        else{
            vstep = 1;
        }
        step = 0;
        end_x = -1;
        end_y = -1;
        if(width%2==0){
            width++;
        }
        if(height%2==0){
            height++;
        }
        this.width = width;
        this.height = height;
        front = new ArrayList<>();
        if(width<4 || height<4){
            // code for GUI - invalid dimensions
            return;
        }
        maze = new MazeBox[height][width];
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                maze[i][j] = new MazeBox();
                maze[i][j].x = j;
                maze[i][j].y = i;
            }
        }
        start_x = rand(width-1);
        start_y = rand(height-1);
        x = start_x;
        y = start_y;
        addFront(start_x, start_y);
    }

    public int getSteps(){
        return step;
    }

    private int rand(int to){
        if(classic) return 1+2*(int)(Math.random()*((to-1)/2+1));
        if(to % 2 == 0) to--;
        return (int)(Math.round(Math.random()*to));
    }

    public boolean nextStep(int speed) throws InterruptedException{
        if(speed>0) Thread.sleep(speed);
        if(!front.isEmpty()){
            int k;
            k = front.size()-1; // DFS generation
            MazeBox box = front.get(k);
            front.remove(k);
            if(!canVisit(box.x, box.y)){
                return nextStep(0);
            }
            visit(box.x, box.y);
            
            ArrayList<Integer> directions = new ArrayList<>();
            directions.add(0); // right
            directions.add(1); // top
            directions.add(2); // bottom
            directions.add(3); // left
            
            Collections.shuffle(directions);
            
            for(int i=0;i<4;i++){              
                int direction = directions.get(i);
                if(direction==0) addFront(x+vstep, y);
                else if(direction==1) addFront(x, y-vstep);
                else if(direction==2) addFront(x, y+vstep);
                else addFront(x-vstep, y);
            }
            return true;
        }
        out1:
        for(int i=height-1;i>=height-3;i--){
            for(int j=0;j<=2;j++){
                if(!maze[i][j].isObstacle){
                    start_x = j;
                    start_y = i;
                    break out1;
                }
            }
        }
        out2:
        for(int i=0;i<3;i++){
            for(int j=width-1;j>=width-3;j--){
                if(!maze[i][j].isObstacle){
                    end_x = j;
                    end_y = i;
                    break out2;
                }
            }
        }
        return false;
    }

    private boolean visit(int x, int y){
        if(visited(x, y)){
            return false;
        }
        if(classic && maze[y][x].previous!=null){
            int wx, wy;
            wx = (maze[y][x].previous.x+x)/2; // wall x
            wy = (maze[y][x].previous.y+y)/2; // wall y
            maze[wy][wx].isObstacle = false;
            // Code for GUI - make wx, wy white
        }
        this.x = x;
        this.y = y;
        maze[y][x].isVisited = true;
        maze[y][x].isObstacle = false;
        step++;
        // Code for GUI - make x, y white
        return true;
    }

    private boolean visited(int x, int y){
        return x>=0 && x<width && y>=0 && y<height && maze[y][x].isVisited;
    }

    private boolean canVisit(int x, int y){        
        if(classic) return x>=0 && x<width && y>=0 && y<height && !maze[y][x].isVisited;
        
        int visitedNeighbors = 0;
        if(visited(x+1, y)) visitedNeighbors++; // right
        if(visited(x, y-1)) visitedNeighbors++; // top
        if(visited(x, y+1)) visitedNeighbors++; // bottom
        if(visited(x-1, y)) visitedNeighbors++; // left
        
        return x>=0 && x<width && y>=0 && y<height && !maze[y][x].isVisited && visitedNeighbors<2;
    }

    private int addFront(int x, int y){
        if(canVisit(x, y)){
            front.add(maze[y][x]);
            maze[y][x].previous = maze[this.y][this.x];
            // code for GUI
            return 1;
        }
        return 0;
    }

    public int[][] getMaze(){
        int out[][] = new int[height][width];
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                if(maze[i][j].isObstacle) out[i][j] = 3;
                else if(j==start_x && i==start_y) out[i][j] = 1;
                else if(j==end_x && i==end_y) out[i][j] = 2;
                else out[i][j] = 0;
            }
        }
        return out;
    }
    
    public int[][] generate(int speed) throws InterruptedException{
        while(nextStep(speed)){
            // continue generation
        }
        return getMaze();
    }
}
