import java.io.File;


//bayes classifier class
public class BayesClassifier {

	public static void main(String[] args) 
	{
		try
		{
			//initialize two files to check that training and test set files valid
			File one = new File(args[0]);
			File two = new File(args[1]);
			
			//check that training set valid
			if(one.exists() && !one.isDirectory())
			{
				//check that test set valid
				if(two.exists() && !two.isDirectory())
				{
					//get n/t value and convert to lowercase
					String type = args[2];
					type = type.toLowerCase();
					
					//initialize two ARFF parsers and parse training and test sets
					ARFF train_parser = new ARFF(args[0], ARFF.Type.TRAINING);
					ARFF test_parser = new ARFF(args[1], ARFF.Type.TESTING);
					train_parser.ParseFile();
					test_parser.ParseFile();
					
					//get list of attributes
					Attributes train_attributes = train_parser.GetAttributes();
					
					//get training and test examples
					Examples train_examples = train_parser.GetExamples();
					Examples test_examples = test_parser.GetExamples();
					
					//get class values
					String first_class_value = train_parser.GetFirstClassValue();
					String second_class_value = train_parser.GetSecondClassValue();
					
					if(type.equals("n"))
						NaiveBayes(train_attributes, train_examples, test_examples, first_class_value, second_class_value);
					else if(type.equals("t"))
						TanBayes(train_attributes, train_examples, test_examples, first_class_value, second_class_value);
					else
						System.out.println("Enter 'n' for naive Bayes or 't' for TAN Bayes");
				}
				//test set doesn't exist
				else
				{
					System.out.println("Testing set file doesn't exist");
				}
			}
			//training set doesn't exist
			else
			{
				System.out.println("Training set file doesn't exist");
			}
		}
		//input missing
		catch(ArrayIndexOutOfBoundsException oob)
		{
			System.out.println("Usage: dt-learn <train-set-file> <test-set-file> m");
		}
	}

	public static void NaiveBayes(Attributes train_attributes, Examples train_examples, Examples test_examples, String first_class_value, String second_class_value)
	{
		Attribute attributes_walker = train_attributes.GetAttributesHead();
		while(attributes_walker != null)
		{
			System.out.println(attributes_walker.AttributeName() + " class");
			
			attributes_walker = attributes_walker.GetNext();
		}
		System.out.println();
		
		double first_class_prob = LaplaceEstimate(train_examples.GetFirstClassCount(), train_examples.GetExamplesCount(), 2);
		double second_class_prob = LaplaceEstimate(train_examples.GetSecondClassCount(), train_examples.GetExamplesCount(), 2);
		int correct = 0;
		
		Example examples_walker = test_examples.GetExamplesHead();
		Value value_walker = null;
		double first_cond_prob = first_class_prob, second_cond_prob = second_class_prob;
		
		while(examples_walker != null)
		{
			value_walker = examples_walker.GetValuesHead();
			while(value_walker != null)
			{
				first_cond_prob *= GetConditionalProbablity(train_examples, value_walker, first_class_value);
				second_cond_prob *= GetConditionalProbablity(train_examples, value_walker, second_class_value);
				
				value_walker = value_walker.GetNext();
			}

			if(first_cond_prob > second_cond_prob)
			{
				if(examples_walker.GetClassValue().equals(first_class_value))
					correct++;
				System.out.println(first_class_value + " " + examples_walker.GetClassValue() +  " " + (double)first_cond_prob/(double)(first_cond_prob + second_cond_prob));
			}
			else
			{
				if(examples_walker.GetClassValue().equals(second_class_value))
					correct++;
				System.out.println(second_class_value +  " " + examples_walker.GetClassValue() +  " " + (double)second_cond_prob/(double)(first_cond_prob + second_cond_prob));
			}
			
			first_cond_prob = first_class_prob;
			second_cond_prob = second_class_prob;
			examples_walker = examples_walker.GetNext();
		}
		
		System.out.println();
		System.out.println(correct);
	}
	
	public static void TanBayes(Attributes train_attributes, Examples train_examples, Examples test_examples, String first_class_value, String second_class_value)
	{
		
	}
	
	public static double GetConditionalProbablity(Examples train_examples, Value value, String class_value)
	{
		int instances = 0, total = 0;
		
		Example example_walker = train_examples.GetExamplesHead();
		while(example_walker != null)
		{
			if(example_walker.GetClassValue().equals(class_value))
			{
				Value value_walker = example_walker.GetValuesHead();
				
				while(!value_walker.GetAttribute().AttributeName().equals(value.GetAttribute().AttributeName()))
				{
					value_walker = value_walker.GetNext();
				}
				
				if(value_walker.GetValue().equals(value.GetValue()))
					instances++;
				
				total++;
			}
			
			example_walker = example_walker.GetNext();
		}
		
		return LaplaceEstimate(instances, total, value.GetAttribute().GetFeatureCount());
	}
	
	public static double LaplaceEstimate(int numerator, int denominator, int pseudocount)
	{
		return (double)(numerator + 1)/(double)(denominator + pseudocount);
	}
}
