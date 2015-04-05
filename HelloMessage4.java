import mpi.*;
public class HelloMessage4 {
	public static void main(String args[]) throws Exception {
		MPI.Init(args);
		int me = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();


		final int ARRAYSIZE = 100;

		

		//boolean flag;

		if (me != 0) {
			int [] buf = new int [ARRAYSIZE];
			Status s = MPI . COMM_WORLD.Recv(buf, 0, ARRAYSIZE, MPI.INT, 0, 0);
			//String msg = "I am processor " 	+ me + " and processor 0 said \"" + new String(buf,0,s.Get_count(MPI.CHAR)) + "\" to me";			
			//MPI.COMM_WORLD.Send(msg.toCharArray(), 0,msg.length(),MPI.CHAR,0,0);

			int numInts = s.Get_count(MPI.INT);
			int total = 0;
			for ( int i =0; i < numInts ; i ++)
				total += buf[i];

			MPI.COMM_WORLD.Send(new int[]{ total }, 0, 1, MPI.INT,0 ,0);
		}
		else {

			System.out.println("Hi, I am process 0, sending randomly generated list of ints to each processor");

			Random rand = new Random ();
			int list[] = new int[ARRAYSIZE * ( size -1)];

			for ( int i=0; i < list.length ; i++)
				list[i] = rg.nextInt();

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
			System.out.println(result);
		}

		MPI.Finalize();
	}
}
