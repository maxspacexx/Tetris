package assignment;

import java.awt.*;
import java.util.*;

/**
 * Represents a Tetris board -- essentially a 2-d grid of piece types (or nulls). Supports
 * tetris pieces and row clearing.  Does not do any drawing or have any idea of
 * pixels. Instead, just represents the abstract 2-d board.
 */
public final class TetrisBoard implements Board {
    private final int width;
    private final int height;
    private Piece currentPiece;
    private Point currentPoint;
    private HashMap<Point, Piece> board;
    private Action lastAction;
    private Result lastResult;
    private int clockwiseWallKickno;
    private int counterclockwiseWallKickno;
    private int rowsCleared;
    private int[] columnHeights;
    private int maxColumnHeight;
    private int[] rowWidths;

    // JTetris will use this constructor
    public TetrisBoard(int width, int height) {
        this.width = width;
        this.height = height;
        board = new HashMap<>();
        clockwiseWallKickno = 0;
        counterclockwiseWallKickno = 0;
        rowsCleared = 0;
        maxColumnHeight = 0;
        columnHeights = new int[width];
        rowWidths = new int[height];
    }

    //constructor used in testMove, needs a copy of the board
    public TetrisBoard(int width, int height, HashMap<Point,Piece> b, Point p, Piece piece) {
        this(width, height);
        this.board = new HashMap<>();
        //deep copy the board
        for (HashMap.Entry<Point, Piece> e : b.entrySet())
        {
            board.put(e.getKey().getLocation(), new TetrisPiece(e.getValue().getType(), e.getValue().getBody(), e.getValue().getRotationIndex()));
        }
        //copy the point and piece
        this.currentPoint = p.getLocation();
        this.currentPiece = new TetrisPiece(piece.getType(), piece.getBody(), piece.getRotationIndex());
    }

    @Override
    public Result move(Action act)
    {
        //check for a piece
        if(currentPiece == null)
        {
            return Result.NO_PIECE;
        }
        //initialize variables
        lastResult = Result.SUCCESS;
        lastAction = act;
        rowsCleared = 0;
        //does actions
        if(act.equals(Action.LEFT))
        {
            //check if action is possible
            if(checkImpossible(act).contains(Action.LEFT))
            {
                lastResult = Result.OUT_BOUNDS;
            }
            else
            {
                //remove piece, move location, add back piece
                removeCurrentPiece();
                currentPoint.setLocation(currentPoint.getX() - 1, currentPoint.getY());
                addCurrentPiece();
            }
        }
        else if(act.equals(Action.RIGHT))
        {
            //check if action is possible
            if(checkImpossible(act).contains(Action.RIGHT))
            {
                lastResult = Result.OUT_BOUNDS;
            }
            else
            {
                //remove piece, move location, add back piece
                removeCurrentPiece();
                currentPoint.setLocation(currentPoint.getX() + 1, currentPoint.getY());
                addCurrentPiece();
            }
        }
        else if(act.equals(Action.DOWN))
        {
            //check if action is possible
            if(checkImpossible(act).contains(Action.DOWN))
            {
                lastResult = Result.PLACE;
                addCurrentPiece();
            }
            else
            {
                //remove piece, move location, add back piece
                removeCurrentPiece();
                currentPoint.setLocation(currentPoint.getX(), currentPoint.getY() - 1);
                addCurrentPiece();
            }
        }
        else if(act.equals(Action.DROP))
        {
            //move the piece down until it hits something
            do
            {
                move(Action.DOWN);
            }
            while(!checkImpossible(act).contains(Action.DOWN));
            //set lastResult and lastAction
            lastResult = Result.PLACE;
            lastAction = Action.DROP;
            addCurrentPiece();
        }
        else if(act.equals(Action.CLOCKWISE))
        {
            //check if action is possible
            if(checkImpossible(act).contains(Action.CLOCKWISE))
            {
                lastResult = Result.OUT_BOUNDS;
            }
            else
            {
                removeCurrentPiece();
                //translate the piece according to wall kick
                Point kick;
                if (currentPiece.getType().equals(Piece.PieceType.STICK))
                {
                    kick = Piece.I_CLOCKWISE_WALL_KICKS[currentPiece.getRotationIndex()][clockwiseWallKickno];
                } else
                {
                    kick = Piece.NORMAL_CLOCKWISE_WALL_KICKS[currentPiece.getRotationIndex()][clockwiseWallKickno];
                }
                currentPoint.translate(kick.x, kick.y);
                //rotate piece
                currentPiece = currentPiece.clockwisePiece();
                addCurrentPiece();
            }
        }
        else if(act.equals(Action.COUNTERCLOCKWISE))
        {
            //check if action is possible
            if(checkImpossible(act).contains(Action.COUNTERCLOCKWISE))
            {
                lastResult = Result.OUT_BOUNDS;
            }
            else
            {
                removeCurrentPiece();
                //translate the piece according to wall kick
                Point kick;
                if (currentPiece.getType().equals(Piece.PieceType.STICK))
                {
                    kick = Piece.I_COUNTERCLOCKWISE_WALL_KICKS[currentPiece.getRotationIndex()][counterclockwiseWallKickno];
                } else
                {
                    kick = Piece.NORMAL_COUNTERCLOCKWISE_WALL_KICKS[currentPiece.getRotationIndex()][counterclockwiseWallKickno];
                }
                currentPoint.translate(kick.x, kick.y);
                //rotate piece
                currentPiece = currentPiece.counterclockwisePiece();
                addCurrentPiece();
            }
        }
        else if(act.equals(Action.NOTHING))
        {
        }

        //find row widths
        computeRowWidths();
        //clear rows if complete
        for(int i = 0; i < height; i++)
        {
            if(getRowWidth(i) == width)
            {
                //removes row
                for(int j = 0; j < width; j++)
                {
                    board.remove(new Point(j, i));
                }
                //shifts higher rows down
                for(int h = i; h < height; h++)
                {
                    for(int j = 0; j < width; j++)
                    {
                        if(board.get(new Point(j, h)) != null)
                        {
                            board.put(new Point(j, h - 1), board.remove(new Point(j, h)));
                        }
                    }
                }
                //recompute row widths
                computeRowWidths();
                i--;
                rowsCleared++;
            }
        }
        //compute aggregate column height
        for(int i = 0; i < width; i++)
        {
            columnHeights[i] = 0;
            for (int j = height - 1; j >= 0; j--)
            {
                if (getGrid(i, j) != null)
                {
                    columnHeights[i] = j + 1;
                    break;
                }
            }
        }
        //find max column height
        maxColumnHeight = 0;
        for(int i = 0; i < width; i++)
        {
            int height = this.getColumnHeight(i);
            if(maxColumnHeight < height)
            {
                maxColumnHeight = height;
            }
        }
        return lastResult;
    }

    @Override
    //tests a move on a copy of the board, used in the AI
    public Board testMove(Action act)
    {
        Board temp = new TetrisBoard(this.width, this.height, this.board, this.currentPoint, this.currentPiece);
        temp.move(act);
        return temp;
    }

    public HashMap<Point, Piece> getBoard()
    {
        return board;
    }

    @Override
    public Piece getCurrentPiece() { return currentPiece; }

    @Override
    public Point getCurrentPiecePosition() { return currentPoint; }

    @Override
    //gets next new piece
    public void nextPiece(Piece p, Point spawnPosition)
    {
        currentPiece = p;
        currentPoint = spawnPosition;
        addCurrentPiece();
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof TetrisBoard)) return false;
        TetrisBoard b = (TetrisBoard) other;
        if(b.getCurrentPiece() != currentPiece && b.getCurrentPiecePosition() != currentPoint && b.getCurrentPiecePosition().equals(currentPoint) && !b.getCurrentPiece().equals(currentPiece))
        {
            return false;
        }
        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                Point check = new Point(x, y);
                if(board.get(check) != null && board.get(check).getType().equals(b.getBoard().get(check).getType()))
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Result getLastResult() { return lastResult; }

    @Override
    public Action getLastAction() { return lastAction; }

    @Override
    public int getRowsCleared() { return rowsCleared; }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }

    @Override
    public int getMaxHeight()
    {
        return maxColumnHeight;
    }

    @Override
    public int dropHeight(Piece piece, int x)
    {
        return this.testMove(Action.DROP).getCurrentPiecePosition().y;
    }

    @Override
    public int getColumnHeight(int x)
    {
        return columnHeights[x];
    }

    void computeRowWidths()
    {
        for(int y = 0; y < height; y++)
        {
            int counter = 0;
            for (int i = 0; i < width; i++)
            {
                if (this.getGrid(i, y) != null)
                {
                    counter++;
                }
            }
            rowWidths[y] = counter;
        }
    }
    @Override
    public int getRowWidth(int y)
    {
        return rowWidths[y];
    }

    @Override
    //get the current board without the current piece on it
    public Piece.PieceType getGrid(int x, int y)
    {
        boolean pieceRemoved = false;
        //only remove current piece if it was not placed
        if (!Result.PLACE.equals(lastResult) && currentPiece != null)
        {
            //add and removeCurrentPiece are constant time since there are exactly 4 points in every piece
            removeCurrentPiece();
            pieceRemoved = true;
        }
        Point temp = new Point(x, y);
        //return null if board or mapping doesn't exist
        if(this.board == null)
        {
            return null;
        }
        if(board.get(temp) == null)
        {
            return null;
        }
        Piece.PieceType t = board.get(temp).getType();
        //add back piece if it was removed earlier
        if(pieceRemoved)
        {
            addCurrentPiece();
        }
        return t;
    }



    //checks which actions are impossible
    Set<Action> checkImpossible(Action act)
    {
        Set<Action> impossibleActions = new HashSet<>();

    //check for collisions on walls
        for(int i = 0; i < currentPiece.getBody().length; i++)
        {
            //dx and dy are the x and y values relative to the bounding box
            int dx = currentPiece.getBody()[i].x;
            int dy = currentPiece.getBody()[i].y;
            //makes a temporary point at the current point
            Point temp = currentPoint.getLocation();
            //adds each dx and dy to the temporary point
            temp.translate(dx, dy);
            //check boundary below
            if(temp.getY() == 0)
            {
                impossibleActions.add(Action.DOWN);
            }
            //check boundary on the right
            if(temp.getX() == width - 1)
            {
                impossibleActions.add(Action.RIGHT);
            }
            //check boundary on the left
            if(temp.getX() == 0)
            {
                impossibleActions.add(Action.LEFT);
            }
        }

        int[] Iskirt;
        Point[] pskirt;
        Piece rotatedSkirt;
    //check for piece collisions below
        if(act.equals(Action.DOWN) || act.equals(Action.DROP))
        {
            //get the skirt
            Iskirt = currentPiece.getSkirt();
            for (int i = 0; i < Iskirt.length; i++)
            {
                //makes a temporary point at the current point and moves it to the new location to test
                Point temp = currentPoint.getLocation();
                temp.translate(i, Iskirt[i]);
                if (this.getGrid(temp.x, temp.y - 1) != (null))
                {
                    impossibleActions.add(Action.DOWN);
                }
            }
        }

    //check for collision to the left
        if(act.equals(Action.LEFT))
        {
            //get the skirt of the counterclockwise piece
            Iskirt = currentPiece.counterclockwisePiece().getSkirt();
            //convert int array to point array
            pskirt = new Point[Iskirt.length];
            for (int i = 0; i < Iskirt.length; i++)
            {
                pskirt[i] = new Point(i, Iskirt[i]);
            }
            //make a new piece with the counterclockwise skirt as body
            rotatedSkirt = new TetrisPiece(currentPiece.getType(), pskirt, 0);
            //rotate the piece back clockwise to get the leftmost edge
            Point[] leftmost = rotatedSkirt.clockwisePiece().getBody();
            for (Point point : leftmost)
            {
                int dx = point.x;
                int dy = point.y;
                //makes a temporary point at the current point and move it to the new location to test
                Point temp = currentPoint.getLocation();
                temp.translate(dx, dy);
                if (this.getGrid(temp.x - 1, temp.y) != (null))
                {
                    impossibleActions.add(Action.LEFT);
                }
            }
        }

    //check for collision to the right
        //get the skirt of the clockwise piece
        if(act.equals(Action.RIGHT))
        {
            Iskirt = currentPiece.clockwisePiece().getSkirt();
            //convert int array to point array
            pskirt = new Point[Iskirt.length];
            for (int i = 0; i < Iskirt.length; i++)
            {
                pskirt[i] = new Point(i, Iskirt[i]);
            }
            //make a new piece with the clockwise skirt as body
            rotatedSkirt = new TetrisPiece(currentPiece.getType(), pskirt, 0);
            //rotate the piece back counterclockwise to get the leftmost edge
            Point[] rightmost = rotatedSkirt.counterclockwisePiece().getBody();
            for (Point point : rightmost)
            {
                int dx = point.x;
                int dy = point.y;
                //makes a temporary point at the current point and move it to the new location to test
                Point temp = currentPoint.getLocation();
                temp.translate(dx, dy);
                if (this.getGrid(temp.x + 1, temp.y) != (null))
                {
                    impossibleActions.add(Action.RIGHT);
                }
            }
        }

    //check for collision in clockwise rotation
        if(act.equals(Action.CLOCKWISE))
        {
            //remove current piece, so it doesn't check for collisions with itself
            removeCurrentPiece();
            Point[] clockwisePiece = currentPiece.clockwisePiece().getBody();
            int testno;
            //check 5 locations for wall kick
            for (testno = 0; testno < 5; )
            {
                Point kickPoint;
                //use different arrays if the piece is a stick
                if(currentPiece.getType().equals(Piece.PieceType.STICK))
                {
                    kickPoint = Piece.I_CLOCKWISE_WALL_KICKS[currentPiece.getRotationIndex()][testno];
                }
                else
                {
                    kickPoint = Piece.NORMAL_CLOCKWISE_WALL_KICKS[currentPiece.getRotationIndex()][testno];
                }
                boolean rotatable = true;
                for (Point point : clockwisePiece)
                {
                    int dx = point.x + kickPoint.x;
                    int dy = point.y + kickPoint.y;
                    //makes a temporary point at the current point and move it to the new location to test
                    Point temp = currentPoint.getLocation();
                    temp.translate(dx, dy);
                    if (board.get(temp) != null || temp.x < 0 || temp.y < 0 || temp.x > width - 1)
                    {
                        rotatable = false;
                        testno++;
                        break;
                    }
                }
                if (rotatable)
                {
                    break;
                }

            }
            clockwiseWallKickno = testno;
            //if all the tests failed, then cannot rotate clockwise
            if (testno == 5)
            {
                impossibleActions.add(Action.CLOCKWISE);
            }
            //add back current piece
            addCurrentPiece();
        }

    //check for collision in counterclockwise rotation
        if(act.equals(Action.COUNTERCLOCKWISE))
        {
            //remove current piece, so it doesn't check for collisions with itself
            removeCurrentPiece();
            Point[] counterclockwisePiece = currentPiece.counterclockwisePiece().getBody();
            int testno;
            //check 5 locations for wall kick
            for (testno = 0; testno < 5; )
            {
                Point kickPoint;
                //use different arrays if the piece is a stick
                if(currentPiece.getType().equals(Piece.PieceType.STICK))
                {
                    kickPoint = Piece.I_COUNTERCLOCKWISE_WALL_KICKS[currentPiece.getRotationIndex()][testno];
                }
                else
                {
                    kickPoint = Piece.NORMAL_COUNTERCLOCKWISE_WALL_KICKS[currentPiece.getRotationIndex()][testno];
                }
                boolean rotatable = true;
                for (Point point : counterclockwisePiece)
                {
                    int dx = point.x + kickPoint.x;
                    int dy = point.y + kickPoint.y;
                    //makes a temporary point at the current point and move it to the new location to test
                    Point temp = currentPoint.getLocation();
                    temp.translate(dx, dy);
                    //System.out.println(temp);
                    if (board.get(temp) != null || temp.x < 0 || temp.y < 0 || temp.x > width - 1)
                    {
                        rotatable = false;
                        testno++;
                        break;
                    }
                }
                if (rotatable)
                {
                    break;
                }
            }
            counterclockwiseWallKickno = testno;
            //if all the tests failed, then cannot rotate counterclockwise
            if (testno == 5)
            {
                impossibleActions.add(Action.COUNTERCLOCKWISE);
            }
            //add back current piece
            addCurrentPiece();
        }
        return impossibleActions;
    }

    //function to remove piece from board
    void removeCurrentPiece()
    {
        for(int i = 0; i < currentPiece.getBody().length; i++)
        {
            //dx and dy are the x and y values relative to the bounding box
            int dx = currentPiece.getBody()[i].x;
            int dy = currentPiece.getBody()[i].y;
            //makes a temporary point at the current location and translates it to the correct location
            Point temp = currentPoint.getLocation();
            temp.translate(dx, dy);
            //removes the point
            board.remove(temp);
        }
    }

    //function to add piece to board
    void addCurrentPiece()
    {
        for(int i = 0; i < currentPiece.getBody().length; i++)
        {
            //dx and dy are the x and y values relative to the bounding box
            int dx = currentPiece.getBody()[i].x;
            int dy = currentPiece.getBody()[i].y;
            //makes a temporary point at the current location and translates it to the correct location
            Point temp = currentPoint.getLocation();
            temp.translate(dx, dy);
            //adds the point
            board.put(temp, currentPiece);
        }
    }
}
