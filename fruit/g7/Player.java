package fruit.g7;

import java.util.*;

public class Player extends fruit.sim.Player
{
    int[] preference;
    int n_players;
    int n_bowls;
    int bowlSize;
    int n_fruits;
    int[] bowls_seen;

    private double[] fruit_probs;

    // Store all bowls that the player observes
    private int[][] fruitHistory;

    // An initial distribution to start with
    private int[] originalDistribution;

    // Current distribution as per our estimate
    private int[] currentDistribution;

    private Random random = new Random();

    // Since there are 12 Fruits
    private static int NUM_FRUITS = 12;

    public void init(int nplayers, int[] pref) {
	    preference = pref;
	    n_players = nplayers;
	    bowls_seen = new int[2];

	    // Since there are 2 rounds
	    n_bowls = nplayers*2;

	    originalDistribution = new int[NUM_FRUITS];
	    currentDistribution = new int[NUM_FRUITS];
	    fruitHistory = new int[n_bowls][NUM_FRUITS];

	    for (int i=0 ; i<n_bowls ; i++){
		    for (int j=0 ; j<NUM_FRUITS ; j++){
			    fruitHistory[i][j] = 0;
		    }
	    }

	    fruit_probs = new double[NUM_FRUITS];
    }

    public boolean pass(int[] bowl, int bowlId, int round,
                        boolean canPick,
                        boolean musTake) {
		double expected;
	
		// Calculate Bowl Size
		bowlSize = 0;
		for(int i=0; i<bowl.length; i++)
			bowlSize += bowl[i];

		// Total Fruits
		n_fruits = n_players*bowlSize;

		estimateDistribution(bowl, bowlId, round, bowlSize);

		expected = expectedValue(bowl, bowlId, round);
	
		if (musTake)
			return true;

		if (canPick && score(bowl, bowlId, round) >= expected)
			return true;
		else
			return false;
    }

    private int[][] history(int[] bowl, int bowlId, int round){
	    for (int i=0 ; i<bowl.length ; i++){
		    fruitHistory[round*n_players + bowlId][i] = bowl[i];
	    }

	    return fruitHistory;
    }

    private int score(int[] bowl, int bowlId, int round){
	    int score = 0;
	    for (int i=0 ; i<bowl.length ; i++){
		    score += preference[i] * bowl[i];
	    }
	    System.out.println("Score: " + score);
	    return score;
    }

    private void showBowl(int[] bowl) {
	    String str = "|";
	    for (int i = 0; i < bowl.length; i++) {
		    str += " " + bowl[i] + " |";
	    }
	    str += "\n";
	    System.out.println(str);
    }


    private double expectedValue(int[] bowl, int bowlId, int round){
	    int bowlSize = 0;
	    for(int i=0; i<bowl.length; i++)
		    bowlSize += bowl[i];

	    double avgFruitScore = 0;
	    int totFruitsInDistribution = 0;
	    for(int i=0; i<NUM_FRUITS; i++){
		    if(currentDistribution[i] > 0){
			    totFruitsInDistribution += currentDistribution[i];
			    avgFruitScore += preference[i] * currentDistribution[i];
		    }
	    }

	    //if for some reason totFruitsInDistrubtion == 0
	    //assume uniform
	    if(avgFruitScore == 0)
		    avgFruitScore = 6.5;
	    else
		    avgFruitScore /= totFruitsInDistribution;

	    double expected = bowlSize * avgFruitScore;
	    System.out.println("Exp: " + expected);
	    return expected;
    }

    private void estimateDistribution(int[] bowl, int bowlId, int round, int bowlSize){
	    // Initially, create a uniform distribution
	    for (int i=0; i < NUM_FRUITS ; i++) {
		    originalDistribution[i] = Math.round(n_fruits/NUM_FRUITS);

		    // Assign uniform probability
		    if (bowls_seen[0] == 0 || bowls_seen[1] == 0)
		    {
			    java.util.Arrays.fill(fruit_probs, 1.0 / NUM_FRUITS);
		    }
	    }

	    // Update fruit history as bowls come by
	    fruitHistory = history(bowl, bowlId, round);
	    bowls_seen[round]++;

	    if (bowls_seen[0] > 0 || bowls_seen[1] > 0)
	    {
		    // Update Probabilities
		    double prob_sum = 0.0;
		    for (int i = 0; i < NUM_FRUITS ; i++) {
			    fruit_probs[i] += (fruitHistory[round*n_players + bowlId][i]*1.0/bowlSize);
//			    System.out.println("Seen: " + fruit_probs[i] + " No: " + fruitHistory[round*n_players + bowlId][i]);
//			    System.out.println(" prob: " + fruitHistory[round*n_players + bowlId][i]*1.0/bowlSize);
			    prob_sum += fruit_probs[i];
		    }
//		    System.out.println(prob_sum);
	    }

//	    for (int i = 0; i < NUM_FRUITS ; i++) {
//		    System.out.print(fruit_probs[i] + "||");
//	    }

	    // generate a platter based on estimated probabilties of each fruit
	    for (int i=0; i < NUM_FRUITS ; i++) {
		    currentDistribution[i] = (int) Math.round(n_fruits * fruit_probs[i]);
	    }

	    //		showBowl(originalDistribution);
	    //		showBowl(currentDistribution);

	    for (int i=0; i < NUM_FRUITS ; i++) {
		    originalDistribution[i] -= currentDistribution[i];
	    }

    }

    private void estimateDistribution_orig(){
	    int totalSelections = 0;

	    // Number of times each fruit category has been selected for distribution
	    int[] selectedFruits = new int[NUM_FRUITS];

	    //i Number of fruit of each category has been distributed
	    int[] distributedFruits = new int[NUM_FRUITS];

	    for(int i = 0;i<fruitHistory.length;i++)
	    {
		    for(int j = 0;j<fruitHistory[i].length;j++)
		    {
			    if(fruitHistory[i][j]!=0){
				    totalSelections++;
				    selectedFruits[j]++;

				    distributedFruits[j] += fruitHistory[i][j];
			    }
		    }
	    }

	    for(int i = 0;i<distributedFruits.length;i++)
	    {
		    if(totalSelections==0)
			    originalDistribution[i] = 0;
		    else
			    // For each fruit determine the percentage of chosen fruits
			    // which are this fruit and multiply by the total number
			    // of fruit in the bowl being selected from
			    originalDistribution[i] = (int) Math.round(distributedFruits[i]/(bowlSize));
			    System.out.println("Estimate of original distribution of fruit " + i + " = " + originalDistribution[i]);

			    //Estimate the current distribution by substracting the distributed fruits from the estimated original distribution
			    currentDistribution[i] = originalDistribution[i] - distributedFruits[i];
			    
			    System.out.println("Estimate of current distribution of fruit " + i + " = " + currentDistribution[i]);
	    }
	    for(int i=0;i<distributedFruits.length;i++)
		    System.out.println("curr Dist: "+currentDistribution[i]);
    }
}
