
//tree node class
public class BayesNode 
{
	private Edges edges = null;
	private BayesNode parent = null;
	private Attribute attribute = null;
	private Feature feature = null;
	private int first_class_value = 0;
	private int second_class_value = 0;
	private String class_value = null;
	public Type type;
	
	//type enumeration
	public enum Type
	{
		ATTRIBUTE, CLASS
	}
	
	//constructor sets number of edges
	public BayesNode()
	{
		edges = new Edges();
	}
	
	//get first class value
	public int GetFirstClassValue()
	{
		return first_class_value;
	}

	//get set first class value
	public void SetFirstClassValue(int first_class_value)
	{
		this.first_class_value = first_class_value;
	}

	//get second class value
	public int GetSecondClassValue()
	{
		return second_class_value;
	}

	//get set second class value
	public void SetSecondClassValue(int second_class_value)
	{
		this.second_class_value = second_class_value;
	}
	
	//get leaf node class value
	public String GetClassValue()
	{
		return class_value;
	}

	//set leaf node class value
	public void SetClassValue(String class_value)
	{
		this.class_value = class_value;
	}
	
	//get attribute
	public Attribute GetAttribute()
	{
		return attribute;
	}
	
	//set attribute
	public void SetAttribute(Attribute attribute)
	{
		this.attribute = attribute;
	}
	
	//get parent node
	public BayesNode GetParent()
	{
		return parent;
	}
	
	//set parent node
	public void SetParent(BayesNode parent)
	{
		this.parent = parent;
	}
	
	//get all edges
	public Edges GetEdges()
	{
		return edges;
	}
}
