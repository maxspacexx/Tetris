package assignment;

import java.util.ArrayList;

/**
 * A Lame Brain implementation for JTetris; tries all possible places to put the
 * piece (but ignoring rotations, because we're lame), trying to minimize the
 * total height of pieces on the board.
 */
public class BrainHelper implements Brain {

    private ArrayList<Board> options;
    private ArrayList<Board.Action> firstMoves;
    double[] coef;

    public BrainHelper(double[] a)
    {
        coef = a;
    }
    /**
     * Decide what the next move should be based on the state of the board.
     */
    public Board.Action nextMove(Board currentBoard) {
        // Fill our options array with versions of the new Board
        options = new ArrayList<>();
        firstMoves = new ArrayList<>();
        enumerateOptions(currentBoard);

        double best = 0;
        int bestIndex = 0;
        // Check all the options and get the one with the highest score
        for (int i = 0; i < options.size(); i++) {
            double score = score(options.get(i), coef);
            if (score > best) {
                best = score;
                bestIndex = i;
            }
        }

        // We want to return the first move on the way to the best Board
        return firstMoves.get(bestIndex);
    }

    public double bestScore(Board currentBoard)
    {
        options = new ArrayList<>();
        firstMoves = new ArrayList<>();
        enumerateOptions(currentBoard);

        double best = 0;

        // Check all of the options and get the one with the highest score
        for (int i = 0; i < options.size(); i++) {
            double score = score(options.get(i), coef);
            if (score > best) {
                best = score;
            }
        }
        return best;
    }

    /**
     * Test all the places we can put the current Piece.
     * Since this is just a Lame Brain, we aren't going to do smart
     * things like rotating pieces.
     */
    private void enumerateOptions(Board currentBoard) {
        // We can always drop our current Piece
        options.add(currentBoard.testMove(Board.Action.DROP));
        firstMoves.add(Board.Action.DROP);

        // Now we'll add all the places to the left we can DROP
        Board left = currentBoard.testMove(Board.Action.LEFT);
        while (left.getLastResult() == Board.Result.SUCCESS) {
            options.add(left.testMove(Board.Action.DROP));
            firstMoves.add(Board.Action.LEFT);
            left.move(Board.Action.LEFT);
        }

        // And then the same thing to the right
        Board right = currentBoard.testMove(Board.Action.RIGHT);
        while (right.getLastResult() == Board.Result.SUCCESS) {
            options.add(right.testMove(Board.Action.DROP));
            firstMoves.add(Board.Action.RIGHT);
            right.move(Board.Action.RIGHT);
        }
    }

    /**
     * Since we're trying to avoid building too high,
     * we're going to give higher scores to Boards with
     * MaxHeights close to 0.
     */
    //calculate scores
    private double score(Board newBoard, double[] a) {
        double score = 0;
        if(newBoard.getMaxHeight() > newBoard.getHeight() - 4)
        {
            return -10000;
        }
        int heightSum = 0;
        for(int i = 0; i < newBoard.getWidth(); i++)
        {
            heightSum += newBoard.getColumnHeight(i);
        }
        int rowsCleared = newBoard.getRowsCleared();
        int holes = holes(newBoard);

        int bumps = 0;
        for(int i = 0; i < newBoard.getWidth() - 1; i++)
        {
            bumps += Math.abs(newBoard.getColumnHeight(i) - newBoard.getColumnHeight(i + 1));
        }
        score = ((a[0])*heightSum) + ((a[1])*rowsCleared) + ((a[2])*holes)+((a[3])*bumps) + 100000;
        return score;
    }

    int holes(Board b)
    {
        int holes = 0;
        for(int x = 0; x < b.getWidth(); x++)
        {
            for(int y = 0; y < b.getColumnHeight(x); y++)
            {
                if(b.getGrid(x, y) == null)
            {
                holes++;
            }
        }
    }
        return holes;
}

}
