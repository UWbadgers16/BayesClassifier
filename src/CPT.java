
//cpt class
public class CPT 
{
	Attribute attribute = null;
	Attribute parent_attribute = null;
	CPTEntry[] cpt_entries = null;
	int size = 0;
	public Type type;
	
	//type enumeration
	public enum Type
	{
		NO_PARENT, PARENT
	}
	
	public CPT(Attribute attribute,Attribute parent_attribute, int entries)
	{
		this.attribute = attribute;
		this.parent_attribute = parent_attribute;
		cpt_entries = new CPTEntry[entries];
		size = entries;
	}
	
	public CPTEntry[] GetEntries()
	{
		return cpt_entries;
	}
	
	public CPTEntry GetEntry(int index)
	{
		return cpt_entries[index];
	}
	
	public Attribute GetAttribute()
	{
		return attribute;
	}
	
	public Attribute GetParentAttribute()
	{
		return parent_attribute;
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
