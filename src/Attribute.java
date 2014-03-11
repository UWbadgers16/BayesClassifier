
//individual attribute
public class Attribute 
{
	private Attribute next = null;
	private Attribute prev = null;
	private String attribute = null;
	private Feature features_head = null;
	private Feature features_tail = null;
	private int feature_count = 0;
	
	//constructor initializes attribute name
	public Attribute(String attribute)
	{
		this.attribute = attribute;
	}
	
	//add feature
	public void AddFeatures(String feature)
	{
		Feature temp = new Feature(feature, this);
		if(features_head == null)
		{
			features_head = temp;
			features_tail = temp;
		}
		else
		{
			features_tail.SetNext(temp);
			temp.SetPrev(features_tail);
			features_tail = temp;
		}
		
		feature_count++;
	}
	
	//get attribute name
	public String AttributeName()
	{
		return attribute;
	}
	
	//get number of features
	public int GetFeatureCount()
	{
		return feature_count;
	}
	
	//get next
	public Attribute GetNext()
	{
		return next;
	}
	
	//set next
	public void SetNext(Attribute attribute)
	{
		next = attribute;
	}
	
	//get previous
	public Attribute GetPrev()
	{
		return prev;
	}
	
	//set previous
	public void SetPrev(Attribute attribute)
	{
		prev = attribute;
	}
	
	//get first feature
	public Feature GetFeaturesHead()
	{
		return features_head;
	}
	
	//get last feature
	public Feature GetFeaturesTail()
	{
		return features_tail;
	}
	
	//copy attribute
	public void CopyAttribute(Attribute attribute)
	{
		features_head = attribute.features_head;
		features_tail = attribute.features_tail;
		feature_count = attribute.feature_count;
	}
}
