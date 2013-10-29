package fruit.g9;

import java.util.*;

public class Player extends fruit.sim.Player
{
    // current bowl information
    private int[] bowl;
    private int bowlId;
    private int round;
    boolean canPick;
    boolean musTake;

    // statistics
    private int[][] first_history;
    private int[][] second_history;
    private int[] first_score;
    private int[] second_score;
    private int[] pref;
    private int nplayers;
    private int maxScoreSoFar;
    private int chooseLimit;
    private int bowlSeenFirstRound;
    private int bowlSize;
    private double expectation;

    public void init(int nplayers, int[] pref) {
        bowlSeenFirstRound = 0;
        this.pref = deepCopyArray( pref );
        this.nplayers = nplayers;
        first_history = new int[ nplayers ][ pref.length ];
        second_history = new int[ nplayers ][ pref.length ];
        first_score = new int[ nplayers ];
        second_score = new int[ nplayers ];
        maxScoreSoFar = 0;
        chooseLimit = (int)((double)nplayers / (double)Math.E);
        maxScoreSoFar = 0;
        bowlSize = 0;
        System.out.println("chooseLimit = " + chooseLimit);
    }

    private int[] deepCopyArray(int[] a) {
        int[] b = new int[ a.length ];
        for (int i=0; i<a.length; i++)
            b[i] = a[i];
        return b;
    }

    // get the score of a given bowl
    int get_bowl_score(int[] bowl) {
        int ret = 0;
        for (int i=0; i<bowl.length; i++)
            ret += bowl[i] * pref[i];
        return ret;
    }

    private void update_info(int[] bowl, int bowlId, int round,
                        boolean canPick,
                        boolean musTake) {
        this.bowl = deepCopyArray( bowl );
        this.bowlId = bowlId;
        this.round = round;
        this.canPick = canPick;
        this.musTake = musTake;

        if (0 == round) {
            first_score[bowlId] = get_bowl_score( bowl );
            first_history[bowlId] = deepCopyArray( bowl );
        } else {
            second_score[bowlId] = get_bowl_score( bowl );
            second_history[bowlId] = deepCopyArray( bowl );
        }

        if (0 == round) bowlSeenFirstRound++;
        if (0 == bowlSize) {
            for (int i=0; i<bowl.length; i++)
                bowlSize += bowl[i];
            expectation = 0.0;
            for (int i=0; i<pref.length; i++)
                expectation += pref[i] * bowlSize / 12.0; 
            if (maxScoreSoFar == 0) maxScoreSoFar = (int)expectation;
            System.out.println("expectation g9 = " + expectation);
        }
        // we can update any information we want here
    }

    private boolean first_round_strategy() {
        if (bowlId < chooseLimit) {
            maxScoreSoFar = Math.max( maxScoreSoFar, get_bowl_score(bowl) );
            System.out.println("Not reached chooseLimit yet, updated maxScoreSoFar = " + maxScoreSoFar);
            return false;
        } else {
            if (get_bowl_score(bowl) >= maxScoreSoFar) {
                System.out.println("Score = " + get_bowl_score(bowl) + " better than maxScoreSoFar = " + maxScoreSoFar + ", choose it!");
                return true;
            }
        }
        System.out.println(get_bowl_score(bowl) + "is worse than maxScoreSoFar " + maxScoreSoFar +  " , pass it");
        return false;
    }

    private boolean second_round_strategy() {
        if (bowlId == 0) maxScoreSoFar = 0;
        int chooseLimitTwo = (int) ((double)(nplayers - bowlSeenFirstRound) / (double)Math.E);
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\nbowlSeenFirstRound = " + bowlSeenFirstRound);
        System.out.println("nplayers = " + nplayers + ", bowlSeenFirstRound = " + bowlSeenFirstRound + ", Math.E = " + Math.E );
        System.out.println("chooseLimitTwo = " + chooseLimitTwo);
        System.out.println("bowlId = " + bowlId);
        if (bowlId < chooseLimitTwo) {
            maxScoreSoFar = Math.max( maxScoreSoFar, get_bowl_score(bowl) );
            System.out.println("Not reached chooseLimit yet, updated maxScoreSoFar = " + maxScoreSoFar);
            return false;
        } else {
            if (get_bowl_score(bowl) >= maxScoreSoFar) {
                System.out.println("Score = " + get_bowl_score(bowl) + " better than maxScoreSoFar = " + maxScoreSoFar + ", choose it!");
                return true;
            }
        }
        System.out.println("Worse than maxScoreSoFar, pass it");
        return true;
    }

    private void print_array(int[] a) {
        if ( 0 == a.length ) return ;
        System.out.print("[");
        System.out.print(a[0]);
        for (int i=1; i<a.length; i++)
            System.out.print(", " + a[i]);
        System.out.println("]");
    }

    private void print_input() {
        System.out.println("#############################");
        print_array( bowl );
        System.out.println(bowlId);
        System.out.println(round);
        System.out.println(canPick);
        System.out.println(musTake);
    }

    public boolean pass(int[] bowl, int bowlId, int round,
                        boolean canPick,
                        boolean musTake) {
        
        
        update_info(bowl, bowlId, round, canPick, musTake);

        print_input();

        if (musTake) return true;
        if (!canPick) return false;

        if (0 == round) {
            return first_round_strategy();
        } else {
            return second_round_strategy();
        }
    }

    private Random random = new Random();
}
