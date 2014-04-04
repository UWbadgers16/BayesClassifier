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
				first_cond_prob *= GetConditionalProbablity(train_examples, value_walker, null, null, first_class_value, 1);
				second_cond_prob *= GetConditionalProbablity(train_examples, value_walker, null, null, second_class_value, 1);

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
		Attribute attribute_walker = train_attributes.GetAttributesHead();
		BayesNode[] tan_nodes = new BayesNode[train_attributes.GetAttributesCount()];
		int index = 0;
		
		while(attribute_walker != null)
		{
			BayesNode tan_node = new BayesNode();
			tan_node.SetAttribute(attribute_walker);
			tan_nodes[index] = tan_node;
			index++;
			attribute_walker = attribute_walker.GetNext();
		}

		double mutual_info = 0;
		
		for(int i = 0; i < tan_nodes.length; i++)
		{
			for(int j = 0; j < tan_nodes.length; j++)
			{
				if(!tan_nodes[i].GetEdges().EdgeExists(tan_nodes[j]))
				{
					if(i == j)
						mutual_info = -1.0;
					else
						mutual_info = MutualInformation(train_examples, tan_nodes[i].GetAttribute(), tan_nodes[j].GetAttribute(), first_class_value, second_class_value);
					Edge temp = new Edge(mutual_info, tan_nodes[i], tan_nodes[j]);
					tan_nodes[i].GetEdges().AddEdge(temp);
				}
			}
		}
		
		System.out.println("DONE");
	}

	public static double MutualInformation(Examples train_examples, Attribute one, Attribute two, String first_class_value, String second_class_value)
	{
		double mutual_information = 0, joint_prob = 0, joint_cond = 0, cond_one = 0, cond_two = 0;
		int count = 0, joint_count = 0;
		Feature feature_one = one.GetFeaturesHead();

		while(feature_one != null)
		{
			Feature feature_two = two.GetFeaturesHead();

			while(feature_two != null)
			{
				if(!feature_two.equals(feature_one))
				{
					String class_value = first_class_value;
					
					for(int i = 0; i < 2; i++)
					{
						Example example_walker = train_examples.GetExamplesHead();
						
						while(example_walker != null)
						{
							Value value_walker = example_walker.GetValuesHead();
							Value first_value = null, second_value = null;
							boolean one_found = false, two_found = false;

							while(!one_found || !two_found)
							{
								if(value_walker.GetAttribute().AttributeName().equals(one.AttributeName()))
								{
									one_found = true;
									first_value = value_walker;
								}
								if(value_walker.GetAttribute().AttributeName().equals(two.AttributeName()))
								{
									two_found = true;
									second_value = value_walker;
								}
								value_walker = value_walker.GetNext();
							}

							if(first_value.GetValue().equals(feature_one.GetFeature()) && second_value.GetValue().equals(feature_two.GetFeature()) && example_walker.GetClassValue().equals(class_value))
								joint_count++;

							example_walker = example_walker.GetNext();
						}

						joint_prob = LaplaceEstimate(joint_count, train_examples.GetExamplesCount(), one.GetFeatureCount() * two.GetFeatureCount() * 2);
						joint_cond = GetConditionalProbablity(train_examples, null, feature_one, feature_two, class_value, 2);
						cond_one = GetConditionalProbablity(train_examples, null, feature_one, null, class_value, 3);
						cond_two = GetConditionalProbablity(train_examples, null, feature_two, null, class_value, 3);
						
						mutual_information += joint_prob * Log2(joint_cond/(cond_one * cond_two));
						
						joint_count = 0;
						class_value = second_class_value;
					}
				}
				
				feature_two = feature_two.GetNext();
			}
			
			feature_one = feature_one.GetNext();
		}
		
		return mutual_information;
	}

	public static double GetConditionalProbablity(Examples train_examples, Value value, Feature one, Feature two, String class_value, int number)
	{
		int instances = 0, total = 0;
		double laplace_estimate = 0;
		
		Example example_walker = train_examples.GetExamplesHead();

		if(number == 1)
		{
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

			laplace_estimate = LaplaceEstimate(instances, total, value.GetAttribute().GetFeatureCount());
		}
		else if(number == 2)
		{
			while(example_walker != null)
			{
				if(example_walker.GetClassValue().equals(class_value))
				{
					Value value_walker = example_walker.GetValuesHead();
					Value first_value = null, second_value = null;
					boolean one_found = false, two_found = false;

					while(!one_found || !two_found)
					{
						if(value_walker.GetAttribute().AttributeName().equals(one.GetAttribute().AttributeName()))
						{
							one_found = true;
							first_value = value_walker;
						}
						if(value_walker.GetAttribute().AttributeName().equals(two.GetAttribute().AttributeName()))
						{
							two_found = true;
							second_value = value_walker;
						}
						value_walker = value_walker.GetNext();
					}

					if(first_value.GetValue().equals(one.GetFeature()) && second_value.GetValue().equals(two.GetFeature()))
						instances++;
					
					total++;
				}
				example_walker = example_walker.GetNext();
			}
			
			laplace_estimate = LaplaceEstimate(instances, total, one.GetAttribute().GetFeatureCount() * two.GetAttribute().GetFeatureCount());
		}
		else if(number == 3)
		{
			while(example_walker != null)
			{
				if(example_walker.GetClassValue().equals(class_value))
				{
					Value value_walker = example_walker.GetValuesHead();

					while(!value_walker.GetAttribute().AttributeName().equals(one.GetAttribute().AttributeName()))
					{
						value_walker = value_walker.GetNext();
					}

					if(value_walker.GetValue().equals(one.GetFeature()))
						instances++;

					total++;
				}

				example_walker = example_walker.GetNext();
			}

			laplace_estimate = LaplaceEstimate(instances, total, one.GetAttribute().GetFeatureCount());
		}
		
		return laplace_estimate;
	}

	public static double LaplaceEstimate(int numerator, int denominator, int pseudocount)
	{
		return (double)(numerator + 1)/(double)(denominator + pseudocount);
	}
	
	//computes log2 of a value
	private static double Log2(double input)
	{
		if(input == 0)
			return 0;
		else
			return (Math.log10(input)/Math.log10(2));
	}
}
