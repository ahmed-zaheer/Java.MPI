import mpi.*;
public class HelloMessage {
	public static void main(String args[]) throws Exception {
		MPI.Init(args);
		int me = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();

		if (me != 0) {
			String msg = "Greetings from process " 
				+ me + " of " + size + ".";
			MPI.COMM_WORLD.Send(msg.toCharArray(),
					0,msg.length(),MPI.CHAR,0,0);
		}
		else {
			System.out.println("Greetings from process 0 of " + size + ".");
			for (int i=1;i<size;i++) {
				char[] buf = new char[100];
				Status s = MPI.COMM_WORLD.Recv(buf,0,100,MPI.CHAR,i,0);
				System.out.println(new String(buf,0,s.Get_count(MPI.CHAR)));
			}
		}

		MPI.Finalize();
	}
}
