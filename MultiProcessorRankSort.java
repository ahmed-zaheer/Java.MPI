import mpi.*;
import java.util.Date;
import java.util.Random;
import java.util.Arrays;

public class MultiProcessorRankSort{
	
	public static void main(String args[]) throws Exception {


		//MPI Initialisation
		MPI.Init(args);
		int me = MPI.COMM_WORLD.Rank();
		int processorsCount = MPI.COMM_WORLD.Size();	// n processors

		final int CHUNKSIZE = 3;	// 'c' is chunksize
		final int ARRAYSIZE = CHUNKSIZE * processorsCount; 	// n*c
		final int ROOT = 0;

		int []in = new int[ARRAYSIZE];		
		int []out = new int[ARRAYSIZE];

		if (me == 0) {

			Random rand = new Random();
			for ( int i=0; i < in.length ; i++)			
				in[i] = rand.nextInt(20);

			System.out.println("Random unsorted list " +  Arrays.toString(in));
		}
	
		long start = new Date().getTime();

		out = rankSort(in, CHUNKSIZE);

		long finish = new Date().getTime();

		if(me == 0)
		{
			System.out.println("Sorted list " +  Arrays.toString(out));
		}
		

		long time = finish - start;
		if(me == 0)
			System.out.println("\nSorting " + ARRAYSIZE + " took " + time + " ms");


		MPI.Finalize();
	}

	public static int[] rankSort(int[] in, int chunkSize) {

		int me = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();

		int[] ranks = new int[in.length];
		int[] out = new int[in.length];
		
		MPI.COMM_WORLD.Bcast(in, 0, in.length, MPI.INT, 0);

		int startIndex = me * chunkSize;	// i*c
		int endIndex = (me+1) * chunkSize -1 ; 	//(i+1)*c - 1

		
		for(int i = startIndex; i <= endIndex; i++) {
			int sum = 0;
			for(int j = 0; j < in.length; j++) {
				if(in[i] > in[j] || (i > j && in[i] == in[j]))
					sum++;
			}
			ranks[i] = sum;
		}
		
		MPI.COMM_WORLD.Reduce(ranks, 0, ranks, 0, ranks.length, MPI.INT, MPI.SUM, 0);
		
		if(me == 0)
		{
			for(int i = 0; i < ranks.length; i++)
				out[ranks[i]] = in[i];
		}
		return out;
	}









}
