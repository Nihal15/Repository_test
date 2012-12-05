package util.graph;


public class FloydWarshall {
	
	private static Graph graph;
	private static int n;
	private static int[][] costsmatrix,pathmatrix;
	
	public FloydWarshall(){
		graph = null;
	}
	
	public static void initializeGraph(Graph g){
		graph = g;
		n = graph.getNodes().size();
		costsmatrix = new int[n][n];
		pathmatrix = new int[n][n];
		calculateCostsMatrix();
		writeCostsMatrix();
		FW();
		writeCostsMatrix();
		writePaths();
	}
	
	private static void calculateCostsMatrix()
	{
		for(Edge edge:graph.getEdges())
		{
			costsmatrix[Integer.parseInt(edge.getFrom().getLabel())-1][Integer.parseInt(edge.getTo().getLabel())-1] = Integer.parseInt(edge.getLabel());
			
		}
		
		for(int i=0;i<n;i++)
		 for(int j=0;j<n;j++)
			 {
			 	if(costsmatrix[i][j]==0 && i!=j) 
			 		costsmatrix[i][j]=1000;
			 	if(costsmatrix[i][j]!=1000 && i!=j) pathmatrix[i][j]=i;
			
			 }
				 
	}
	
	private static void writeCostsMatrix()
	{
		for(int i=0;i<n;i++)
			{ 
				for(int j=0;j<n;j++)
					System.out.print(costsmatrix[i][j]+" ");
			  		System.out.println();
			}
		System.out.println();
	}
	
	private static void FW()
	{
		for(int k=0;k<n;k++)
			for(int i=0;i<n;i++)
				for(int j=0;j<n;j++)
					if(i!=j && i!=k && k!=j && costsmatrix[i][j]>costsmatrix[i][k]+costsmatrix[k][j])
						{ 
							costsmatrix[i][j]=costsmatrix[i][k]+costsmatrix[k][j];
							pathmatrix[i][j]=pathmatrix[k][j];
						}	
	}
	
	private static void path(int i,int j)
	{
		if(i!=j) 
		{
			path(i,pathmatrix[i][j]);
			System.out.print((pathmatrix[i][j]+1)+" ");
		}
	}
	
	private static void writePaths()
	{
		for(int i=0;i<n;i++)
			for(int j=0;j<n && i!=j;j++)
			{
				if(pathmatrix[i][j]==0)
					System.out.println("There is no path between "+(i+1)+" and "+(j+1)+".");
				else 
					{
						System.out.print("The path from "+(i+1)+" to "+(j+1)+" is ");
						path(i,j);
						System.out.println(j+1);
					}
			}
	}
	
}
