
//edges class
public class Edges 
{
	private Edge edges_head = null;
	private Edge edges_tail = null;
	private int edges_count = 0;
	
	//add an attribute
	public void AddEdge(Edge edge)
	{
		if(edges_head == null)
		{
			edges_head = edge;
			edges_tail = edge;
		}
		else
		{
			edges_tail.SetNext(edge);
			edge.SetPrev(edges_tail);
			edges_tail = edge;
		}
		
		edges_count++;
	}
	
	public boolean EdgeExists(BayesNode child)
	{
		Edge edge_walker = edges_head;
		
		while(edge_walker != null)
		{
			if(edge_walker.GetChild().equals(child))
				return true;
			
			edge_walker = edge_walker.GetNext();
		}
		
		return false;
	}
	
	public void RemoveEdge(Edge edge)
	{
		Edge edge_walker = edges_head;
		
		while(edge_walker != edge && edge_walker != null)
		{
			edge_walker = edge_walker.GetNext();
		}
		
		if(edge_walker == edges_head && edges_head != edges_tail)
		{
			edges_head = edge_walker.GetNext();
			edge_walker.GetNext().SetPrev(null);
		}
		else if(edge_walker == edges_tail && edges_head != edges_tail)
		{
			edges_tail = edge_walker.GetPrev();
			edge_walker.GetPrev().SetNext(null);
		}
		else if(edge_walker == edges_head && edge_walker == edges_tail)
		{
			edges_head = null;
			edges_tail = null;
		}
		else
		{
			edge_walker.GetPrev().SetNext(edge_walker.GetNext());
			edge_walker.GetNext().SetPrev(edge_walker.GetPrev());
		}
	}
	
	//get count of attributes
	public int GetAttributesCount()
	{
		return edges_count;
	}
	
	//get first attribute
	public Edge GetAttributesHead()
	{
		return edges_head;
	}

	//get last attribute
	public Edge GetAttributesTail()
	{
		return edges_tail;
	}
}
