
//example class
public class Example 
{
	private Value values_head = null;
	private Value values_tail = null;
	private Example next = null;
	private Example prev = null;
	private String class_value = null;
	private int values_count = 0;
	private Value held_value;
	
	//add a value
	public void AddValues(String value, String attribute)
	{
		Value temp = new Value(value, attribute);
		if(values_head == null)
		{
			values_head = temp;
			values_tail = temp;
		}
		else
		{
			values_tail.SetNext(temp);
			temp.SetPrev(values_tail);
			values_tail = temp;
		}
		
		values_count++;
	}
	
	//get count of values
	public int GetValuesCount()
	{
		return values_count;
	}
	
	//get first value
	public Value GetValuesHead()
	{
		return values_head;
	}
	
	//get last value
	public Value GetValuesTail()
	{
		return values_tail;
	}
	
	//get next
	public Example GetNext()
	{
		return next;
	}
	 //set next
	public void SetNext(Example example)
	{
		next = example;
	}
	
	//get previous
	public Example GetPrev()
	{
		return prev;
	}
	
	//set previous
	public void SetPrev(Example example)
	{
		prev = example;
	}
	
	//get class value
	public String GetClassValue()
	{
		return class_value;
	}

	//set class value
	public void SetClassValue(String class_value)
	{
		this.class_value = class_value;
	}
	
	//copy example
	public void CopyExample(Example example)
	{
		values_head = example.values_head;
		values_tail = example.values_tail;
		class_value = example.class_value;
		values_count = example.values_count;
	}
}
