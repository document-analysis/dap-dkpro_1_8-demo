package org.dap.dap_dkpro_1_8.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.dap.dap_dkpro_1_8.Dkpro_1_8_Annotator;
import org.dap.data_structures.Document;
import org.dap.data_structures.LanguageFeature;

import de.tudarmstadt.ukp.dkpro.core.maltparser.MaltParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordCoreferenceResolver;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;

public class Test
{

	public static void main(String[] args) throws Exception
	{
		AnalysisEngine uimaAnnotator = AnalysisEngineFactory.createEngine(
				AnalysisEngineFactory.createEngineDescription(
						// Other DKPro annotators can be used. See https://dkpro.github.io/dkpro-core/releases/1.8.0/docs/component-reference.html
						// Note that some annotators can be removed, if they are not necessary. Remember, however, that some annotators depend on each other.
						AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class),
						AnalysisEngineFactory.createEngineDescription(OpenNlpPosTagger.class),
						AnalysisEngineFactory.createEngineDescription(OpenNlpChunker.class),
						AnalysisEngineFactory.createEngineDescription(OpenNlpNamedEntityRecognizer.class),
						AnalysisEngineFactory.createEngineDescription(StanfordNamedEntityRecognizer.class),
						AnalysisEngineFactory.createEngineDescription(MaltParser.class),
						AnalysisEngineFactory.createEngineDescription(OpenNlpParser.class) // This is constituency parser.
						
						
						// Note: Coreference annotator has a run-time issue, and is commented out here.
						// This is a known issue with DkPro dependencies related to this annotator (it's solvable, but not trivial).
						,
						AnalysisEngineFactory.createEngineDescription(StanfordCoreferenceResolver.class)
						)
				);
		
		
		try(Dkpro_1_8_Annotator annotator = new Dkpro_1_8_Annotator(uimaAnnotator))
		{
			File directory = new File(args[0]);
			
			for (File file : Arrays.stream(directory.listFiles()).sorted().collect(Collectors.toList()))
			{
				String text = readFile(file);
				System.out.println(file.getName());
				Document document = new Document("example_document", text);
				LanguageFeature.setDocumentLanguage(document, "en");
				annotator.annotate(document);
			}
		}

	}
	
	
	private static String readFile(File file) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ( (line=reader.readLine())!=null )
			{
				sb.append(line).append("\n");
			}
		}
		return sb.toString();
	}

}
