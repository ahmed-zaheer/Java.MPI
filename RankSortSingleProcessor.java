import java.util.Date;
import java.util.Random;
import java.util.Arrays;

public class RankSortSingleProcessor {

	public static void main(String args[]) throws Exception {

		final int datasize = 10;
		Random rand = new Random();
		int [] unsorted = new int[datasize];
		int [] sorted = new int[datasize];

		for (int i = 0; i < datasize ; i++)
			unsorted[i] = rand.nextInt(100);

		System.out.println("Random unsorted list " +  Arrays.toString(unsorted));

		int count = 0;
    		long start = new Date().getTime();

		for ( int i =0 ;i < datasize ; i ++){
			count = 0;
			for ( int j = 0; j < datasize ; j++) {
				if ( unsorted[j] < unsorted[i] || (j < i && unsorted[j] == unsorted[i]) )
					count++;
			}
			sorted [count] = unsorted [ i ];
		}

		long finish = new Date().getTime();
	  	long time = finish - start;

		System.out.println("Random sorted list " +  Arrays.toString(sorted));

		System.out.println("\nSorting " + datasize + " took " + time + " milliseconds");
	}
}
