import mpi.*;
import java.util.Random;
import java.util.Date;
import java.nio.ByteBuffer;

public class MultiprocessorRankSortReduce {
	
	public static void main(String args[]) throws Exception {
		Random rg = new Random();
		String message = ""; 
				
		//Initialise MPI
		MPI.Init(args);
		int me = MPI.COMM_WORLD.Rank();
		int numberOfProcesses = MPI.COMM_WORLD.Size();
		
		//Initialise arrays
		final int CHUNKSIZE = 2;
		int initialArraySize = CHUNKSIZE * numberOfProcesses;
		int[] initialArray = new int[initialArraySize];
		int[] finalArray = new int[initialArraySize];
		
		//Initailise chunkArray and positionsArray
		int[] chunkArray = new int[CHUNKSIZE];
		int[] positionsArray = new int[CHUNKSIZE];
		int[] chunkArrayFromProcess = new int[CHUNKSIZE];
		int[] receivedComputedRanks = new int[CHUNKSIZE];
		int[] outputArray = new int[initialArraySize];
		
		//Initialise buffer to use with Bsend
		ByteBuffer buffer = ByteBuffer.allocate((CHUNKSIZE * numberOfProcesses * Integer.SIZE)*2);
		MPI.Buffer_attach(buffer);

		//Process 0 populates initialArray with random numbers between 1 and 50
		if(me == 0) {
			System.out.println(" ");
			for(int i = 0; i < initialArraySize; i++) {
				initialArray[i] = rg.nextInt(50) + 1;
			}
			System.out.println(printArray("initialArray: ", initialArray));
			System.out.println(" ");
		}

		//Process 0 calls Scatter method to split the array into chunks and send to processes
		MPI.COMM_WORLD.Scatter(
				initialArray, //Sending buffer - must be an array of any kind (int, long etc)
				0, 			  //offset - element in the sending buffer array where message starts
				CHUNKSIZE,	  //Number of elements to send
				MPI.INT,   	  //Datatype - type of the items in the sending buffer/array - available as a static field of the MPI class
				chunkArray,   //Receiving buffer - must be an array of any kind (int, long etc)
				0,			  //offset - element in the receiving buffer array where message starts
				CHUNKSIZE,	  //Number of elements to receive
				MPI.INT,	  //Datatype - type of the items in the receiving buffer/array 
				0);			  //Sending process
		
		System.out.println(printArray("Process " + me + " received: ", chunkArray));
		
		//Each process computes ranks for its own chunkArray
		positionsArray = rankLocal(chunkArray, positionsArray);
		
		//Each process sends its own chunk to all other processes
		//and receives chunks from other processes to compute ranks for them
		for(int i = 0; i < numberOfProcesses; i++) {
			if(me != i) {
				//Bsend() - non-blocking variant of Send()
				//With Bsend() the system will attempt to buffer messages so that Bsend() method can return immediately. 
				MPI.COMM_WORLD.Bsend(
						chunkArray,	 //buffer - sent array of type Object, i.e. must be an array of any kind (int, long etc)
						0,			 //offset - element in the buffer array where message starts
						CHUNKSIZE,   //count - number of items to send (size of numbers array in this case)
						MPI.INT,	 //Datatype - type of the items in the buffer/array - available as a static field of the MPI class
						i,			 //Rank of the process where the message is sent
						0);			 //Tag - used to identify a message out of several messages - used by the Recv method
			
				MPI.COMM_WORLD.Recv(
						chunkArrayFromProcess,	 //receiving buffer - received array
						0,			 			 //offset - element in the buffer array where the received message starts
						CHUNKSIZE, 			 	 //count - number of items to receive
						MPI.INT,	 			 //Datatype - type of the items in the buffer/array
						i,			 			 //Rank of the source process
						0);						 //Tag - used to select between several incomming messages - the call will wait until a message
												 //with a matching tag value arrives
			
				//Compute ranks for each received chunk from other processes and send it back to them
                MPI.COMM_WORLD.Bsend(
                		rank(chunkArray, chunkArrayFromProcess, me, i),
                		0,
                		CHUNKSIZE,
                		MPI.INT,
                		i,
                		1);
                
                //Receive ranks computed for this process from other processes
    			MPI.COMM_WORLD.Recv(
    					receivedComputedRanks,
    					0,
    					CHUNKSIZE,
    					MPI.INT,
    					i,
    					1);
                
				//Recalculate own ranks based on the received ranks from other processes (add up rank arrays)
				positionsArray = addRanks(positionsArray, receivedComputedRanks);
			}
		}
		
		//Put elements of the chunkArray to appropriate places in the outputArray using positionsArray
		for(int i = 0; i < chunkArray.length; i++) {
			int element = chunkArray[i];
			int position = positionsArray[i];
			outputArray[position] = element;
		}
		
		//Combine elements in send buffer of each process using the reduce operation and
		//return the combined values in the receive buffer of the root process
		MPI.COMM_WORLD.Reduce(
				outputArray,	  //Array to be send
				0,				  //Offset in array to be send
				finalArray,		  //Receiving array
				0,				  //Offset in receiving array
				initialArraySize, //Number of elements to receive
				MPI.INT,		  //Type of the elements to be received
				MPI.SUM,		  //Reduce operation
				0);				  //Root process rank
		
		if(me == 0)
			System.out.println(printArray("finalArray: ", finalArray));
		
		MPI.Finalize(); //shut down MPI
	}
	
	//Work out ranks for received chunks comparing elements from received chunk with own chunk
	public static int[] rankLocal(int[] chunkArray, int[] positionsArray) {
		for(int i = 0; i < chunkArray.length; i++) {
			int numberOfSmallerElements = 0;
			
			for(int j = 0; j < chunkArray.length; j++) {
				if(chunkArray[j] < chunkArray[i] || (j < i && chunkArray[j] == chunkArray[i]))
					numberOfSmallerElements++;
			}
			
			positionsArray[i] = positionsArray[i] + numberOfSmallerElements;
		}
		
		return positionsArray;
	}
	
	//Work out ranks for received chunks comparing elements from received chunk with own chunk
	public static int[] rank(int[] chunkArray, int[] chunkArrayFromProcess, int myRank, int processRank) {
		int[] ranks = new int[chunkArray.length];
				
		for(int i = 0; i < chunkArray.length; i++) {
			int numberOfSmallerElements = 0;
			
			for(int j = 0; j < chunkArray.length; j++) {
				if(chunkArray[j] < chunkArrayFromProcess[i] || myRank < processRank && chunkArray[j] == chunkArrayFromProcess[i])
					numberOfSmallerElements++;
			}
			
			ranks[i] = numberOfSmallerElements;
		}
		
		return ranks;
	}
	
	//Recalculate own ranks based on the received ranks from other processes (add up rank arrays)
	public static int[] addRanks(int[] positionsArray, int[] receivedComputedRanks) {
		for(int i = 0; i < positionsArray.length; i++) {
			positionsArray[i] = positionsArray[i] + receivedComputedRanks[i];
		}
		
		return positionsArray;
	}
	
	public static String printArray(String message, int[] a) {
		String s = message + ": ";
		for(int i = 0; i < a.length; i++) {
			s += "|" + a[i];
		}
		s +="|";
		return s;
	}
}