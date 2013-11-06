package fruit.g5;
import java.io.*;
     
public class Player extends fruit.sim.Player
{
    final int typesOfFruit = 12;

	int nplayers, index, choice = -1;
	int[] pref;
	int[][] choicelist;

    int bowlSize;
    int totalFruitSeen = 0;
    int firstRoundBowlsSeen = 0;
    int secondRoundBowlsSeen = 0;
    int[][] firstRoundBowlHistory;
    int[][] secondRoundBowlHistory;

    boolean firstBowlReceived = false;

	public void init(int nplayers, int[] pref) {
		this.nplayers = nplayers;
		this.pref = pref;
		this.index = getIndex();

		this.choicelist = new int[nplayers + 1][pref.length];

        // Recording the history of bowls we've seen
        this.firstRoundBowlHistory = new int[nplayers - getIndex()][typesOfFruit];
        this.secondRoundBowlHistory = new int[getIndex() + 1][typesOfFruit];
	}

    public boolean pass(int[] bowl, int bowlId, int round, boolean canPick, boolean musTake) {
        updateStats(bowl, round);

        if (totalFruitSeen >= 60) {
            return distributionStrategy(bowl);
        } else {
            return stoppingStrategy(bowl, round);
        }
    }

	public boolean stoppingStrategy(int[] bowl, int round) {
		int choicesLeft, cutoff = 0;
		boolean pickFlag = false;
		
		choice++;
		choicelist[choice] = bowl.clone();

		if (round == 0) {
			choicesLeft = nplayers - index - choice;
			if((choice + 1) > Math.round((nplayers - index) / Math.E))
				pickFlag = true;
		} else {
			choicesLeft = nplayers + 1 - choice;
			if((choice + 1) > Math.round((nplayers + 1) / Math.E))
				pickFlag = true;
		}
		
		for (int i = choice - 1, j = 0; i > -1 && j < Math.round(choicesLeft / (Math.E - 1));  i--, j++) {
			if(bowlScore(choicelist[i]) > cutoff)
				cutoff = bowlScore(choicelist[i]);
		}
		
		if((pickFlag && bowlScore(bowl) > cutoff) || (bowlScore(bowl) > (int) (9.0 * bowlSize)))
			return true;

		return false;
	}

    public boolean distributionStrategy(int[] bowl) {
        predictDistribution();
        double expectedScore = calculateExpectedScore();

        // Take a bowl greater than your expected score based on the distribution
        if (bowlScore(bowl) > expectedScore + .05 * expectedScore)
            return true;

        return false;
    }

    /**
     * Count the number of each fruit and put it as a fraction over the total number of fruit seen.
     * This is the predicted distribution, given as a probability of each fruit
     * @return An array of probabilities of each fruit
     */
    public double[] predictDistribution() {
        int[] fruitCounts = new int[12];
        int totalFruit = 0;
        double[] fruitProbabilities = new double[12];

        for (int i = 0; i < firstRoundBowlHistory.length; i++) {
            for (int j = 0; j < firstRoundBowlHistory[i].length; j++) {
                fruitCounts[j] += firstRoundBowlHistory[i][j];
            }
        }

        for (int i = 0; i < secondRoundBowlHistory.length; i++) {
            for (int j = 0; j < secondRoundBowlHistory[i].length; j++) {
                fruitCounts[j] += secondRoundBowlHistory[i][j];
            }
        }

        for (int i: fruitCounts) {
            totalFruit += i;
        }

        for (int i = 0; i < fruitCounts.length; i++) {
            fruitProbabilities[i] = (double) fruitCounts[i] / totalFruit;
        }

        return fruitProbabilities;
    }

    /**
     * Using the predicted distribution, calculated your expected score based on your preferences
     * @return The expected score
     */
    public double calculateExpectedScore() {
        double score = 0;
        double[] fruitProbabilities = predictDistribution();
        for (int i = 0; i < typesOfFruit; i++) {
            score += fruitProbabilities[i] * bowlSize * pref[i];
        }
        return score;
    }

    /**
     * Keeps track of all the bowls you've seen so far
     * @param bowl A fruit bowl
     * @param round The round number
     */
    public void updateStats(int[] bowl, int round)
    {
        // Once you receive the first bowl, calculate the size of a bowl
        if (!firstBowlReceived) {
            for (int i = 0; i < bowl.length; i++) {
                this.bowlSize += bowl[i];
            }
            firstBowlReceived = true;
        }

        totalFruitSeen += this.bowlSize;

        if (round == 0) {
            firstRoundBowlHistory[firstRoundBowlsSeen] = bowl;
            firstRoundBowlsSeen++;
        } else if (round == 1) {
            secondRoundBowlHistory[secondRoundBowlsSeen] = bowl;
            secondRoundBowlsSeen++;
        }
    }

    /**
     * Calculates the score of a bowl
     * @param bowl A fruit bowl
     * @return The score of the bowl
     */
	public int bowlScore(int[] bowl)
	{
		int score = 0;
		for (int i = 0; i < bowl.length; i++)
		{
			score += pref[i] * bowl[i];
		}
		return score;
	}
}

