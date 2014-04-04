
//cpt class
public class CPT 
{
	Attribute attribute = null;
	CPTEntry[] cpt_entries = null;
	int size = 0;
	
	public CPT(Attribute attribute, int entries)
	{
		this.attribute = attribute;
		cpt_entries = new CPTEntry[entries];
		size = entries;
	}
	
	public Attribute GetAttribute()
	{
		return attribute;
	}
	
	public int GetSize()
	{
		return size;
	}
	
	public void AddEntry(CPTEntry entry, int index)
	{
		cpt_entries[index] = entry;
	}
}
