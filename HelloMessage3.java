import mpi.*;
public class HelloMessage3 {
	public static void main(String args[]) throws Exception {
		MPI.Init(args);
		int me = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();

		//boolean flag;

		if (me != 0) {
			char[] buf = new char[100];
			Status s = MPI.COMM_WORLD.Recv(buf,0,100,MPI.CHAR,0,0);
			String msg = "I am processor " 	+ me + " and processor 0 said \"" + new String(buf,0,s.Get_count(MPI.CHAR)) + "\" to me";
			
			MPI.COMM_WORLD.Send(msg.toCharArray(), 0,msg.length(),MPI.CHAR,0,0);
		}
		else {

			System.out.println("Hi, I am process 0, saying hi to all");
			for (int i=1;i<size;i++) {
				char[] buf = new char[100];
				String str = " hi " + i;
				MPI.COMM_WORLD.Send(str.toCharArray(), 0,str.length(),MPI.CHAR,i,0);
			}

			for (int i=1;i<size;i++) {
				char[] buf = new char[100];
				Status s = MPI.COMM_WORLD.Recv(buf,0,100,MPI.CHAR,i,0);
				System.out.println(new String(buf,0,s.Get_count(MPI.CHAR)));
			}

			
		}

		MPI.Finalize();
	}
}
