
//examples class
public class Examples 
{
	private Example examples_head = null;
	private Example examples_tail = null;
	private String first_value = null;
	private String second_value = null;
	private int first_value_count = 0;
	private int second_value_count = 0;
	private int examples_count = 0;
	
	//constructor initializes first and second class value
	public Examples(String first_value, String second_value)
	{
		this.first_value = first_value;
		this.second_value = second_value;
	}
	
	//add example
	public void AddExample(Example example)
	{
		if(examples_head == null)
		{
			examples_head = example;
			examples_tail = example;
		}
		else
		{
			examples_tail.SetNext(example);
			example.SetPrev(examples_tail);
			examples_tail = example;
		}
		
		if(example.GetClassValue().equals(first_value))
			first_value_count++;
		else if(example.GetClassValue().equals(second_value))
			second_value_count++;
		
		examples_count++;
	}
	
	//get examples count
	public int GetExamplesCount()
	{
		return examples_count;
	}
	
	//get first example
	public Example GetExamplesHead()
	{
		return examples_head;
	}
	
	//get last example
	public Example GetExamplesTail()
	{
		return examples_tail;
	}
	
	//get first class value count
	public int GetFirstClassCount()
	{
		return first_value_count;
	}
	
	//get second class value count
	public int GetSecondClassCount()
	{
		return second_value_count;
	}
}
