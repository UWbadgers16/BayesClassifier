
//feature class
public class Feature 
{
	private String feature = null;
	private Feature next = null;
	private Feature prev = null;
	private Attribute attribute = null;
	private int midpoints_count = 0;
	private int real_features_count = 0;
	private int unique_reals_count = 0;
	
	//constructor sets feature name and attribute
	public Feature(String feature, Attribute attribute)
	{
		this.feature = feature;
		this.attribute = attribute;
	}
	
	//get feature
	public String GetFeature()
	{
		return feature;
	}
	
	//get next
	public Feature GetNext()
	{
		return next;
	}
	 
	//set next
	public void SetNext(Feature feature)
	{
		next = feature;
	}
	
	//get previous
	public Feature GetPrev()
	{
		return prev;
	}
	
	//set previous
	public void SetPrev(Feature feature)
	{
		prev = feature;
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
}
