package entityLinking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.queryparser.classic.ParseException;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.CborFileTypeException;
import edu.unh.cs.treccar_v2.read_data.CborRuntimeException;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import main.SearchIndex;

public class EntityLinker 
{
	private String tokenize;
	private String nameFinder;
	private String locationFinder;
	private String orgFinder;
	private String file1; // path to allButBenchmark file
	private String file2; // path to leading paragraphs file
	private String cbor_file; // path to leading paragraphs cbor file
	private String index;
	private HTreeMap<String,Double> paraToScoreMap;
	private DB db;
	private AnnotateText annotate;
	
	public EntityLinker(String tokenize, String nameFinder, String locationFinder, String orgFinder, String file1, String file2, String cbor_file, String index) throws FileNotFoundException, IOException
	{
		this.tokenize = tokenize;
		this.nameFinder = nameFinder;
		this.locationFinder = locationFinder;
		this.orgFinder = orgFinder;
		this.file1 = file1;
		this.file2 = file2;
		this.cbor_file = cbor_file;
		this.index = index;
		db = DBMaker.fileDB("EntityLinker.db").fileMmapEnable().transactionEnable().make();
		paraToScoreMap = db.hashMap("paraToScoreMap", Serializer.STRING, Serializer.DOUBLE).counterEnable().create();
		annotate = new AnnotateText(this.tokenize,this.nameFinder,this.locationFinder,this.orgFinder);
	}
	
	public void link() throws IOException, ParseException
	{
		// Candidate Entity Generation Step
		CandidateEntityGeneration ceg = new CandidateEntityGeneration(file1);
		
		//Build the dictionary 
		ceg.buildDictionary();
		
		BufferedReader br = new BufferedReader(new FileReader(file2));
		String line, text,topEntity;
		Data.Paragraph paragraph;
		ArrayList<String> entityMentions = new ArrayList<String>();
		ArrayList<String> candidateEntitySet = new ArrayList<String>();
		List<String> trueEntities;
		List<String> topEntities = new ArrayList<String>();
		double f1Measure;
		new SearchIndex(index);
		while ((line = br.readLine()) != null) 
		{
			paragraph = getParagraph(line);
			if(paragraph == null )
			{
				System.out.println("Error! Could not find paragraph!");
				System.exit(0);
			}
			text = paragraph.getTextOnly();
			trueEntities = paragraph.getEntitiesOnly();
			entityMentions = annotate.getEntityMentions(text);
			for(String e : entityMentions)
			{
				candidateEntitySet = ceg.getCandidateEntities(e);
				topEntity = CandidateEntityRank.rank(e,candidateEntitySet);
				topEntities.add(topEntity);
			}	
			f1Measure = getF1Score( trueEntities, topEntities);
			paraToScoreMap.put(line, f1Measure);
		}
		br.close();
	}
	private Data.Paragraph getParagraph(String paraID) throws CborRuntimeException, CborFileTypeException, FileNotFoundException
	{
		Data.Paragraph para = null;
		for (Data.Paragraph p : DeserializeData.iterableParagraphs(new FileInputStream(new File(cbor_file))))
			if(p.getParaId().equalsIgnoreCase(paraID))
			{
				para = p;
				break;
			}
		return para;
	}
	private double getF1Score(List<String> relevant, List<String> retrieved)
	{
		int common = 0;
		double precision, recall, F;
		
		for(String s : relevant)
			if(retrieved.contains(s))
				common++;
		precision = common / retrieved.size();
		recall = common / relevant.size();
		F = (2 * precision * recall) / (precision + recall);
		
		return F;
	}

}
