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

	if (musTake)
		return true;

	if (canPick && score(bowl, bowlId, round) >= expected)
		return true;

        return false;
    }
    

    private Random random = new Random();

    public double score(int[] bowl, int bowlId, int round){
	    double score = 0;
	    for (int i=0 ; i<bowl.length ; i++){
		    score += preference[bowl[i]];
	    }
	    return score;
    }

    public double expectedValue(int[] bowl, int bowlId, int round){
	    double expected = bowl.length * 6.5;
	    return expected;
    }
}
