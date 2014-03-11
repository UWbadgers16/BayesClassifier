
//value class
public class Value 
{
	private Attribute attribute = null;
	private String value = null;
	private Value next = null;
	private Value prev = null;
	
	//constructor initializes value string and related attribute
	public Value(String value, Attribute attribute)
	{
		this.value = value;
		this.attribute = attribute;
	}
	
	//get value
	public String GetValue()
	{
		return value;
	}
	
	//get next
	public Value GetNext()
	{
		return next;
	}
	
	//set next
	public void SetNext(Value next)
	{
		this.next = next;
	}
	
	//get previous
	public Value GetPrev()
	{
		return prev;
	}
	
	//set previous
	public void SetPrev(Value prev)
	{
		this.prev = prev;
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
