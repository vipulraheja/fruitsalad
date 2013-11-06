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
    private int nplayers;
    private int[][] first_history;
    private int[][] second_history;
    private int[] first_score;
    private int[] second_score;
    private int[] pref;
    private int nfruits;

    private int accumulated_bowl_number;
    //private int[][] accumulated_bowl_history;
    private int[] accumulated_bowl_score;

    // keep adding bowls to the estimation
    private double[] smoothed_distribution_count;
    // normalized from smoothed_distribution_count by nfruits
    private double[] estimated_initial_distribution;
    // keep summing up the bowls seen
    private double[] substracted_distribution;
    // recalculate from the the above two
    private double[] estimated_remainging_distribution;

    private int maxScoreSoFar;
    private int chooseLimit;
    private int chooseLimitTwo;
    private int bowlSize;
    private double expectation;
    private int seat;
    private int totalBowls;
    private int bowlsRest;

    private int lastRound;


    // belief for initial distribution
    // The uniform distribution belief
    private double[] belief = {1.0/12.0, 1.0/12.0, 1.0/12.0, 1.0/12.0, 1.0/12.0, 1.0/12.0,
                                 1.0/12.0, 1.0/12.0, 1.0/12.0, 1.0/12.0, 1.0/12.0, 1.0/12.0};

    /*
    // The normal distribution belief
    private double[] belief = {0.03. 0.05, 0.06, 0.08, 0.1, 0.18
                                ,0.18, 0.1, 0.08, 0.06, 0.05, 0.03 };
    */                      
    // parameter for statistic updating
    private double updateWeight = 0;
    // parameter for smoothed statistic updating, "rare_count" for fruit not observed in this bowl
    private double smoothing_factor = 0.5;
    // parameter for strategy choosing
    private int bowlSizeThreshold = 0;
    // parameter for the speed to lower the expectation
    private double decreaseSpeed = 4.0;

    public void init(int nplayers, int[] pref) {
        this.nplayers = nplayers;
        this.pref = deepCopyArray( pref );
        this.nplayers = nplayers;
        first_history = new int[ nplayers ][ pref.length ];
        second_history = new int[ nplayers ][ pref.length ];
        first_score = new int[ nplayers ];
        second_score = new int[ nplayers ];
        maxScoreSoFar = 0;
        seat = getIndex();
        chooseLimit = (int)((double)(nplayers - seat) / (double)Math.E);
        chooseLimitTwo = (int)((double)(seat+1) / (double)Math.E);
        maxScoreSoFar = 0;
        bowlSize = 0;
        nfruits = 0;
        bowlsRest = -1;
        System.out.println("#############################");
        System.out.println("chooseLimit = " + chooseLimit);
        System.out.println("chooseLimitTwo = " + chooseLimitTwo);

        smoothed_distribution_count = new double[ pref.length ];
        estimated_initial_distribution = new double[ pref.length ];
        substracted_distribution = new double[ pref.length ];
        estimated_remainging_distribution = new double[ pref.length ];
        accumulated_bowl_number = 0;
        accumulated_bowl_score = new int[ 2 * nplayers ];
        lastRound = -1;
        //accumulated_bowl_history = new int[ 2*nplayers ][ pref.length ];

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

        // update the score and fruit count history for each bowl
        if (0 == round) {
            first_score[bowlId] = get_bowl_score( bowl );
            first_history[bowlId] = deepCopyArray( bowl );
        } else {
            second_score[bowlId] = get_bowl_score( bowl );
            second_history[bowlId] = deepCopyArray( bowl );
        }


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

        nfruits = nplayers * bowlSize;
        if (0 == round) {
            totalBowls = nplayers - getIndex();
        } else {
            totalBowls = getIndex() + 1;
        }
        if (lastRound != round ) {
            bowlsRest = totalBowls;
            lastRound = round;
        }
        bowlsRest--;
        // update the observed distribution
        // both the estimated initial distribution
        // and the substracted distribution
        
        accumulated_bowl_score[accumulated_bowl_number] = get_bowl_score( bowl );
        //accumulated_bowl_history[accumulated_bowl_number] = deepCopyArray( bowl );
        accumulated_bowl_number++;

        double[] smoothed_bowl = new double[bowl.length];
        smoothed_bowl = smooth_bowl(bowl);

        update_estimated_initial_distribution( accumulated_bowl_number, smoothed_bowl );
        update_substracted_distribution( totalBowls - bowlsRest - 1, smoothed_bowl );
        update_estimated_remainging_distribution();
    }

    private void assign_belief_of_initial_distribution(double[] dist) {
        for ( int i = 0; i < dist.length ; i++ ) {
            dist[i] = belief[i]*nfruits;
        }
    }

    private double[] smooth_bowl(int[] bowl) {
        double[] ret = new double[bowl.length];
        int count_of_zero = 0;
        for (int i = 0 ; i < bowl.length; i++ ) {
            if ( 0 == bowl[i]) {
                count_of_zero++;
            }
            ret[i] = (double)(bowl[i]);
        }
        double smoothing_count = smoothing_factor * 
                                (double)(count_of_zero) /
                                (double)(bowl.length - count_of_zero);

        for (int i = 0 ; i < bowl.length; i++ ) {
            if ( 0 == bowl[i]) {
                ret[i] += smoothing_factor;
            }
            else {
                ret[i] -= smoothing_count;
            }
        }
        return ret;
    }

    private void update_estimated_initial_distribution( int j, double[] bowl ) {
        // initialization
        if( 0 == j ) {
            assign_belief_of_initial_distribution(smoothed_distribution_count);
        }
        // Calculate the smoothed count
        if ( 0 == j ) {
            for ( int i = 0; i < pref.length; i++) {
                smoothed_distribution_count[i] *= updateWeight;
            }
        }
        double sum = 0;
        for ( int i = 0; i < pref.length; i++) {
            smoothed_distribution_count[i] += (double)(bowl[i]);
            sum += smoothed_distribution_count[i];
        }
        for ( int i = 0; i < pref.length; i++) {
            estimated_initial_distribution[i] = 
                smoothed_distribution_count[i] * ((double)(nfruits) / sum);
        }
    }

    // add this bowl to the substracted distribution
    private void update_substracted_distribution( int j, double[] bowl ) {
        if( 0 == j ) {
            Arrays.fill(substracted_distribution, 0); 
        }
        for (int i = 0; i < pref.length ; i++) {
            substracted_distribution[i] += bowl[i];
        }
    }

    private void update_estimated_remainging_distribution() {
        for (int i = 0; i < pref.length ; i++) {
            estimated_remainging_distribution[i] = 
                estimated_initial_distribution[i] - substracted_distribution[i];
        }
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

    private boolean small_size_strategy(int[] bowl) {
        // haven't implemented it yet
        return true;        
    }

    private double bowl_expectation() {
        // calculate the expectation for the remaining bowls
        double ret = 0;
        double sum = 0;
        for (int i = 0; i < pref.length; i++) {
            ret += (double)(pref[i]) * estimated_remainging_distribution[i];
            sum += estimated_remainging_distribution[i];
        } 
        return ret * ( bowlSize / sum );
    }

    private double bowl_SD() {
        // use the scores of bowls we have seen 
        // to estimate the SD of the scores of remaining bowls
        double ret = 0;
        double avg = 0;
        double x = 0;
        for (int i = 0; i < accumulated_bowl_number ; i++) {
            avg += (double)(accumulated_bowl_score[i]);
        }
        avg /= (double)(accumulated_bowl_number);
        for (int i = 0; i < accumulated_bowl_number ; i++) {
            x = (double)(accumulated_bowl_number) - avg;
            ret += x * x;
        }
        ret /= (double)(accumulated_bowl_score.length - 1);
        ret = Math.sqrt(ret);
        return ret;
    }

    private double good_bowl_score(double decreaseFactor) {
        System.out.println("Ex = " + bowl_expectation());
        System.out.println("SD = " + bowl_SD());
        return bowl_expectation() + bowl_SD() * decreaseFactor / decreaseSpeed;
    }

    private boolean large_size_strategy(int[] bowl) {
        
        // the distributions are updated in the beginning in the pass() function
        // we don't have to update / initialize again

        // Use the updated distribution for the decision-making
        System.out.println( "b-score = " + get_bowl_score(bowl));
        System.out.println( "bowlsRest = " + bowlsRest);
        System.out.println( "totalBowls = " + totalBowls);
        double decreaseFactor = (double)(bowlsRest) / (double)(totalBowls);
        if ( (double)(get_bowl_score(bowl)) >= good_bowl_score(decreaseFactor) ) {
            System.out.println( "g-score = " + good_bowl_score(decreaseFactor));
            return true;
        }
        System.out.println( "g-score = " + good_bowl_score(decreaseFactor));
        return false;
    }

    public boolean pass(int[] bowl, int bowlId, int round,
                        boolean canPick,
                        boolean musTake) {
        
        update_info(bowl, bowlId, round, canPick, musTake);

        print_input();

        if (musTake) return true;
        if (!canPick) return false;

        /*
        if (0 == round) {
            return first_round_strategy();
        } else {
            return second_round_strategy();
        }
        */
        // For Deliverable III, we use different strategy for different size bowls
        if (bowlSize < bowlSizeThreshold) {
            return small_size_strategy(bowl);
        } else {
            return large_size_strategy(bowl);
        }
    }

    private Random random = new Random();
}
