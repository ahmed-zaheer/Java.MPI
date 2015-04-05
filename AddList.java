import mpi.*;
import java.util.Random;
import java.util.Arrays;

public class AddList {

	public static void main(String args[]) throws Exception {

		MPI.Init(args);
		int me = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();

		final int ARRAYSIZE = 10;

		if (me != 0) {
			int [] buf = new int [ARRAYSIZE];
			Status s = MPI . COMM_WORLD.Recv(buf, 0, ARRAYSIZE, MPI.INT, 0, 0);
			int numInts = s.Get_count(MPI.INT);

			int total = 0;
			for ( int i =0; i < numInts ; i ++)
				total += buf[i];

			String msg = "Hi, I am process '" + me +"', and have received a list of ints from processor '0'\n";
 			System.out.println(msg +  Arrays.toString(buf) + " -> Total=" + total);
			MPI.COMM_WORLD.Send(new int[]{ total }, 0, 1, MPI.INT,0 ,0);
		}
		else {

			Random rand = new Random();
			int list[] = new int[ARRAYSIZE * ( size -1)];

			for ( int i=0; i < list.length ; i++)
				list[i] = rand.nextInt(100);

			for (int i=1;i<size;i++) {
				MPI.COMM_WORLD.Send(list, (i -1)* ARRAYSIZE, ARRAYSIZE, MPI.INT, i, 0);
			}

			Status s;
			int result = 0;
			for ( int i =1; i < size ; i ++) {
				s = MPI.COMM_WORLD.Recv(list, 0, 1, MPI.INT, i, 0);
				if (s.Get_count(MPI.INT) == 1)
					result += list[0];
			}
			System.out.println("\nHi, I am process 0, and I have sent randomly generated list of ints to each processor");
			System.out.println("Total of All list = " + result);
		}

		MPI.Finalize();
	}
}
