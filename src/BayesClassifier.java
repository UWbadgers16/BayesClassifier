import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


//bayes classifier class
public class BayesClassifier 
{
	static CPT[] cpts = null;
	static int cpt_index = 0;
	static Attributes train_attributes = null;
	static String first_class_value = null, second_class_value = null;
	static double first_class_prob = 0, second_class_prob = 0;
	
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
					train_attributes = train_parser.GetAttributes();

					//get training and test examples
					Examples train_examples = train_parser.GetExamples();
					Examples test_examples = test_parser.GetExamples();

					//get class values
					first_class_value = train_parser.GetFirstClassValue();
					second_class_value = train_parser.GetSecondClassValue();
					first_class_prob = LaplaceEstimate(train_examples.GetFirstClassCount(), train_examples.GetExamplesCount(), 2);
					second_class_prob = LaplaceEstimate(train_examples.GetSecondClassCount(), train_examples.GetExamplesCount(), 2);
					String verbosity = null;
					
					if(args.length == 5)
						verbosity = args[4];
					else
						verbosity = null;
					
					if(args.length >= 4)
					{
						if(args[3].toLowerCase().equals("learning_curve"))
						{
							LearningCurve(train_examples, test_examples, type, verbosity);
						}
					}
					else
					{
						if(type.equals("n"))
							NaiveBayes(train_examples, test_examples);
						else if(type.equals("t"))
							TanBayes(train_examples, test_examples, verbosity);
						else
							System.out.println("Enter 'n' for naive Bayes or 't' for TAN Bayes");
					}
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
			System.out.println("Usage: bayes <train-set-file> <test-set-file> <n|t> <v>");
		}
	}
	
	private static int NaiveBayes(Examples train_examples, Examples test_examples)
	{
		Attribute attributes_walker = train_attributes.GetAttributesHead();
		while(attributes_walker != null)
		{
			System.out.println(attributes_walker.AttributeName() + " class");

			attributes_walker = attributes_walker.GetNext();
		}
		System.out.println();

		int correct = 0;

		Example examples_walker = test_examples.GetExamplesHead();
		Value value_walker = null;
		double first_cond_prob = first_class_prob, second_cond_prob = second_class_prob;

		while(examples_walker != null)
		{
			value_walker = examples_walker.GetValuesHead();
			while(value_walker != null)
			{
				first_cond_prob *= GetConditionalProbablity(train_examples, value_walker, null, null, null, first_class_value, 1);
				second_cond_prob *= GetConditionalProbablity(train_examples, value_walker, null, null, null, second_class_value, 1);

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
		
		return correct;
	}

	private static int TanBayes(Examples train_examples, Examples test_examples, String verbose)
	{
		cpts = new CPT[train_attributes.GetAttributesCount()];
		Attribute attribute_walker = train_attributes.GetAttributesHead();
		BayesNode[] tan_nodes = new BayesNode[train_attributes.GetAttributesCount()];
		int index = 0;
		
		while(attribute_walker != null)
		{
			BayesNode tan_node = new BayesNode();
			tan_node.type = BayesNode.Type.ATTRIBUTE;
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
						mutual_info = MutualInformation(train_examples, tan_nodes[i].GetAttribute(), tan_nodes[j].GetAttribute());
					Edge temp = new Edge(mutual_info, tan_nodes[i], tan_nodes[j]);
					tan_nodes[i].GetEdges().AddEdge(temp);
				}
			}
		}
		
		BayesNode root = PrimMST(tan_nodes);
		if(verbose != null && verbose.toLowerCase().equals("v"))
			PrintTANTree(root, 0);
		BayesNode class_node = new BayesNode();
		class_node.type = BayesNode.Type.CLASS;
		GetCPTs(train_examples, root, null);
		PrintStructure(root);
		
		int correct = 0;

		Example examples_walker = test_examples.GetExamplesHead();
		Value value_walker = null;
		double first_cond_prob = first_class_prob, second_cond_prob = second_class_prob;

		while(examples_walker != null)
		{
			value_walker = examples_walker.GetValuesHead();
			
			while(value_walker != null)
			{
				CPTEntry[] cpt_entries = GetCPTEntry(value_walker, examples_walker);
				first_cond_prob *= cpt_entries[0].GetProbability();
				second_cond_prob *= cpt_entries[1].GetProbability();
				
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
		return correct;
	}
	
	private static CPTEntry[] GetCPTEntry(Value value, Example example)
	{
		CPTEntry[] cpt_entry = new CPTEntry[2];
		boolean found = false;
		
		for(int i = 0; i < cpts.length && !found; i++)
		{
			if(cpts[i].GetAttribute().AttributeName().equals(value.GetAttribute().AttributeName()))
			{
				found = true;
				Value value_walker = example.GetValuesHead();
				
				if(cpts[i].type == CPT.Type.NO_PARENT)
				{
					for(int j = 0; j < cpts[i].GetEntries().length; j++)
					{
						if(cpts[i].GetEntry(j).GetThisFeature().GetFeature().equals(value.GetValue()) && cpts[i].GetEntry(j).GetClassValue().equals(first_class_value))
						{
							cpt_entry[0] = cpts[i].GetEntry(j);
						}
						else if(cpts[i].GetEntry(j).GetThisFeature().GetFeature().equals(value.GetValue()) && cpts[i].GetEntry(j).GetClassValue().equals(second_class_value))
						{
							cpt_entry[1] = cpts[i].GetEntry(j);
						}
					}
				}
				else
				{
					while(!value_walker.GetAttribute().AttributeName().equals(cpts[i].GetParentAttribute().AttributeName()))
					{
						value_walker = value_walker.GetNext();
					}
					
					for(int j = 0; j < cpts[i].GetEntries().length; j++)
					{
						if(cpts[i].GetEntry(j).GetThisFeature().GetFeature().equals(value.GetValue()) && cpts[i].GetEntry(j).GetParentFeature().GetFeature().equals(value_walker.GetValue()) && cpts[i].GetEntry(j).GetClassValue().equals(first_class_value))
						{
							cpt_entry[0] = cpts[i].GetEntry(j);
						}
						else if(cpts[i].GetEntry(j).GetThisFeature().GetFeature().equals(value.GetValue()) && cpts[i].GetEntry(j).GetParentFeature().GetFeature().equals(value_walker.GetValue()) && cpts[i].GetEntry(j).GetClassValue().equals(second_class_value))
						{
							cpt_entry[1] = cpts[i].GetEntry(j);
						}
					}
				}
			}
		}
		
		return cpt_entry;
	}
	
	private static void PrintStructure(BayesNode root)
	{
		Attribute attributes_walker = train_attributes.GetAttributesHead();
		
		while(attributes_walker != null)
		{
			BayesNode parent = FindParent(attributes_walker, root, null);
			if(parent == null)
				System.out.println(attributes_walker.AttributeName() + " class");
			else
				System.out.println(attributes_walker.AttributeName() + " " + parent.GetAttribute().AttributeName() + " class");
			attributes_walker = attributes_walker.GetNext();
		}
		
		System.out.println();
	}
	
	private static BayesNode FindParent(Attribute attribute, BayesNode root, BayesNode parent)
	{
		BayesNode parent_node = null;
		Edge edge_walker = root.GetEdges().GetEdgesHead();
		
		if(root.GetAttribute().equals(attribute))
			return parent;
		else
		{
			while(edge_walker != null)
			{
				parent_node = FindParent(attribute, edge_walker.GetChild(), root);
				
				if(parent_node != null)
					return parent_node;
				else
					edge_walker = edge_walker.GetNext();
			}
		}
		
		return parent_node;
	}
	
	private static void GetCPTs(Examples train_examples, BayesNode tan_node, BayesNode parent)
	{
		CPT cpt = null;
		int index = 0;
		Feature tan_node_walker = tan_node.GetAttribute().GetFeaturesHead();
		
		if(parent == null)
		{
			cpt = new CPT(tan_node.GetAttribute(), null, tan_node.GetAttribute().GetFeatureCount() * 2);
			
			while(tan_node_walker != null)
			{
				String class_value = first_class_value;
				for(int i = 0; i < 2; i++)
				{
					CPTEntry cpt_entry = new CPTEntry(tan_node_walker, null, class_value, GetConditionalProbablity(train_examples, null, null, tan_node_walker, null, class_value, 3));
					cpt.type = CPT.Type.NO_PARENT;
					cpt.AddEntry(cpt_entry, index);
					index++;
					class_value = second_class_value;
				}
				
				tan_node_walker = tan_node_walker.GetNext();
			}
		}
		else
		{
			cpt = new CPT(tan_node.GetAttribute(), parent.GetAttribute(), tan_node.GetAttribute().GetFeatureCount() * parent.GetAttribute().GetFeatureCount() * 2);
			
			while(tan_node_walker != null)
			{
				Feature parent_walker = parent.GetAttribute().GetFeaturesHead();
				
				while(parent_walker != null)
				{
					String class_value = first_class_value;
					for(int i = 0; i < 2; i++)
					{
						CPTEntry cpt_entry = new CPTEntry(tan_node_walker, parent_walker, class_value, GetConditionalProbablity(train_examples, null, null, tan_node_walker, parent_walker, class_value, 4));
						cpt.type = CPT.Type.PARENT;
						cpt.AddEntry(cpt_entry, index);
						index++;
						class_value = second_class_value;
					}
					
					parent_walker = parent_walker.GetNext();
				}
				
				tan_node_walker = tan_node_walker.GetNext();
			}
		}
		
		cpts[cpt_index] = cpt;
		cpt_index++;
		
		Edge edge_walker = tan_node.GetEdges().GetEdgesHead();
		
		while(edge_walker != null)
		{
			GetCPTs(train_examples, edge_walker.GetChild(), tan_node);
			edge_walker = edge_walker.GetNext();
		}
	}
	
	private static void PrintTANTree(BayesNode root, int level)
	{
		for(int j = 0; j < level; j++)
		{
			System.out.print("|\t");
		}
		System.out.println(GetID(root.GetAttribute().AttributeName()));
		
		Edge edge_walker = root.GetEdges().GetEdgesHead();
		
		while(edge_walker != null)
		{
			PrintTANTree(edge_walker.GetChild(), level + 1);
			edge_walker = edge_walker.GetNext();
		}
	}
	
	private static int GetID(String attribute)
	{
		int id = 0;
		switch(attribute)
		{
		case "lymphatics":
			id = 0;
			break;
		case "block_of_affere":
			id = 1;
			break;
		case "bl_of_lymph_c":
			id = 2;
			break;
		case "bl_of_lymph_s":
			id = 3;
			break;
		case "by_pass":
			id = 4;
			break;
		case "extravasates":
			id = 5;
			break;
		case "regeneration_of":
			id = 6;
			break;
		case "early_uptake_in":
			id = 7;
			break;
		case "lym_nodes_dimin":
			id = 8;
			break;
		case "lym_nodes_enlar":
			id = 9;
			break;
		case "changes_in_lym":
			id = 10;
			break;
		case "defect_in_node":
			id = 11;
			break;
		case "changes_in_node":
			id = 12;
			break;
		case "changes_in_stru":
			id = 13;
			break;
		case "special_forms":
			id = 14;
			break;
		case "dislocation_of":
			id = 15;
			break;
		case "exclusion_of_no":
			id = 16;
			break;
		case "no_of_nodes_in":
			id = 17;
			break;			
		}
		
		return id;
	}
	
	private static BayesNode PrimMST(BayesNode[] tan_nodes)
	{
		Edge max_edge = null;
		double max = -1.0;
		Edge[] edges = new Edge[train_attributes.GetAttributesCount() - 1];
		BayesNode[] vertices_list = new BayesNode[train_attributes.GetAttributesCount()];
		vertices_list[0] = tan_nodes[0];
		int vertices = 1;
		
		for(int i = 0; i < edges.length; i++)
		{
			for(int j = 0; j < vertices; j++)
			{
				Edge edge_walker = vertices_list[j].GetEdges().GetEdgesHead();
				
				while(edge_walker != null)
				{
					if(edge_walker.GetMutualInformation() > max && !ContainsVertex(vertices_list, edge_walker.GetChild()))
					{
						max = edge_walker.GetMutualInformation();
						max_edge = edge_walker;
					}
					
					edge_walker = edge_walker.GetNext();
				}
			}
			
			edges[i] = max_edge;
			vertices_list[vertices] = max_edge.GetChild();
			vertices++;
			max = -1.0;
		}
		
		return MakeTree(vertices_list[0], edges);
	}
	
	private static BayesNode MakeTree(BayesNode node, Edge[] edges)
	{
		BayesNode root = null;
		root = new BayesNode();
		root.type = BayesNode.Type.ATTRIBUTE;
		root.SetAttribute(node.GetAttribute());
		
		for(int i = 0; i < edges.length; i++)
		{
			if(edges[i].GetParent().equals(node))
			{
				Edge temp = new Edge(edges[i].GetMutualInformation(), node, MakeTree(edges[i].GetChild(), edges));
				root.GetEdges().AddEdge(temp);
			}
		}
		
		return root;
	}
	
	private static boolean ContainsVertex(BayesNode[] vertices, BayesNode vertex)
	{
		for(int i = 0; i < vertices.length; i++)
		{
			if(vertex.equals(vertices[i]))
				return true;
		}
		
		return false;
	}
	
	private static double MutualInformation(Examples train_examples, Attribute one, Attribute two)
	{
		double mutual_information = 0, joint_prob = 0, joint_cond = 0, cond_one = 0, cond_two = 0;
		int joint_count = 0;
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
						joint_cond = GetConditionalProbablity(train_examples, null, null, feature_one, feature_two, class_value, 2);
						cond_one = GetConditionalProbablity(train_examples, null, null, feature_one, null, class_value, 3);
						cond_two = GetConditionalProbablity(train_examples, null, null, feature_two, null, class_value, 3);
						
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

	public static double GetConditionalProbablity(Examples train_examples, Value value_one, Value value_two, Feature one, Feature two, String class_value, int number)
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

					while(!value_walker.GetAttribute().AttributeName().equals(value_one.GetAttribute().AttributeName()))
					{
						value_walker = value_walker.GetNext();
					}

					if(value_walker.GetValue().equals(value_one.GetValue()))
						instances++;

					total++;
				}

				example_walker = example_walker.GetNext();
			}

			laplace_estimate = LaplaceEstimate(instances, total, value_one.GetAttribute().GetFeatureCount());
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
		else if(number == 4)
		{
			while(example_walker != null)
			{
				if(example_walker.GetClassValue().equals(class_value))
				{
					Value value_walker = example_walker.GetValuesHead();

					while(!value_walker.GetAttribute().AttributeName().equals(two.GetAttribute().AttributeName()))
					{
						value_walker = value_walker.GetNext();
					}
					
					if(value_walker.GetValue().equals(two.GetFeature()))
					{
						value_walker = example_walker.GetValuesHead();

						while(!value_walker.GetAttribute().AttributeName().equals(one.GetAttribute().AttributeName()))
						{
							value_walker = value_walker.GetNext();
						}

						if(value_walker.GetValue().equals(one.GetFeature()))
							instances++;

						total++;
					}
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
	
	//print off learning curve information
	private static void LearningCurve(Examples train_examples, Examples test_examples, String type, String verbose)
	{
		double average = 0, accuracy = 0, min = 1, max = 0;
		double sum = 0, count = 0;
		int eval_val = 0;
		DecimalFormat formatter = new DecimalFormat("0.000000");

		//training size trees of 25
		for(int i = 0; i < 4; i++)
		{
			//sample the training set
			Examples temp = StratifiedSampling(train_examples, 25);

			//get an evaluation
			if(type.toLowerCase().equals("n"))
				eval_val = NaiveBayes(temp, test_examples);
			else if(type.toLowerCase().equals("t"))
				eval_val = TanBayes(temp, test_examples, verbose);

			//get accuracy and min/max of tree evaluation
			accuracy = (double)eval_val/(double)test_examples.GetExamplesCount();
			sum+=accuracy;
			count++;
			min = Min(min, accuracy);
			max = Max(max, accuracy);
		}
		//find average of all 10 tree accuracies
		average = (double)sum/(double)count;

		//print information
		System.out.println("*************************TRAINING SIZE: 25*************************");
		System.out.println("AVG: " + formatter.format(average));
		System.out.println("MIN: " + formatter.format(min));
		System.out.println("MAX: " + formatter.format(max));
		accuracy = 0;
		average = 0;
		min = 1;
		max = 0;
		sum = 0;
		count = 0;

		//training size trees of 50
		for(int i = 0; i < 4; i++)
		{
			//sample the training set
			Examples temp = StratifiedSampling(train_examples, 50);

			//get an evaluation
			if(type.toLowerCase().equals("n"))
				eval_val = NaiveBayes(temp, test_examples);
			else if(type.toLowerCase().equals("t"))
				eval_val = TanBayes(temp, test_examples, verbose);

			//get accuracy and min/max of tree evaluation
			accuracy = (double)eval_val/(double)test_examples.GetExamplesCount();
			sum+=accuracy;
			count++;
			min = Min(min, accuracy);
			max = Max(max, accuracy);
		}
		//find average of all 10 tree accuracies
		average = (double)sum/(double)count;

		//print information
		System.out.println("*************************TRAINING SIZE: 50*************************");
		System.out.println("AVG: " + formatter.format(average));
		System.out.println("MIN: " + formatter.format(min));
		System.out.println("MAX: " + formatter.format(max));
		accuracy = 0;
		average = 0;
		min = 1;
		max = 0;
		sum = 0;
		count = 0;

		//training size trees of 50
		for(int i = 0; i < 4; i++)
		{
			//sample the training set
			Examples temp = StratifiedSampling(train_examples, 100);

			//get an evaluation
			if(type.toLowerCase().equals("n"))
				eval_val = NaiveBayes(temp, test_examples);
			else if(type.toLowerCase().equals("t"))
				eval_val = TanBayes(temp, test_examples, verbose);

			//get accuracy and min/max of tree evaluation
			accuracy = (double)eval_val/(double)test_examples.GetExamplesCount();
			sum+=accuracy;
			count++;
			min = Min(min, accuracy);
			max = Max(max, accuracy);
		}
		//find average of all 10 tree accuracies
		average = (double)sum/(double)count;

		//print information
		System.out.println("*************************TRAINING SIZE: 100*************************");
		System.out.println("AVG: " + formatter.format(average));
		System.out.println("MIN: " + formatter.format(min));
		System.out.println("MAX: " + formatter.format(max));
	}

	//perform stratified sampling
	private static Examples StratifiedSampling(Examples examples, int number)
	{
		Examples examples_subset = new Examples(first_class_value, second_class_value);
		Random rand = new Random();

		int extra = rand.nextInt(100);
		double first_class_number = ((double)examples.GetFirstClassCount()/(double)examples.GetExamplesCount()) * (double)number;
		double second_class_number = ((double)examples.GetSecondClassCount()/(double)examples.GetExamplesCount()) * (double)number;
		int first_class_pick = 0;
		int second_class_pick = 0;

		//if number of first class examples to be used is closer to its ceiling
		if(first_class_number % Math.floor(first_class_number) > second_class_number % Math.floor(second_class_number))
		{
			//set number to choose from first and second class
			first_class_pick = (int)Math.ceil(first_class_number);
			second_class_pick = (int)Math.floor(second_class_number);
		}
		//if number of second class examples to be used is closer to its ceiling
		else if(second_class_number % Math.floor(second_class_number) > first_class_number % Math.floor(first_class_number))
		{
			//set number to choose from first and second class
			second_class_pick = (int)Math.ceil(second_class_number);
			first_class_pick = (int)Math.floor(first_class_number);
		}
		//both are same distance to their ceiling, randomly choose which gets its ceiling
		else
		{
			//first class gets ceiling
			if(extra < 50)
			{
				//set number to choose from first and second class
				first_class_pick = (int)Math.ceil(first_class_number);
				second_class_pick = (int)Math.floor(second_class_number);
			}
			//second class gets ceiling
			else
			{
				//set number to choose from first and second class
				second_class_pick = (int)Math.ceil(second_class_number);
				first_class_pick = (int)Math.floor(first_class_number);
			}
		}

		//create array list of first and second class examples
		ArrayList<Example> first_class_examples = new ArrayList<Example>();
		ArrayList<Example> second_class_examples = new ArrayList<Example>();
		Example example_walker = examples.GetExamplesHead();
		while(example_walker != null)
		{
			if(example_walker.GetClassValue().equals(first_class_value))
				first_class_examples.add(example_walker);
			else if(example_walker.GetClassValue().equals(second_class_value))
				second_class_examples.add(example_walker);

			example_walker = example_walker.GetNext();
		}

		//randomly shuffle first and second class examples
		Collections.shuffle(first_class_examples);
		Collections.shuffle(second_class_examples);

		//choose first class values
		for(int i = 0; i < first_class_pick; i++)
		{
			Example ex = new Example();
			ex.CopyExample(first_class_examples.get(i));
			examples_subset.AddExample(ex);
		}
		//choose second class values
		for(int j = 0; j < second_class_pick; j++)
		{
			Example ex = new Example();
			ex.CopyExample(second_class_examples.get(j));
			examples_subset.AddExample(ex);
		}

		//return statified sample
		return examples_subset;
	}

	//calculate minimum of numbers
	private static double Min(double current_min, double candidate)
	{
		if(candidate < current_min)
			return candidate;
		else
			return current_min;
	}

	//calculate maximum of numbers
	private static double Max(double current_max, double candidate)
	{
		if(candidate > current_max)
			return candidate;
		else
			return current_max;
	}
}
