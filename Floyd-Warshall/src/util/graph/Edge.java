package util.graph;


public class Edge extends GraphComponent
{
	protected String	label	= null; // FIXME: support null labels throughout the source
	protected Node		from	= null;
	protected Node		to		= null;
	
	/**
	 * Constructs a new edge. WARNING: this also changes the from and to nodes, by adding the newly constructed edge to their respective out and in lists.
	 * 
	 * @param fromNode
	 *            : the from node; this edge is added to the node's outEdges list
	 * @param toNode
	 *            : the to node; this edge is added to the node's inEdges list
	 * @param edgeLabel
	 *            : the label of the edge
	 */
	public Edge(Node fromNode, Node toNode, String edgeLabel)
	{
		this.from = fromNode;
		this.to = toNode;
		this.label = edgeLabel;
		if(this.to != null)
			this.to.inEdges.add(this);
		if(this.from != null)
			this.from.outEdges.add(this);
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public Node getFrom()
	{
		return from;
	}
	
	public Node getTo()
	{
		return to;
	}
	
	public Edge setFrom(Node fromNode)
	{
		if(from != null)
			from.outEdges.remove(this);
		from = fromNode;
		if(from != null)
			from.outEdges.add(this);
		return this;
	}
	
	public Edge setTo(Node toNode)
	{
		if(to != null)
			to.inEdges.remove(this);
		to = toNode;
		if(to != null)
			to.inEdges.add(this);
		return this;
	}
	
	public Edge setLabel(String edgeLabel)
	{
		label = edgeLabel;
		return this;
	}
	
	@Override
	public String toString()
	{
		return from + toStringShort() + to;
	}
	
	public String toStringShort()
	{
		return toStringShort(false);
	}
	
	public String toStringShort(boolean isBackward)
	{
		return (isBackward ? "<" : "") + (this.label != null ? ("-" + this.label + "-") : "-") + (isBackward ? "" : ">");
	}
	
}