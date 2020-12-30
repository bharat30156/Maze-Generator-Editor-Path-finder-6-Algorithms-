package SourcePackage;


import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
/**
 * 
 * @author Bharat
 *
 */
public class Maze {
    
    private int rows;//rows of this maze
    private int columns;//columns of this maze
    private MazeBox[][] mazeLogic;//maze data
    private Point start;//startig poiint
    private Point goal;//goal point
    private Point current;//current point
    private ArrayList<MazeBox> solution;//current solution

    public Maze() {
        rows = 0;
        columns = 0;
        mazeLogic = null;
        start = null;
        goal = null;
        current = null;
        solution = null;
    }

    public Maze(int rows, int columns){
        this.rows = rows;
        this.columns = columns;
        mazeLogic = new MazeBox[rows][columns];
        for (int i = 0;i< rows;i++){
            for (int j = 0;j< columns;j++){
                mazeLogic[i][j] = new MazeBox();
            }
        }
        start = null;
        goal = null;
        current = null;
        solution = null;
    }

    public Maze (String path){
        this();
        try (Scanner scanner = new Scanner(new File(path))){
            int input;
            int rows = scanner.nextInt();
            int columns = scanner.nextInt();
            this.rows = rows;
            this.columns = columns;
            
            mazeLogic = new MazeBox[rows][columns];
            for (int i = 0;i< rows;i++){
                for (int j = 0;j< columns;j++){
                    mazeLogic[i][j] = new MazeBox();
                }
            }
            
            for (int i = 0;i< rows;i++){
                for (int j = 0;j< columns;j++){
                    if (scanner.hasNextInt()){
                        input = scanner.nextInt();
                        if (input == 0){
                            mazeLogic[i][j].setIsObstacle(false);
                        }
                        else if (input == 1){
                            mazeLogic[i][j].setIsObstacle(false);
                            start = new Point(i, j);
                        }
                        else if (input == 2){
                            mazeLogic[i][j].setIsObstacle(false);
                            goal = new Point(i, j);
                        }
                        else{
                            mazeLogic[i][j].setIsObstacle(true);
                        }
                                    
                        
                    }
                }
            }

            
            
        } catch (IOException e) {
            System.out.println("Input issue!");
        }
    }

    public boolean saveMaze (String path){

        try (PrintWriter printer = new PrintWriter(new FileWriter(new File(path)) {
        })) {
            
            
            printer.println(rows);
            printer.println(columns);
            
            
            
            
            for (int i = 0;i< rows;i++){
                for (int j = 0;j< columns;j++){
                    if (start != null && start.x == i && start.y == j){
                        printer.print("1 ");
                    }
                    else if (goal != null && goal.x == i && goal.y == j){
                        printer.print("2 ");
                    }
                    else if (mazeLogic[i][j].isObstacle()){
                        printer.print("3 ");
                    }
                    else{
                        printer.print("0 ");
                    }
                }
                printer.println();
            }
            
        } catch (Exception e) {
            System.out.println("Output issue!");
            return false;
        }


        return true;
    }

    public void isObstacle(int x, int y, boolean obstacle){
        mazeLogic[x][y].setIsObstacle(obstacle);
    }

    public void setStart(int x, int y){
        if (start != null){
            start.x = x;
            start.y = y;
        }
        else{
            start = new Point(x, y);
        }
    }

    public void setGoal(int x, int y){
        if (goal != null){
            goal.x = x;
            goal.y = y;
        }
        else{
            goal = new Point(x, y);
        }
    }

    public int getRows(){
        return rows;
    }

    public int getColumns(){
        return columns;
    }

    public MazeBox[][] getMazeLogic(){
        return mazeLogic;
    }
 
    public Point getStart(){
        return start;
    }

    public Point getGoal(){
        return goal;
    }

    public void setSolution(ArrayList<MazeBox> solution) {
        this.solution = solution;
    }

    public void blacken(){
        for (int i = 0;i< rows;i++){
            for (int j = 0;j< columns;j++){
                mazeLogic[i][j].setIsObstacle(true);
            }
        }
        start = null;
        goal = null;
    }
 
    public void whiten(){
        for (int i = 0;i< rows;i++){
            for (int j = 0;j<columns;j++){
                mazeLogic[i][j].setIsObstacle(false);
            }
        }
        start = null;
        goal = null;
    }
    

    
    
    public void setStart(Point newStartPoint){
        start = newStartPoint;
    }
    
    public void setGoal(Point newGoalPoint){
        goal = newGoalPoint;
    }
    
        public ArrayList<MazeBox> getSolution() {
        return solution;
    }
        
    public Point getCurrent() {
        return current;
    }

    public void setCurrent(Point current) {
        this.current = current;
    }
    
    public void copyMazeObstacles(Maze otherMaze, int iStart, int jStart){
        for (int i = 0;i< rows;i++){
            for (int j = 0;j< columns;j++){
                if (i + iStart>= otherMaze.getRows() || j + jStart>= otherMaze.getColumns() ||
                        i + iStart<0 || j + jStart< 0){
                    mazeLogic[i][j].setIsObstacle(false);
                }
                else{
                    mazeLogic[i][j].setIsObstacle(otherMaze.getMazeLogic()[i + iStart]
                        [j + jStart].isObstacle());
                }
            }
        }
        if (otherMaze.getStart() != null && start == null){
            start = new Point(otherMaze.getStart().x, otherMaze.getStart().y);
            
        }
        else if (otherMaze.getStart() == null){
            start = null;
        }
        if (otherMaze.getGoal() != null && goal == null){
            goal = new Point(otherMaze.getGoal().x, otherMaze.getGoal().y);
        }
        else if (otherMaze.getGoal() == null){
            goal = null;
        }
    }
    
    
    public void addRow(Maze oldMaze){
        rows++;
        mazeLogic = new MazeBox[rows][columns];
        for (int i = 0;i< rows;i++){
            for (int j = 0;j< columns;j++){
                mazeLogic[i][j] = new MazeBox();
            }
        }
        copyMazeObstacles(oldMaze, 0, 0);
    }
    
    public void addColumn(Maze oldMaze){
        columns++;
        mazeLogic = new MazeBox[rows][columns];
        for (int i = 0;i< rows;i++){
            for (int j = 0;j< columns;j++){
                mazeLogic[i][j] = new MazeBox();
            }
        }
        copyMazeObstacles(oldMaze, 0, 0);
    }
    
    public void removeRow(){
        Maze temp = new Maze(rows, columns);
        temp.copyMazeObstacles(this, 0, 0);
        rows--;
        mazeLogic = new MazeBox[rows][columns];
        for (int i = 0;i< rows;i++){
            for (int j = 0;j< columns;j++){
                mazeLogic[i][j] = new MazeBox();
            }
        }
        if (start!= null && start.x>= rows){
            temp.setStart(null);
            setStart(null);
        }
        if (goal != null && goal.x>= rows){
            temp.setGoal(null);
            setGoal(null);
        }
        copyMazeObstacles(temp, 0, 0);
    }
    
    public void removeColumn(){
        Maze temp = new Maze(rows, columns);
        temp.copyMazeObstacles(this, 0, 0);
        columns--;
        mazeLogic = new MazeBox[rows][columns];
        for (int i = 0;i< rows;i++){
            for (int j = 0;j< columns;j++){
                mazeLogic[i][j] = new MazeBox();
            }
        }
        if (goal != null && goal.y>= columns){
            temp.setGoal(null);
            setGoal(null);
        }
        if (start != null && start.y>= columns){
            temp.setStart(null);
            setStart(null);
        }
        copyMazeObstacles(temp, 0, 0);
    }

}
