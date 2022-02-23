package assignment;

import java.awt.*;
import java.util.Set;

/**
 * An immutable representation of a tetris piece in a particular rotation.
 * 
 * All operations on a TetrisPiece should be constant time, except for it's
 * initial construction. This means that rotations should also be fast - calling
 * clockwisePiece() and counterclockwisePiece() should be constant time! You may
 * need to do precomputation in the constructor to make this possible.
 */
public final class TetrisPiece implements Piece {

    /**
     * Construct a tetris piece of the given type. The piece is in its spawn orientation,
     * i.e., a rotation index of 0.
     */
    private final PieceType ptype;
    int rotationIndex;
    private Point[] body;
    //skirt is the array of blocks at the bottom of the piece
    private int[] skirt;

    public TetrisPiece(PieceType type)
    {
        this.ptype = type;
        this.body = type.getSpawnBody();
        rotationIndex = 0;
        makeSkirt();
    }

    public TetrisPiece(PieceType type, Point[] points, int rotationIndex)
    {
        this(type);
        this.body = points;
        this.rotationIndex = rotationIndex;
        makeSkirt();
    }

    //skirt is the array of blocks at the bottom of the piece
    private void makeSkirt()
    {
        //make skirt
        int width = ptype.getBoundingBox().width;
        int[] skirt = new int[width];
        //initialize skirt array
        for(int i = 0; i < width; i++)
        {
            skirt[i] = Integer.MAX_VALUE;
        }
        //find skirt
        for (Point point : body)
        {
            //check for normal x values
            if(point.x > 4 || point.x < 0)
            {
                break;
            }
            if (skirt[point.x] > point.y)
            {
                skirt[point.x] = point.y;
            }
        }
        this.skirt = skirt;
    }

    @Override
    public PieceType getType() {
        return ptype;
    }

    @Override
    public int getRotationIndex() {
        return rotationIndex;
    }

    public void setRotationIndex(int a){
        rotationIndex = a;
    }

    @Override
    public Piece clockwisePiece() {
        Point[] temp = new Point[body.length];
        for(int i = 0; i < temp.length; i++)
        {
            //creates a temporary body for the new piece
            temp[i] = body[i].getLocation();
        }
        if(ptype.equals(PieceType.SQUARE))
        {
            for (Point point : temp)
            {
                //rotates the temp body clockwise
                point.setLocation(point.getY(), -point.getX() + 1);
            }
        }
        else if(ptype.equals(PieceType.STICK))
        {
            for (Point point : temp)
            {
                //rotates the temp body clockwise
                point.setLocation(point.getY(), -point.getX() + 3);
            }
        }
        else
        {
            for (Point point : temp)
            {
                //rotates the temp body clockwise
                point.setLocation(point.getY(), -point.getX() + 2);
            }
        }
        return new TetrisPiece(ptype, temp, (rotationIndex + 1) % 4);
    }

    @Override
    public Piece counterclockwisePiece() {
        Point[] temp = new Point[body.length];
        for(int i = 0; i < temp.length; i++)
        {
            //creates a temporary body for the new piece
            temp[i] = body[i].getLocation();
        }
        if(ptype.equals(PieceType.SQUARE))
        {
            for (Point point : temp)
            {
                //rotates the temp body clockwise
                point.setLocation(-point.getY() + 1, point.getX());
            }
        }
        else if(ptype.equals(PieceType.STICK))
        {
            for (Point point : temp)
            {
                //rotates the temp body clockwise
                point.setLocation(-point.getY() + 3, point.getX());
            }
        }
        else
        {
            for (Point point : temp)
            {
                //rotates the temp body clockwise
                point.setLocation(-point.getY() + 2, point.getX());
            }
        }
        return new TetrisPiece(ptype, temp, (rotationIndex + 3) % 4);
    }

    @Override
    public int getWidth() {
        return ptype.getBoundingBox().width;
    }

    @Override
    public int getHeight() {
        return ptype.getBoundingBox().height;
    }

    @Override
    public Point[] getBody() {
        return body;
    }

    @Override
    public int[] getSkirt() {
        return skirt;
    }

    @Override
    public boolean equals(Object other)
    {
        // Ignore objects which aren't also tetris pieces.
        if (!(other instanceof TetrisPiece otherPiece)) return false;
        if(this.getRotationIndex() != otherPiece.getRotationIndex() && !ptype.equals(PieceType.SQUARE))
        {
            return false;
        }
        boolean bodyMatch = true;
        Set<Point> set = Set.of(body);
        for (int i = 0; i < otherPiece.getBody().length && i < body.length; i++)
        {
            if (!set.contains(otherPiece.getBody()[i]))
            {
                bodyMatch = false;
                break;
            }
        }
        return bodyMatch;
    }
}
