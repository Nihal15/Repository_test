package util.graph.representation;

import util.graph.Graph;
import util.graph.GraphComponent;
import util.logging.Unit;

/**
 * Abstract class for all classes that produce a representation (graphical, textual, etc) for a {@link Graph} instance.
 * 
 * <p>
 * It is possible that a {@link GraphRepresentation} class uses other, "sub-" {@link GraphRepresentation} instances for the representation, forming a tree that is defined by its root representation (the one called from the exterior).
 * 
 * <p>
 * A {@link GraphRepresentation} is based on {@link RepresentationElement} instances, each instance associated with a specific {@link GraphComponent}. Representation elements should be updatable.
 * 
 * @author Andrei Olaru
 * 
 */
public abstract class GraphRepresentation extends Unit
{
	/**
	 * Configures the graph representation with the {@link Graph} to represent, the root of the representation hierarchy (if any), and an indication whether to process the graph immediately or not.
	 * 
	 * @author Andrei Olaru
	 * 
	 */
	public static class GraphConfig extends UnitConfigData
	{
		Graph				graph				= null;
		GraphRepresentation	rootRepresentation	= null;
		boolean				doProcess			= true;
		
		public GraphConfig(Graph thegraph)
		{
			super();
			if(thegraph == null)
				throw new IllegalArgumentException("the graph cannot be null");
			this.graph = thegraph;
		}
		
		@Override
		public GraphConfig setName(String _unitName)
		{
			if(DEFAULT_UNIT_NAME.equals(_unitName))
			{
				if(this.graph.getName() == null)
					return (GraphConfig)super.setName(setDefaultName("Graph" + new Integer(graph.hashCode()).toString().substring(0, 5)));
				return (GraphConfig)super.setName(setDefaultName(this.graph.getName()));
			}
			return (GraphConfig)super.setName(_unitName);
			
		}
		
		@SuppressWarnings("static-method")	// this has to be overridable
		protected String setDefaultName(String name)
		{
			return name + "-R";
		}
	}
	
	Graph					theGraph			= null;
	GraphConfig				config				= null;
	RepresentationElement	theRepresentation	= null;
	
	public GraphRepresentation(GraphConfig conf)
	{
		super(conf);
		
		if(conf == null)
			throw new IllegalArgumentException("null graph configuration");
		this.config = conf;
		
		this.theGraph = this.config.graph;
		
		if(this.config.rootRepresentation == null)
			this.config.rootRepresentation = this;
		
		if(this.config.doProcess)
			processGraph();
	}
	
	public void update()
	{
		processGraph();
	}
	
	abstract void processGraph();
	
	public abstract RepresentationElement getRepresentation();
	
	public abstract Object displayRepresentation();
}
