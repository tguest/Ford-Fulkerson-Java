/* NetworkFlow.java
   CSC 226 - Fall 2014
   Assignment 4 - Max. Flow Template
   
   This template includes some testing code to help verify the implementation.
   To interactively provide test inputs, run the program with
	java NetworkFlow
	
   To conveniently test the algorithm with a large input, create a text file
   containing one or more test graphs (in the format described below) and run
   the program with
	java NetworkFlow file.txt
   where file.txt is replaced by the name of the text file.
   
   The input consists of a series of directed graphs in the following format:
   
    <number of vertices>
	<adjacency matrix row 1>
	...
	<adjacency matrix row n>
	
   Entry A[i][j] of the adjacency matrix gives the capacity of the edge from 
   vertex i to vertex j (if A[i][j] is 0, then the edge does not exist).
   For network flow computation, the 'source' vertex will always be vertex 0 
   and the 'sink' vertex will always be vertex 1.
	
   An input file can contain an unlimited number of graphs; each will be 
   processed separately.


   B. Bird - 07/05/2014
   MODIFIED BY TREVOR GUEST V00807662 ON NOVEMBER 12TH 2015
*/

import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;
import java.io.File;

//Do not change the name of the NetworkFlow class
public class NetworkFlow{

	/* MaxFlow(G)
	   Given an adjacency matrix describing the structure of a graph and the 
	   capacities of its edges, return a matrix containing a maximum flow from
	   vertex 0 to vertex 1 of G.
	   In the returned matrix, the value of entry i,j should be the total flow
	   across the edge (i,j).
	*/
	static int[][] MaxFlow(int[][] G){
		int numVerts = G.length;

		//START OF MY CODE -- TREVOR GUEST
		
		int[][] CFlow = new int[G.length][G.length]; //init current flow all zero


		while(AugPath(G, CFlow)){ //G is used as residual flow matrix
			//runs augpath until it returns false (no augmenting path = we can stop)
		}

		return CFlow;//returns the matrix with max flow
		
	}

	public static boolean AugPath (int[][] RFlow, int[][] CFlow) {//modified bfs
																  //did the rest in here because scope was convenient
		boolean[] visited = new boolean[RFlow.length];
		int[] discovered = new int[RFlow.length];
		QList queue = new QList();

		queue.add(new QNode(0));
		visited[0] = true;

		while(queue.head!=null && !visited[1]){//bfs part
			int u = queue.dequeue();

			for (int i = 0; i < RFlow.length; i++){
				if(RFlow[u][i]>0 && !visited[i]){
					visited[i] = true;
					discovered[i] = u;
					queue.add(new QNode(i));
				}
			}
		}
		if (visited[1]){//compute bottleneck capacity and update
			int j = 1;
			int bneck = RFlow[discovered[j]][j];

			while(discovered[j] != 0){//bottleneck
				j = discovered[j];
				int k = discovered[j];

				if (RFlow[k][j] < bneck){
					bneck = RFlow[k][j];
				}
			}

			j = 1;
			while(j != 0){//go along the path again
				int k = discovered[j];
				//update matrices
				RFlow[k][j] -= bneck;
				RFlow[j][k] += bneck;
				CFlow[k][j] += bneck;

				j = discovered[j];
			} 
		}

		return visited[1];
	}


	public static class QNode {//for queue
		int value;
		QNode next;

		public QNode(int v){
			value = v;
			next = null;
		}
	}

	public static class QList {//for queue
		QNode head;
		QNode tail;

		public QList(){
			head = null;
			tail = null;
		}

		public void add(QNode n){
			if (head == null){
				head = n;
				tail = n;
			} else {
				tail.next = n;
				tail = tail.next;
			}
		}

		public int dequeue(){
			if (head == null){
				return -1;
			}
			int v = head.value;

			if (head == tail){
				head = null;
				tail = null;
			} else {
				head = head.next;		
			}
			return v;
		}
	}
	
	//END OF MY CODE

	public static boolean verifyFlow(int[][] G, int[][] flow){
		
		int n = G.length;
		
		//Test that the flow on each edge is less than its capacity.
		for (int i = 0; i < n; i++){
			for (int j = 0; j < n; j++){
				if (flow[i][j] < 0 || flow[i][j] > G[i][j]){
					System.err.printf("ERROR: Flow from vertex %d to %d is out of bounds.\n",i,j);
					return false;
				}
			}
		}
		
		//Test that flow is conserved.
		int sourceOutput = 0;
		int sinkInput = 0;
		for (int j = 0; j < n; j++)
			sourceOutput += flow[0][j];
		for (int i = 0; i < n; i++)
			sinkInput += flow[i][1];
		
		if (sourceOutput != sinkInput){
			System.err.printf("ERROR: Flow leaving vertex 0 (%d) does not match flow entering vertex 1 (%d).\n",sourceOutput,sinkInput);
			return false;
		}
		
		for (int i = 2; i < n; i++){
			int totalIn = 0, totalOut = 0;
			for (int j = 0; j < n; j++){
				totalIn += flow[j][i];
				totalOut += flow[i][j];
			}
			if (totalOut != totalIn){
				System.err.printf("ERROR: Flow is not conserved for vertex %d (input = %d, output = %d).\n",i,totalIn,totalOut);
				return false;
			}
		}
		return true;
	}
	
	public static int totalFlowValue(int[][] flow){
		int n = flow.length;
		int sourceOutput = 0;
		for (int j = 0; j < n; j++)
			sourceOutput += flow[0][j];
		return sourceOutput;
	}
	
	/* main()
	   Contains code to test the MaxFlow function. You may modify the
	   testing code if needed, but nothing in this function will be considered
	   during marking, and the testing process used for marking will not
	   execute any of the code below.
	*/
	public static void main(String[] args){
		Scanner s;
		if (args.length > 0){
			try{
				s = new Scanner(new File(args[0]));
			} catch(java.io.FileNotFoundException e){
				System.out.printf("Unable to open %s\n",args[0]);
				return;
			}
			System.out.printf("Reading input values from %s.\n",args[0]);
		}else{
			s = new Scanner(System.in);
			System.out.printf("Reading input values from stdin.\n");
		}
		
		int graphNum = 0;
		double totalTimeSeconds = 0;
		
		//Read graphs until EOF is encountered (or an error occurs)
		while(true){
			graphNum++;
			if(graphNum != 1 && !s.hasNextInt())
				break;
			System.out.printf("Reading graph %d\n",graphNum);
			int n = s.nextInt();
			int[][] G = new int[n][n];
			int valuesRead = 0;
			for (int i = 0; i < n && s.hasNextInt(); i++){
				for (int j = 0; j < n && s.hasNextInt(); j++){
					G[i][j] = s.nextInt();
					valuesRead++;
				}
			}
			if (valuesRead < n*n){
				System.out.printf("Adjacency matrix for graph %d contains too few values.\n",graphNum);
				break;
			}
			long startTime = System.currentTimeMillis();
			
			int[][] G2 = new int[n][n];
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++)
					G2[i][j] = G[i][j];
			int[][] flow = MaxFlow(G2);
			long endTime = System.currentTimeMillis();
			totalTimeSeconds += (endTime-startTime)/1000.0;
			
			if (flow == null || !verifyFlow(G,flow)){
				System.out.printf("Graph %d: Flow is invalid.\n",graphNum);
			}else{
				int value = totalFlowValue(flow);
				System.out.printf("Graph %d: Max Flow Value is %d\n",graphNum,value);
			}
				
		}
		graphNum--;
		System.out.printf("Processed %d graph%s.\nAverage Time (seconds): %.2f\n",graphNum,(graphNum != 1)?"s":"",(graphNum>0)?totalTimeSeconds/graphNum:0);
	}
}
