package org.dap.dap_dkpro_1_8.demo;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dap.common.DapException;
import org.dap.dap_dkpro_1_8.Dkpro_1_8_Annotator;
import org.dap.dap_dkpro_1_8.annotations.Sentence;
import org.dap.dap_dkpro_1_8.annotations.coref.CoreferenceLink;
import org.dap.dap_dkpro_1_8.annotations.ne.NamedEntity;
import org.dap.dap_dkpro_1_8.annotations.pos.POS;
import org.dap.dap_dkpro_1_8.annotations.syntax.depencency.Dependency;
import org.dap.data_structures.Annotation;
import org.dap.data_structures.AnnotationReference;
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

/**
 * Demo class for {@link Dkpro_1_8_Annotator}.
 * <br>
 * <b>Some annotators, especially coreference-annotator, might lead to run-time problems. If so, comment-out the problematic annotators.</b>
 * 
 * <p>
 * Note: you can comment out the annotators, and also replace the implementations. See comments inside the code here.
 * 
 *  
 * 
 *
 * <p>
 * Date: 8 Oct 2017
 * @author Asher Stern
 *
 */
public class DemoDapDkpro_1_8
{
	public static final String EXAMPLE_TEXT = "Albert Einstein (14 March 1879 – 18 April 1955) was a German-born theoretical physicist.\n"
			+ "Einstein developed the theory of relativity, one of the two pillars of modern physics (alongside quantum mechanics).\n"
			+ "Einstein's work is also known for its influence on the philosophy of science.\n"
			+ "Einstein is best known by the general public for his mass–energy equivalence formula E = mc2 (which has been dubbed \"the world\'s most famous equation\").\n"
			+ "He received the 1921 Nobel Prize in Physics \"for his services to theoretical physics, and especially for his discovery of the law of the photoelectric effect\", a pivotal step in the evolution of quantum theory.";
	
	public static void main(String[] args) throws ResourceInitializationException
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
						AnalysisEngineFactory.createEngineDescription(OpenNlpParser.class),
						
						// Note: if Coreference annotator turns you into runtime problems, comment it out.
						// There is a known issue with DkPro dependencies related to this annotator (it's solvable, but not trivial).
						AnalysisEngineFactory.createEngineDescription(StanfordCoreferenceResolver.class)
						)
				);
		
		
		try(Dkpro_1_8_Annotator annotator = new Dkpro_1_8_Annotator(uimaAnnotator))
		{
			Document document = new Document("example_document", EXAMPLE_TEXT);
			LanguageFeature.setDocumentLanguage(document, "en");
			annotator.annotate(document);
			showAnnotations(document);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private static void showAnnotations(Document document)
	{
		System.out.println("Sentences:");
		for (Annotation<?> annotation : document.iterable(Sentence.class))
		{
			System.out.println(annotation.getCoveredText());
		}
		
		System.out.println();
		System.out.println("Part of speech tags:");
		for (Annotation<?> sentenceAnnotation : document.iterable(Sentence.class))
		{
			for (Annotation<?> posAnnotation : document.iterable(POS.class, sentenceAnnotation.getBegin(), sentenceAnnotation.getEnd()))
			{
				System.out.print(posAnnotation.getCoveredText()+"/"+posAnnotation.getAnnotationContents().getClass().getSimpleName()+" ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Named entities:");
		for (Annotation<?> annotation : document.iterable(NamedEntity.class))
		{
			String neType = ((NamedEntity)annotation.getAnnotationContents()).getType();
			System.out.println(annotation.getCoveredText()+"/"+neType);
		}
		
		System.out.println();
		System.out.println("Dependency trees:");
		printDependencyTrees(document);
		
		System.out.println();
		System.out.println("Coreference links:");
		for (Annotation<?> annotation : document.iterable(CoreferenceLink.class))
		{
			Annotation<CoreferenceLink> coreferenceLinkAnnotation = (Annotation<CoreferenceLink>)annotation;
			if ( coreferenceLinkAnnotation.getAnnotationReference().equals(coreferenceLinkAnnotation.getAnnotationContents().getFirst()) )
			{
				if (coreferenceLinkAnnotation.getAnnotationContents().getNext()!=null) // It is not an single (orphan) coref-link
				{
					Annotation<CoreferenceLink> a_coreferenceLinkAnnotation = coreferenceLinkAnnotation;
					while (a_coreferenceLinkAnnotation != null)
					{
						System.out.print(a_coreferenceLinkAnnotation.getCoveredText().replaceAll("\\s+", " ")+" -> ");
						AnnotationReference nextReference = a_coreferenceLinkAnnotation.getAnnotationContents().getNext();
						if (nextReference != null)
						{
							a_coreferenceLinkAnnotation = (Annotation<CoreferenceLink>) document.findAnnotation(nextReference, true);
						}
						else
						{
							a_coreferenceLinkAnnotation = null;
						}
					}
					System.out.println();
				}
			}
			
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private static void printDependencyTrees(Document document)
	{
		for (Annotation<?> sentenceAnnotation : document.iterable(Sentence.class))
		{
			Annotation<Dependency> root = null;
			Map<Annotation<Dependency>, List<Annotation<Dependency>>> mapParentToChildren = new LinkedHashMap<>();
			for (Annotation<?> dependencyAnnotation : document.iterable(Dependency.class, sentenceAnnotation.getBegin(), sentenceAnnotation.getEnd()))
			{
				Dependency dependency = (Dependency)dependencyAnnotation.getAnnotationContents();
				if (dependency.getGovernor()!=null)
				{
					if (dependency.getGovernor().equals(dependency.getDependent()))
					{
						root = (Annotation<Dependency>)dependencyAnnotation;
					}
					else
					{
						Annotation<?> governorTokenAnnotation = document.findAnnotation(dependency.getGovernor(), true);
						Annotation<Dependency> governorAnnotation = (Annotation<Dependency>) document.iterator(Dependency.class, governorTokenAnnotation.getBegin(), governorTokenAnnotation.getEnd()).next();
						mapParentToChildren.computeIfAbsent(governorAnnotation, (k)->new LinkedList<Annotation<Dependency>>()).add((Annotation<Dependency>)dependencyAnnotation);
					}
				}
				else
				{
					throw new DapException("Unexpected null governor for dependency tree.");
				}
			}
			printDependencyTree(mapParentToChildren, root, 0);
			System.out.println();
		}
	}
	
	
	private static void printDependencyTree(Map<Annotation<Dependency>, List<Annotation<Dependency>>> mapParentToChildren, Annotation<Dependency> node, int indent)
	{
		if (indent>0)
		{
			for (int i=0; i<(indent-1); ++i)
			{
				System.out.print("| ");
			}
			System.out.print("|-");
		}
		System.out.println(node.getCoveredText());
		
		List<Annotation<Dependency>> children = mapParentToChildren.get(node);
		if (children != null)
		{
			for (Annotation<Dependency> child : children)
			{
				printDependencyTree(mapParentToChildren, child, indent+1);
			}
		}
	}
}
