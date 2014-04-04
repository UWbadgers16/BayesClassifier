
//edge class
public class Edge 
{
	private Edge next = null;
	private Edge prev = null;
	private double mutual_information = 0;
	private BayesNode parent = null, child = null;
	
	public Edge(double mutual_information, BayesNode parent, BayesNode child)
	{
		this.mutual_information = mutual_information;
		this.parent = parent;
		this.child = child;
	}
	
	public void SetMutualInformation(double mutual_information)
	{
		this.mutual_information = mutual_information;
	}
	
	public double GetMutualInformation()
	{
		return mutual_information;
	}
	
	public void SetParent(BayesNode parent)
	{
		this.parent = parent;
	}
	
	public BayesNode GetParent()
	{
		return parent;
	}
	
	public void SetChild(BayesNode child)
	{
		this.child = child;
	}
	
	public BayesNode GetChild()
	{
		return child;
	}
	
	//get next
	public Edge GetNext()
	{
		return next;
	}
	
	//set next
	public void SetNext(Edge Edge)
	{
		next = Edge;
	}
	
	//get previous
	public Edge GetPrev()
	{
		return prev;
	}
	
	//set previous
	public void SetPrev(Edge edge)
	{
		prev = edge;
	}
}
