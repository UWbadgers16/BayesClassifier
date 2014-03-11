
//list of attributes
public class Attributes 
{
	private Attribute attributes_head = null;
	private Attribute attributes_tail = null;
	private int attributes_count = 0;
	
	//add an attribute
	public void AddAttribute(Attribute attribute)
	{
		if(attributes_head == null)
		{
			attributes_head = attribute;
			attributes_tail = attribute;
		}
		else
		{
			attributes_tail.SetNext(attribute);
			attribute.SetPrev(attributes_tail);
			attributes_tail = attribute;
		}
		
		attributes_count++;
	}
	
	//get count of attributes
	public int GetAttributesCount()
	{
		return attributes_count;
	}
	
	//get first attribute
	public Attribute GetAttributesHead()
	{
		return attributes_head;
	}

	//get last attribute
	public Attribute GetAttributesTail()
	{
		return attributes_tail;
	}
	
	//remove the last attribute for class values
	public void RemoveTail()
	{
		attributes_tail.GetPrev().SetNext(null);
		attributes_tail = attributes_tail.GetPrev();
		attributes_tail.SetPrev(null);
		attributes_count--;
	}
}
