package util.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import util.graph.representation.GraphRepresentation;
import util.graph.representation.RepresentationElement;

public abstract class GraphComponent
{
	protected Set<RepresentationElement> representations = new HashSet<RepresentationElement>();
	
	public void addRepresentation(RepresentationElement repr)
	{
		representations.add(repr);
	}
	
	public Collection<RepresentationElement> getRepresentations()
	{
		return representations;
	}
	
	public RepresentationElement getFirstRepresentationForPlatform(GraphRepresentation representation)
	{
		Collection<RepresentationElement> filtered = getRepresentationsForPlatform(representation);
		if(filtered.isEmpty())
			return null;
		return filtered.iterator().next();
	}
	
	protected Collection<RepresentationElement> getRepresentationsForPlatform(GraphRepresentation representation)
	{
		Collection<RepresentationElement> ret = new HashSet<RepresentationElement>();
		for(RepresentationElement repr : representations)
			if(repr.getRootRepresentation() == representation)
				ret.add(repr);
		return ret;
	}
}
