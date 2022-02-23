package assignment;

import java.util.ArrayList;

public class MyBrain implements Brain
{
    private static int rotations;
    //array of coefficients used for scoring
    double[] coef;
    MyBrain(double[] a)
    {
        coef = a;
    }
    @Override
    public Board.Action nextMove(Board currentBoard)
    {
        double bestScore = Double.MIN_VALUE;
        int bestIndex = 0;

        //make a brain helper and new board
        BrainHelper brainHelper = new BrainHelper(coef);
        Board testBoard = currentBoard.testMove(Board.Action.NOTHING);
        //array to keep track of scores
        double[] score = new double[4];
        ArrayList<Board.Action> actions = new ArrayList<>();
        //use the brain helper to get the best move and score for that board, then rotate the piece and recalculate
        for(int i = 0; i < 4; i++)
        {
            score[i] = brainHelper.bestScore(testBoard);
            actions.add(brainHelper.nextMove(testBoard));
            testBoard.move(Board.Action.CLOCKWISE);
        }
        //find the best score
        for(int i = 0; i < 4; i++)
        {
            if(bestScore < score[i])
            {
                bestScore = score[i];
                bestIndex = i;
            }
        }
        //bestIndex is also the number of rotations needed
        rotations = bestIndex;
        while(rotations > 0)
        {
            rotations--;
            return Board.Action.CLOCKWISE;
        }
        return actions.get(bestIndex);
    }
}
