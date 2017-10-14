/**
 * A1Q2.java
 *
 * COMP 3190 SECTION [A01]
 * INSTRUCTOR: John Braico
 * NAME: Jeremy Arde
 * ASSIGNMENT: Assignment 1
 * QUESTION: Q2
 *
 * PURPOSE: Reads in a maze from stdin, and determines the best path 
 * to collecting flags from a specific starting location.
 */

import java.io.*;
import java.util.*;

public class A1Q2
{
  public static void main(String[] args) 
  {
    String inputLine;                    //store the first line of the file
    
    Scanner scanner = new Scanner(System.in);
    int height = scanner.nextInt();
    int width = scanner.nextInt();
    
    int[][] inputMaze = new int[height][width];
    
    
    //Read the input lines one by one, char by char and translate into integer 2d array
    for (int h = 0; h< height; h++)
    {
      inputLine = scanner.next();       //get the next line in the input
      for (int w = 0; w < width; w++)   //loop through all the words in the tokenized line
      {
        if ( inputLine.charAt(w) == '#')
        {
          inputMaze[h][w] = Maze.Tile.WALL.getValue();
        }
        else if ( inputLine.charAt(w) == '!')
        {
          inputMaze[h][w] = Maze.Tile.FLAG.getValue();
        }
        else if ( inputLine.charAt(w) == '.')
        {
          inputMaze[h][w] = Maze.Tile.OPEN.getValue();
        }
        else if ( inputLine.charAt(w) == 'h' )
        {
          inputMaze[h][w] = Maze.Tile.HERO.getValue();
        }
      }
    }
    
    scanner.close();
    
    Maze maze = new Maze(inputMaze);  //create the initial maze object
    
    Maze result = AStarHelper(maze);  //return a new maze after using the algorithm on the starting state
    
    System.out.println("\n\n\n------- Solution: -------");
    result.print();                   //print out the final resulting maze
    
    int cost = result.getCost();      //get the moves count
    
    System.out.println("Total moves in solution: "+result.countMoves()+"\nTotal moves considered: "+cost);
  }
  
  /*
   * METHOD: testCheckList: Takes a maze object and tests wether the checkList method returns the correct value
   * INPUT: maze object to test
   */
  public static void testCheckList(Maze maze)
  {
    ArrayList<Maze> children = maze.generateChildren();
    boolean testB = checkLists(children, maze);
    System.out.println("testCheckList result: "+testB);
    
    for (int i = 0; i< 10; i++)
    {
      maze.moveHero(1);
    }
    
    children = maze.generateChildren();
    System.out.println("Children in test: "+children);
    System.out.println(children.get(0).toString());
    System.out.println(children.get(0).toString());
    testB = checkLists(children, maze);
    System.out.println("testCheckList result: "+testB);
  }
  
  /*
   * METHOD: testMove: tests moving the hero around the maze
   * INPUT: maze object to test
   */
  public static void testMove(Maze maze)
  {
    //move the hero to the right
    for (int i = 0; i< 10; i++)
    {
      maze.moveHero(1);
      maze.print();
    }
    
    for (int i = 0; i< 10; i++)     //move the hero up!
      
    {
      maze.moveHero(0);
      maze.print();
    }
  }
  
  /*
   * METHOD: testChildrenGeneration: takes an initial maze state and creates the children and prints them out
   * INPUT:maze object to test
   */
  public static ArrayList<Maze> testChildrenGeneration(Maze maze)
  {
    ArrayList<Maze> children = maze.generateChildren();
    
    System.out.println("\n*************TESTING CHILDREN GENERATION***************");
    System.out.println(children.toString());
    
    return children;
  }
  
  /*
   * METHOD: testEucDist: find out if the euclidean distance values are correct
   */
  public static void testEucDist()
  {
    Loc d1 = new Loc(0, 0);
    Loc d2 = new Loc(1, 1);
    Loc d3 = new Loc(6, 9);
    
    System.out.println("d1-d1: "+d1.euclideanDistance(d1));
    System.out.println("d1-d2: "+d1.euclideanDistance(d2));
    System.out.println("d1-d3: "+d1.euclideanDistance(d3));
    System.out.println("d2-d1: "+d2.euclideanDistance(d1));
    System.out.println("d2-d2: "+d2.euclideanDistance(d2));
    System.out.println("d2-d3: "+d2.euclideanDistance(d3));
    System.out.println("d3-d1: "+d3.euclideanDistance(d1));
    System.out.println("d3-d2: "+d3.euclideanDistance(d2));
    System.out.println("d3-d3: "+d3.euclideanDistance(d3));
  }
  
  /*
   * METHOD: AStarHelper: helper method for AStar where we find out the closest flag to the current position, and pass the current maze and location to get to into AStar until all of the flags are found
   * INPUT: initial starting maze state
   * OUTPUT: resulting maze after finding all of the possible flags
   * PARAM: Maze maze
   * RETURN: Maze maze (result from AStar)
   */
  public static Maze AStarHelper(Maze maze)
  {
    //calculate distance to each of the flags from heroLoc
    //perform AStar search from hero to closest flag
    //if null delete that flag (unreachable)
    
    while (maze.flagsLeft() != 0)   //Keep using the A star algorithm while there are flags to find
    {
      int closestFlagPos = maze.getClosestFlagLoc();
      Loc closestFlag = maze.getFlag(closestFlagPos);
      
      //System.out.println("------- AStarHelper --------\nClosest flag to hero: "+closestFlagPos+" Actual loc: "+closestFlag);
      //System.out.println("Maze BEFORE: "+maze);
      
      maze = AStar(maze, closestFlag);    //do the search and return a maze thats the same when failed or a new maze state
      System.out.println("Maze AFTER: "+maze);
      maze.deleteFlag(closestFlagPos);    //delete the closest flag after finding, or not finding it
    }
    return maze;
  }
  
  /*
   * METHOD: AStar: taks a starting state and goal location, and determines the best path to get there
   * INPUT: starting state of maze, and goal to reach (a flag)
   * OUTPUT: the modified maze object if a solution is found, or the same initial maze state.
   * PARAM: Maze startState, Loc goal
   * RETURN: Maze solution (same as start if no solution, otherwise modified)
   */
  public static Maze AStar(Maze startState, Loc goal)
  {
    //System.out.println("-------------- AStar beginning: --------------");
    
    ArrayList<Maze> open = new ArrayList<Maze>();   //new arraylist of open positions (children)
    ArrayList<Maze> closed = new ArrayList<Maze>(); //new arraylist of closed positions (already visited)
    
    ArrayList<Maze> children;                       //holds the children Maze states from current maze state
    Maze current, solution = startState;            //start state as solution just incase no solution is found, then we just keep the same state
    int currentLoc;
    boolean inOpen = false, inClosed = false;       //boolean flags to determine where to put a state
    Maze child;
    
    open.add(startState);     //add starting state to the "prioQ"
    
    while (open.size() != 0)  //while available states to visit in open
    {
      currentLoc = findLowestCost(open, goal);  //get the best/lowest cost in open list
      current = open.get(currentLoc);           //get the lowest costing state (priority queue hack)
      open.remove(currentLoc);                  //remove (deque) from open
      
      if (current.getHeroLoc().compare(goal) == true)
      {
        return current;
      }
      
      children = current.generateChildren();    //generate child states, check if not in closed or open
      
      //Goes through all of the children and adds to open or updates depending on boolean flags inOpen and inClosed
      for (int i = 0; i< children.size(); i++)
      {
        child = children.get(i);  //set child to one of children in the list
        
        inOpen = checkLists(open, current);     //check if current is in the open list
        inClosed = checkLists(closed, current);
        
        if (inOpen != true && inClosed != true) //when true, means a new state needs to be created
        {
          child.updateHeuristicCost(goal);      //need to set the heuristic cost of the child          
          open.add(child);
        }
        else if (inOpen == true)
        {
          child.updateHeuristicCost(goal);      //this needs to update cost only if lower!
        }
        else 
        {
          //do nothing?
        }
        
        inOpen = false;    //resetting boolean flags
        inClosed = false;
      }
      
      closed.add(current); //adding the current to closed
    }
    return solution;
  }
  
  
  /*
   * METHOD: checkLists: given an arraylist of maze states and a maze object, will determine if the arraylist contains the passed maze
   * INPUT: An arraylist to search through and a maze object to look for
   * OUTPUT: Boolean true/false
   * PARAM: Arraylist<Maze> list, Maze current (to check for)
   * RETURN: true/false
   */
  public static boolean checkLists(ArrayList<Maze> list, Maze current)
  {
    boolean result = false;
    
    for (int i= 0; i< list.size(); i++)
    {
      int listCost = list.get(i).getCost();      //can just compare the cost and hero location to get uniqueness
      int currentCost = current.getCost();
      boolean sameLoc = list.get(i).getHeroLoc().compare(current.getHeroLoc()); //comparing the two
      
      if ( listCost == currentCost && sameLoc == true )
      {
        result = true;
      }
    }
    return result;
  }
  
//Adds the heuristic value to the current cost to reach that state to determine cost
  /*
   * METHOD: findLowestCost: takes a arraylist and a goal location and finds the location in the arraylist where the maze states hero is closest to the goal
   * INPUT: arraylist to look through and goal (flag) to calculate distance from
   * OUTPUT: position in the arraylist containing the closest state to the goal
   * PARAM: ArrayList<Maze> list, Loc goal
   * RETURN: int bestLocation
   */
  public static int findLowestCost(ArrayList<Maze> list, Loc goal)
  {
    //Maze lowestCost = null;
    double currLowest;
    double bestLowest = 9999;
    int bestLocation = 0;
    
    for (int i = 0; i< list.size(); i++)
    {
      currLowest = list.get(i).getCost() + list.get(i).heuristicValue(goal); //goal being the flag location
      
      if (currLowest < bestLowest)
      {
        bestLocation = i;
      }
    }
    
    return bestLocation;
  } 
  
} //END MAIN


/*
 * CLASS: Loc: groups x,y coordinates into easy to handle class
 * PURPOSE: char[][] array, a stack with a coordinate and the character integer to fill with
 */
class Loc
{
  private int x;
  private int y;
  
  public Loc(int x, int y)
  {
    this.x = x;
    this.y = y;
  }
  
  public String toString()
  {
    return "("+x+", "+y+")";
  }
  
  public int getX() {return x;}
  public int getY() {return y;}
  public void setY(int y) {this.y = y;}
  public void setX(int x) {this.x = x;}
  
  public boolean compare(Loc a)
  {
    boolean result = false;
    if (a.getX() == this.x && a.getY() == this.y)
    {
      result = true;
    }
    return result;
  }
  
  /*
   * METHOD: euclideanDistance: calculates the euclidean distance from "this" point and another
   * INPUT: two Loc objects, this and other
   * OUTPUT: euclidean distance from the points
   * PARAM: Loc other, this
   * RETURN: double distance
   */
  public double euclideanDistance(Loc other)
  {
    double dY = y - other.getY();
    double dX = x - other.getX();
    double result = Math.sqrt(dX*dX + dY*dY);
    return result;
  }
  
  //Simple clone method for Loc objects
  public Loc clone()
  {
    Loc cloneLoc = new Loc(this.x, this.y);
    return cloneLoc;
  }
}


/*
 * CLASS: Maze: keeps data about the current state of this maze
 * PURPOSE: To contain all of the data about each state
 */
class Maze
{ 
  private ArrayList<Maze> children; //keep track of each states children
  public int[][] maze;              //the actual maze 2d array
  private int height;               
  private int width;
  private Loc heroLoc;              //location of hero
  private int cost;                 //cost to get to this maze state
  private double heuristicCost;     //cost + euclidean value
  private ArrayList<Loc> flags;     //flags left to find in this maze state
  
  
  //Enumerated types for the types of tiles
  public enum Tile
  {
    OPEN(0), WALL(-1), FLAG(-2), HERO(-3);
    
    private int value;
    private Tile(int value) 
    {
      this.value = value;
    }
    
    //get value of the enum used
    public int getValue()
    {
      return value;
    }
  }
  
  //Constructor for Maze
  public Maze(int[][] newMaze)
  {
    this.maze = newMaze;
    height = maze.length;
    width = maze[0].length;
    heroLoc = null;
    cost = 0;
    heuristicCost = 0;  //cost + euclidean distance from flag
    flags = new ArrayList<Loc>();
    children = new ArrayList<Maze>();
    
    analyzeMaze(); //adds flags, the hero location to the Maze object.
  }
  
  //Simple clone method for maze objects, returns new maze object
  public Maze clone()
  {
    Maze mazeClone;
    Loc cloneHeroLoc;
    int[][] arrayClone = new int[this.maze.length][];
    for ( int i= 0; i < this.maze.length; i++)
    {
      arrayClone[i] = this.maze[i].clone();
    }
    
    mazeClone = new Maze(arrayClone);
    cloneHeroLoc = this.heroLoc.clone();
    mazeClone.setHeroLoc(cloneHeroLoc);
    mazeClone.setCost(this.cost);
    return mazeClone;
  }
  
  public void setCost(int cost) 
  {
    this.cost = cost;
  }
  
  public Loc getFlag(int position)
  {
    return flags.get(position);
  }
  
  public int flagsLeft()
  {
    return flags.size();
  }
  
  public void deleteFlag(int arrayLoc)
  {
    flags.remove(arrayLoc);
  }
  
  //finds the euclidean distance from the flag to the hero
  public double heuristicValue(Loc goal)
  {
    return heroLoc.euclideanDistance(goal);
  }
  
  public int getCost() 
  {
    return cost;
  }
  
  //takes the hero location and goes through all of the flags to find the location of the closest flag!
  /*
   * METHOD: getClosestFlagLoc: goes through the arraylist of flags and finds the closest one
   * INPUT: maze objects flags arraylist
   * OUTPUT: The location in the flag arraylist of the closest flag
   * PARAM: None
   * RETURN: Int result
   */
  public int getClosestFlagLoc()
  {
    int result = 0;
    double closestDist = 99999;
    double currDist;
    for (int i = 0; i< flags.size(); i++)
    {
      currDist = heroLoc.euclideanDistance(flags.get(i));
      if (currDist < closestDist)
      {
        closestDist = currDist;
        result = i;
      }
    }
    return result;
  }
  
  public void addChild(Maze child)
  {
    children.add(child);
  }
  
  public void setHeroLoc(Loc location) 
  { 
    this.heroLoc = location; 
  }
  
  public Loc getHeroLoc() 
  {
    return heroLoc; 
  }
  
  /*
   * METHOD: generateChildren: takes this maze, applies all of the possible moves to it and adds them into a children arraylist if valid
   * INPUT: this maze state
   * OUTPUT: new arraylist of maze states that are possible from this location
   * PARAM: None
   * RETURN: ArrayList<Maze> children
   */
  public ArrayList<Maze> generateChildren()
  {
    ArrayList<Maze> children = new ArrayList<Maze>();
    boolean valid = false; //tells us if the move is valid, so we can add it to children or not
    //for each possible move direction attempt to move the hero that direction
    
    for ( int i = 0; i < 4; i++) //for the 4 directions we can move
    {
      valid = false; //resetting valid as false
      Maze child = this.clone();
      valid = child.moveHero(i);
      
      if (valid == true)
      {
        children.add(child);
      }
    }
    return children;
  }
  
  //Calculates the cost from the hero location to the location of a flag in the maze
  public void updateHeuristicCost(Loc flag)
  {
    this.heuristicCost = cost + this.heroLoc.euclideanDistance(flag);
  }
  
  /*
   * METHOD: moveHero: takes a direction operator and applies it to the maze state, if the move doesn't make the hero hit a wall
   * INPUT: direction to move
   * OUTPUT: Boolean true/false if move is possible
   * PARAM: int direction (0 = up, 1 = right, 2 = down, 3 = left)
   * RETURN: boolean valid
   */
  public boolean moveHero(int direction)
  {
    boolean valid = false;
    
    if (direction == 0) //up direction
    {
      if ( (maze[heroLoc.getY()-1][heroLoc.getX()] ) != Tile.WALL.getValue() )
      {
        heroLoc.setY(heroLoc.getY()-1);
        valid = true;
        if ( maze[heroLoc.getY()][heroLoc.getX()] == Tile.FLAG.getValue())
        {
          maze[heroLoc.getY()][heroLoc.getX()] = maze[heroLoc.getY()][heroLoc.getX()] +3; //only add two when encountering the flag for the first time
        }
        else 
        {
          maze[heroLoc.getY()][heroLoc.getX()] = maze[heroLoc.getY()][heroLoc.getX()] +1;
        }
      }
    }
    
    else if (direction == 1) //right direction
    {
      if ( (maze[heroLoc.getY()][heroLoc.getX()+1] ) != Tile.WALL.getValue() )
      {
        heroLoc.setX(heroLoc.getX()+1);
        valid = true;
        if ( maze[heroLoc.getY()][heroLoc.getX()] == Tile.FLAG.getValue())
        {
          maze[heroLoc.getY()][heroLoc.getX()] = maze[heroLoc.getY()][heroLoc.getX()] +3; //only add two when encountering the flag for the first time
        }
        else 
        {
          maze[heroLoc.getY()][heroLoc.getX()] = maze[heroLoc.getY()][heroLoc.getX()] +1;
        }
      }
    }
    
    else if (direction == 2)  //down direction
    {
      if ( (maze[heroLoc.getY()+1][heroLoc.getX()] ) != Tile.WALL.getValue() )
      {
        heroLoc.setY(heroLoc.getY()+1);
        valid = true;
        
        if ( maze[heroLoc.getY()][heroLoc.getX()] == Tile.FLAG.getValue())
        {
          maze[heroLoc.getY()][heroLoc.getX()] = maze[heroLoc.getY()][heroLoc.getX()] +3; //only add two when encountering the flag for the first time
        }
        else
        {
          maze[heroLoc.getY()][heroLoc.getX()] = maze[heroLoc.getY()][heroLoc.getX()] +1;
        }
      }
    }
    else if (direction == 3) //left direction
    {
      if ( (maze[heroLoc.getY()][heroLoc.getX()-1] ) != Tile.WALL.getValue() )
      {
        heroLoc.setX(heroLoc.getX()-1);
        valid = true;
        if ( maze[heroLoc.getY()][heroLoc.getX()] == Tile.FLAG.getValue())
        {
          maze[heroLoc.getY()][heroLoc.getX()] = maze[heroLoc.getY()][heroLoc.getX()] +3; //only add three when encountering the flag for the first time
        }
        else
        {
          maze[heroLoc.getY()][heroLoc.getX()] = maze[heroLoc.getY()][heroLoc.getX()] +1;
        }
      }
    }
    
    if (valid == true)  //only increase the current cost to reach this state when a move is valid
    {
      cost++;
    }
    return valid;
  }
  
  
  /*
   * METHOD: analyzeMaze: takes the initial maze object and fills in some of its private variables (flags, hero location etc)
   * INPUT: The maze's int[][] and heigh/width
   * OUTPUT: Fills the flag arraylist and heroLoc in the maze object
   * PARAM: None
   * RETURN: None
   */  private void analyzeMaze()
   {
     Loc newItem;
     
     for (int y = 0; y < height; y++)
     {
       for(int x = 0; x< width; x++)
       {
         if (maze[y][x] == Tile.FLAG.value)
         {
           newItem = new Loc(x, y);
           flags.add(newItem);
         }
         else if (maze[y][x] == Tile.HERO.value)
         {
           heroLoc = new Loc(x, y);
           maze[y][x] = 1; //wherever the hero is standing will count as being travelled on already
         }
       }
     }
   }
   
   
   /*
    * METHOD: countMoves: goes through the maze 2d array and counts the amount of moves have been done
    * INPUT: maze 2d array
    * OUTPUT: number of moves completed
    * PARAM: None
    * RETURN: int moves
    */
   public int countMoves()
   {
     int moves = 0;
     for (int y = 0; y < height; y++)
     {
       for(int x = 0; x< width; x++)
       {
         if (maze[y][x] > 0)
         {
           moves++;
         }
       }
     }
     return moves;
   }
   
   
   public String toString()
   {
     String result = "\n---------- Maze: ----------\n";
     char wall = '#';
     //char hero = '@';
     char flag = '!';
     
     for (int y = 0; y< height; y++)
     {
       for(int x = 0; x< width; x++)
       {
         
         if (maze[y][x] == -1)
         {
           result+= wall;
         }
         else if (maze[y][x] == -2)
         {
           result+=flag;
         }
         else
         {
           result+= ( (char) (maze[y][x]+'0') );
         }
       }
       
       result+="\n"; //start a new line
     }
     result +="---------- Stats: ----------";
     
     //Print out the locations of important items on the map
     if (heroLoc != null)
     {
       result += "\nHero at: "+heroLoc.toString();
     }
     
     if ( flags != null)
     {
       result +="\nFlags at: "+flags.toString();
     }
     
     result += "\nCurrent cost: "+cost;
     return result;
   }
   
   public void print()
   {
     System.out.println(this.toString());
   }
}