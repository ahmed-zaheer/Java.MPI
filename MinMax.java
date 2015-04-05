import mpi.*;
import java.util.Random;
import java.util.Arrays;

public class MinMax {
	public static void main(String args[]) throws Exception {

		MPI.Init(args);
		int me = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();

		final int CHUNKSIZE = 2;
		final int ROOT = 0;


		int [] bigBuf = new int [CHUNKSIZE*size];

		if(me==ROOT)
		{
			Random rand = new Random();
			for ( int i = 0; i < bigBuf.length ; i++)
				bigBuf [i] = rand.nextInt(50);

			System.out.println("Random number list" + Arrays.toString(bigBuf));

		}
		int [] smallBuf =new int[CHUNKSIZE];
		int localMax =0;
		int localMin = 0;
		int [] minBuf =new int [2];

		//Sends the chunks for other process
		MPI.COMM_WORLD.Scatter(bigBuf,0,CHUNKSIZE, MPI.INT,smallBuf ,0,CHUNKSIZE,MPI.INT,ROOT );

		
		for ( int i =0; i < smallBuf.length ; i ++)	//Find the local min & max
		{
			if (i==0)
				localMin = localMax = smallBuf[i];
			else {
				if(smallBuf[i]> localMax) localMax = smallBuf[i];
				if(smallBuf[i]< localMin) localMin = smallBuf[i];
			}
		}
			
		System.out.println("Proocessor " +  me +" = " + Arrays.toString(smallBuf));

		System.out.println("Local Min of " + me + " is " + localMin);
		System.out.println("Local Max of " + me + " is " + localMax);

		MPI.COMM_WORLD.Reduce ( new int []{ localMin } ,0 ,minBuf ,0,1,MPI.INT ,MPI.MIN,ROOT );
		MPI.COMM_WORLD.Reduce ( new int []{ localMax} ,0,bigBuf ,0,1,MPI.INT ,MPI.MAX,ROOT );

		if ( me == ROOT ){
			System.out.println("Maximum is : " + bigBuf [0]);
			System.out.println("Minimum is : " + minBuf [0]);
		}

		MPI.Finalize ();

	}
}
