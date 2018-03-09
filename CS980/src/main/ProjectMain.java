package main;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.queryparser.classic.ParseException;

import carHypertextGraph.MakeCARGraphFile;
import carHypertextGraph.PageRank;
import entityLinking.EntityLinker;
import pageRank.PersonalisedPageRank;

public class ProjectMain 
{
	public static void main(String[] args)throws IOException
	{
		if(args[0].equalsIgnoreCase("-u"))
			use();
		else if(args[0].equalsIgnoreCase("-b"))
		{
			System.out.println("Building index");
			String indexDir = args[1];
			String cborDir = args[2];
			new BuildIndex(indexDir,cborDir);
			BuildIndex.createIndex();
		}
		else if(args[0].equalsIgnoreCase("-sp"))
		{
			System.out.println("Searching index for pages");
			String indexDir = args[1];
			String outDir = args[2];
			String cborOutline = args[3];
			String outFile = args[4];
			int top = Integer.parseInt(args[5]);
			new SearchIndex(indexDir,outDir,cborOutline,outFile,top);
			SearchIndex.searchPages();
		}
		else if(args[0].equalsIgnoreCase("-ss"))
		{
			System.out.println("Searching index for sections");
			String indexDir = args[1];
			String outDir = args[2];
			String cborOutline = args[3];
			String outFile= args[4];
			int top = Integer.parseInt(args[5]);
			new SearchIndex(indexDir,outDir,cborOutline,outFile,top);
			SearchIndex.searchSections();
		}
		else if(args[0].equalsIgnoreCase("-pr"))
		{
			System.out.println("PageRank Algorithm on CAR Graph of top paragraphs of each page as nodes");
			System.out.println("Make sure to build the index first using -b option and "
					+ "then search the index for page queries using -sp option. See usage for more details.");
			double alpha = Double.parseDouble(args[1]);
			new PageRank(alpha);
		}
		else if(args[0].equalsIgnoreCase("-ppr"))
		{
			System.out.println("Doing PersonalisedPageRank...");
			String file = args[1];
			double alpha = Double.parseDouble(args[2]);
			int n = Integer.parseInt(args[3]);
			int c=4;
			ArrayList<String> seed = new ArrayList<String>();
			for(int i = 1 ; i <= n ; i++)
				seed.add(args[c++]);
			PersonalisedPageRank p = new PersonalisedPageRank(file,alpha,seed);
			p.calculate();
		}
		else if(args[0].equalsIgnoreCase("-make"))
		{
			System.out.println("Making CAR Hypertext Graph File");
			String cborFile = args[1];
			String file = args[2];
			String paraRunFile = args[3];
			MakeCARGraphFile ob = new MakeCARGraphFile(cborFile,file,paraRunFile);
			ob.makeGraphFile();
		}
		else if(args[0].equalsIgnoreCase("-link"))
		{
			System.out.println("Entity Linking");
			String dir = args[1];
			String s1 = dir+"/"+args[2];
			String s2 = dir+"/"+args[3];
			String s3 = dir+"/"+args[4];
			String s4 = dir+"/"+args[5];
			String s5 = args[6];
			String s6 = args[7];
			String s7 = args[8];
			String s8 = args[9];
			EntityLinker ob = new EntityLinker(s1,s2,s3,s4,s5,s6,s7,s8);
			try 
			{
				ob.link();
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Wrong usage.To see usage, run with option -u.");
		}
		
	}
	private static void use()
	{
		System.out.println("************************************************************************************************************************************************************");
		System.out.println("             										USAGE OPTIONS                   																			");
		System.out.println("************************************************************************************************************************************************************");
		System.out.println("-u: Display usage");
		System.out.println("-b: Build Index");
		System.out.println("-sp: Search Index for Page queries");
		System.out.println("-ss: Search Index for Section queries");
		System.out.println("-pr: Run PageRank Algorithm on a Graph");
		System.out.println("-ppr: Run PersonalisedPageRank Algorithm on a Graph");
		System.out.println("-make: Make a CAR Hypertext Graph of paragraphs as nodes and entities as edges");
		System.out.println("-link: Entity Linking");
		System.out.println();
		System.out.println("************************************************************************************************************************************************************");
		System.out.println("             										USAGE SYNTAX                   																			");
		System.out.println("************************************************************************************************************************************************************");
		
		
		
		System.out.println("java -jar $jar file$ -b $path to index directory$ $path to directory containing paragrapgh cbor file$");
		
		System.out.println("java -jar $jar file$ -sp $path to index directory$ $path to output directory$"
				+" "+"$path to cbor outline file$ $name of paragragh run file$ $top how many results$");
		
		System.out.println("java -jar $jar file$ -ss $path to index directory$ $path to output directory$"
				+" "+"$path to cbor outline file$ $name of section run file$ $top how many results$");
		
		System.out.println("java -jar $jar file$ -pr  $value of random jump (alpha)$");
		
		System.out.println("java -jar $jar file$ -ppr $path to graph file$ $value of random jump (alpha)$ $size of seed set$ $seed values$");
		
		System.out.println("java -jar $jar file$ -make $path to paragraph corpus cbor file$ $path to graph file$");
		System.out.println("java -jar $jar file$ -link $path to openNLP directory$ $name of tokenizer file$"
				+ "$name of name finder file$ $name of location finder file$ $name of organization finder file$"
				+ "$path to all but benchmark file$ $path to leading paragraphs file$ $path to leading paragraphs cbor file$"
				+ "$path to index directory$");
	}
}