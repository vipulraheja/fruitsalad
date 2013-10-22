package fruit.g7;

import java.util.*;


public class Player extends fruit.sim.Player
{
    int[] preference;

    public void init(int nplayers, int[] pref) {
	    preference = pref;
    }

    public boolean pass(int[] bowl, int bowlId, int round,
                        boolean canPick,
                        boolean musTake) {
	double expected = expectedValue(bowl, bowlId, round);

	System.out.println("Pref:\n");
	for (int i=0 ; i<preference.length ; i++)
		System.out.print(preference[i] + ", ");

	if (musTake)
		return true;

	if (canPick && score(bowl, bowlId, round) >= expected)
		return true;

        return false;
    }
    

    private Random random = new Random();

    public int score(int[] bowl, int bowlId, int round){
	    int score = 0;
	    for (int i=0 ; i<bowl.length ; i++){
		    score += preference[i] * bowl[i];
	    }
	    System.out.println("Score: " + score);
	    return score;
    }

    public double expectedValue(int[] bowl, int bowlId, int round){
	    
	    int bowlSize = 0;
	    for(int i=0; i<bowl.length; i++)
	    	bowlSize += bowl[i];

	    double expected = bowlSize * 6.5;
	    System.out.println("Exp: " + expected);
	    return expected;
    }
}
