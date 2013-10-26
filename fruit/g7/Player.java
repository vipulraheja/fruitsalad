package fruit.g7;

import java.util.*;

public class Player extends fruit.sim.Player
{
    int[] preference;
    int n_players;
    int n_bowls;
    private int[][] fruitHistory;

    // Since there are 12 Players
    private static int NUM_FRUITS = 12;
   
    public void init(int nplayers, int[] pref) {
	    preference = pref;
	    n_players = nplayers;

	    // Since there are 2 rounds
	    n_bowls = nplayers*2;

	    fruitHistory = new int[n_bowls][NUM_FRUITS];
	    for (int i=0 ; i<n_bowls ; i++){
		    for (int j=0 ; j<NUM_FRUITS ; j++){
			    fruitHistory[i][j] = 0;
		    }
	    }
    }
    
    public boolean pass(int[] bowl, int bowlId, int round,
                        boolean canPick,
                        boolean musTake) {
	double expected = expectedValue(bowl, bowlId, round);
	
	fruitHistory = history(bowl, bowlId, round);
	
	for (int i=0 ; i<preference.length ; i++)
		System.out.print(preference[i] + ", ");

	if (musTake)
		return true;

	if (canPick && score(bowl, bowlId, round) >= expected)
		return true;

        return false;
    }

    private int[][] history(int[] bowl, int bowlId, int round){
	    for (int i=0 ; i<bowl.length ; i++){
		    fruitHistory[round*n_players + bowlId][i] = bowl[i];
	    }
    
	    return fruitHistory;
    }

    private Random random = new Random();

    private int score(int[] bowl, int bowlId, int round){
	    int score = 0;
	    for (int i=0 ; i<bowl.length ; i++){
		    score += preference[i] * bowl[i];
	    }
	    System.out.println("Score: " + score);
	    return score;
    }

    private double expectedValue(int[] bowl, int bowlId, int round){
	    
	    int bowlSize = 0;
	    for(int i=0; i<bowl.length; i++)
	    	bowlSize += bowl[i];

	    double expected = bowlSize * 6.5;
	    System.out.println("Exp: " + expected);
	    return expected;
    }
}
