
//cptentry class
public class CPTEntry 
{
	Feature this_feature = null;
	Feature parent_feature = null;
	String class_value = null;
	double probability = 0;
	
	public CPTEntry(Feature this_feature, Feature parent_feature, String class_value, double probability)
	{
		this.this_feature = this_feature;
		this.parent_feature = parent_feature;
		this.class_value = class_value;
		this.probability = probability;
	}
	
	public Feature GetThisFeature()
	{
		return this_feature;
	}
	
	public Feature GetParentFeature()
	{
		return parent_feature;
	}
	
	public String GetClassValue()
	{
		return class_value;
	}
	
	public double GetProbability()
	{
		return probability;
	}
}
