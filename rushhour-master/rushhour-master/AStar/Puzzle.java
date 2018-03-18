package AStar;

import java.io.*;
import java.util.*;

/**
 * This is the class for representing a particular rush hour puzzle. Methods are
 * provided for accessing information about a puzzle, and also for reading in a
 * list of puzzles from a data file. In addition, this class maintains a counter
 * of the number of search nodes that have been expanded for this puzzle.
 * Methods for accessing, incrementing or resetting this counter are also
 * provided.
 * <p>
 * Every car is constrained to only move horizontally or vertically. Therefore,
 * each car has one dimension along which it is fixed, and another dimension
 * along which it can be moved. The fixed dimension is stored here as part of
 * the puzzle. Also stored here are the sizes and orientations of the cars, the
 * size of the puzzle grid, the name of the puzzle and the initial (root) search
 * node of the puzzle.
 * <p>
 * The goal car is always assigned index 0.
 */
public class Puzzle {

	private String name;
	private Node initNode;

	private int searchCount;

	private int numCars;
	private int fixedPos[];
	private int carSize[];
	private boolean carOrient[];
	private char carName[];

	private int gridSize;

	/** Returns the number of cars for this puzzle. */
	public int getNumCars() {
		return numCars;
	}

	/** Returns the fixed position of car <tt>v</tt>. */
	public int getFixedPosition(int v) {
		return fixedPos[v];
	}

	/** Returns the size (length) of car <tt>v</tt>. */
	public int getCarSize(int v) {
		return carSize[v];
	}

	/**
	 * Returns the orientation of car <tt>v</tt>, where <tt>true</tt> means that
	 * the car is vertically oriented.
	 */
	public boolean getCarOrient(int v) {
		return carOrient[v];
	}

	/** Increments the search counter by <tt>d</tt>. */
	public void incrementSearchCount(int d) {
		searchCount += d;
	}

	/**
	 * Returns the current value of the search counter, which keeps a count of
	 * the number of nodes generated on the current search.
	 */
	public int getSearchCount() {
		return searchCount;
	}

	/** Resets the search counter to 1 (for the initial node). */
	public void resetSearchCount() {
		searchCount = 1;
	}

	/** Returns the name of this puzzle. */
	public String getName() {
		return name;
	}

	/**
	 * Returns the grid size of this puzzle, i.e., the length along each side.
	 */
	public int getGridSize() {
		return gridSize;
	}

	/** Returns the initial (root) node of this puzzle. */
	public Node getInitNode() {
		return initNode;
	}

	/**
	 * The main constructor for constructing a puzzle. You probably will never
	 * need to use this constructor directly, since ordinarily puzzles will be
	 * constructed by reading them in from a datafile using the
	 * <tt>readPuzzlesFromFile</tt> method. It is assumed that the goal car is
	 * always assigned index 0.
	 *
	 * @param name
	 *            the name of the puzzle
	 * @param gridSize
	 *            the size of one side of the puzzle grid
	 * @param numCars
	 *            the number of cars on this puzzle
	 * @param orient
	 *            the orientations of each car (<tt>true</tt> = vertical)
	 * @param size
	 *            the sizes of each car
	 * @param x
	 *            the x-coordinates of each car
	 * @param y
	 *            the y-coordinates of each car
	 */
	public Puzzle( int gridSize, int numCars,char name[], boolean orient[], int size[], int x[], int y[]) {
		
		this.numCars = numCars;
		this.gridSize = gridSize;
		if (numCars <= 0) {
			throw new IllegalArgumentException("Each puzzle must have a positive number of cars");
		}
		carOrient = new boolean[numCars];
		carSize = new int[numCars];
		fixedPos = new int[numCars];
		carName= new char[numCars];
		int varPos[] = new int[numCars];

		boolean grid[][] = new boolean[gridSize][gridSize];

		for (int v = 0; v < numCars; v++) {
			carOrient[v] = orient[v];
			carSize[v] = size[v];
			carName[v]=name[v];
			if (size[v] <= 0)
				throw new IllegalArgumentException("Cars must have positive size");

			if (x[v] < 0 || y[v] < 0 || (orient[v] && y[v] + size[v] > gridSize)
					|| (!orient[v] && x[v] + size[v] > gridSize))
				throw new IllegalArgumentException("Cars must be within bounds of grid"+name[v]);

			for (int d = 0; d < size[v]; d++) {
				
				int xv = x[v], yv = y[v];
				if (!orient[v])
					yv += d;
				else
					xv += d;
				if (grid[xv][yv])
					throw new IllegalArgumentException("Cars cannot overlap "+name[v]);
				System.out.println(name[v] +" "+ xv+ " "+ yv);
				grid[xv][yv] = true;
			}

			if (orient[v]) {
				fixedPos[v] = x[v];
				varPos[v] = y[v];
			} else {
				fixedPos[v] = y[v];
				varPos[v] = x[v];
			}

		}

		initNode = new Node(new State(this, varPos), 0, null);

		resetSearchCount();
	}

	/**
	 * A static method for reading in a list of puzzles from the data file
	 * called <tt>filename</tt>. Each puzzle is described in the data file using
	 * the format described on the assignment. The set of puzzles is returned as
	 * an array of <tt>Puzzle</tt>'s.
	 */
	public static Puzzle[] readPuzzlesFromFile2(String filename) throws FileNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));

		ArrayList puzzles = new ArrayList();
		ArrayList car_list = null;

		
		String line;
		
		int numcars=0;
		int line_count = 0;
		int gridsize = 36;
		String inputToString="";
		
		int[][] grid =new int [6][6];
		while ((line = in.readLine()) != null) {
			ArrayList<Character> name = new ArrayList();
			for(int i=0;i<36;i++) {
				char inp = line.charAt(i);
				if(inp!=('.') && !name.contains(inp))
				{
					name.add(inp);
					
				}
				
			}
			//System.out.println(name.toString());
			grid=makeGrid(line);
			numcars=name.size();
			boolean orient[] = new boolean[numcars];
			int size[] = new int[numcars];
			int x[] = new int[numcars];
			int y[] = new int[numcars];
			char names[]=new char[numcars];
			for(int i=0;i<numcars;i++) {
				char car=name.get(i);
				if(car=='X' ||(name.get(i)>='A' && car<='K'))size[i]=2;
				else 
					if(car>='O' && car<='R')size[i]=3;
				
				int []xy=searchGrid(grid, car);
				x[i]=xy[0];
				y[i]=xy[1];
				names[i]=car;
				orient[i]=findOrient(grid, car);
				System.out.println(names[i]);
				System.out.println(x[i]);
				System.out.println(y[i]);
				System.out.println(size[i]);
				System.out.println(orient[i]);
				System.out.println("....................................");
				
			}
			
		//	 int gridSize, int numCars,String name[], boolean orient[], int size[], int x[], int y[]
			Puzzle p=new Puzzle( gridsize, numcars, names, orient, size, x, y);
			puzzles.add(p);
		//	System.out.println(p.toString());
			
		}
		
		
		
		
		System.out.println(puzzles.toString());
		
		//puzzles.add(new Puzzle(name, gridsize, numcars, orient, size, x, y));
		
		return (Puzzle[]) puzzles.toArray(new Puzzle[0]);
	}
	
	public static int [][] makeGrid(String input)
	{
		int[][] grid =new int [6][6];
		int index=0;
		for(int i=0; i<6;i++) {
			for(int j=0;j<6;j++) {
				grid[i][j]=input.charAt(index++);
			}
		}
		return grid;
	}
	
	public static int[] searchGrid(int [][]grid,char x) {
		int [] xy=new int [2];
		for(int i=0;i<6;i++)
		{
			for(int j=0;j<6;j++)
				if(grid[i][j]==x) {
					xy[0]=i;
					xy[1]=j;
					return xy;
				}
		}
		return xy;		
		
	}
	
	public static boolean findOrient(int [][]grid,char x) {
		
		for(int i=0;i<6;i++)
		{
			for(int j=0;j<6;j++)
				if(grid[i][j]==x) {
					if(i+1<6 && grid[i+1][j]==x)return true;
					else return false;
				}
		}
		return true;		
		
	}
	
	

	private static class CarRec {
		boolean orient;
		int size;
		int x;
		int y;
	}

}
