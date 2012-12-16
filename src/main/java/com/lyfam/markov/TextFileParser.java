package com.lyfam.markov;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.SerializationUtils;

public class TextFileParser implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private MarkovModelBuilder modelBuidler;
    
    public TextFileParser()
    {
    	modelBuidler = new MarkovModelBuilder();
    }
    
    public void saveMarkovModelForFile(File input, File output)
    {
    	Map<String, WordProbability[]> wordMap = modelBuidler.parse(input);
    	
    	FileOutputStream out;
		try
		{
			out = new FileOutputStream(output);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
    	
    	SerializationUtils.serialize((Serializable) wordMap, out);
    }
    
    public void generate(File file, String startWith) throws Exception
    {
        System.out.println(" -- Loading Words --");
        Map<String, WordProbability[]> wordMap = (Map<String, WordProbability[]>) SerializationUtils.deserialize(new FileInputStream(file));
        System.out.println(" -- done --");
        
        Random rand = new Random();
        
        StringBuffer sb = new StringBuffer();
        sb.append(startWith);
        
        int counted = 0;
        while (counted < 1000)
        {
            WordProbability[] array = wordMap.get(startWith);
            
            if (array == null)
            {
                array = wordMap.get("christmas");
            }
            
            counted++;
            double var = rand.nextDouble();
            int index = Arrays.binarySearch(array, new WordProbability("", var), new Comparator<WordProbability>()
            {
                @Override
                public int compare(WordProbability o1, WordProbability o2)
                {
                    if (o1.getWord().equals(o2.getWord()))
                        return 0;
                    
                    if (o1.getProb() > o2.getProb())
                        return 1;
                    else
                        return -1;
                }
                
            });
            
            index = index >= 0 ? index : -index - 1;
            sb.append(" ").append(array[index].getWord());
            
            startWith = array[index].getWord();
        }
        
        System.out.println(sb.toString());
    }
    
    public void prediction(File file) throws Exception
    {
    	System.out.println(" -- Loading Words --");
        Map<String, WordProbability[]> wordMap = (Map<String, WordProbability[]>) SerializationUtils.deserialize(new FileInputStream(file));
        System.out.println(" -- done --");
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("Type a text:");        
        String text = br.readLine();
        
        StringBuffer bf = new StringBuffer();
        while (!"quit".equals(text))
        {
        	bf.append(text).append(" ");
        	WordProbability[] array = wordMap.get(text);
        	if (array != null && array.length > 0)
        	{
        		int idx = array.length - 1;
        		System.out.print(bf.toString() + ": ");
        		int count = 0;
        		while (idx >= 0 && count < 5)
        		{
        			System.out.print(array[idx].getWord() + " --- ");
        			idx--;
        			count++;
        		}
        		System.out.println();
        	}
        	
        	text = br.readLine();
        }
    }
    
    private static Options makeCLIOptions()
    {
    	Option help = new Option( "help", "print this message" );
    	Option build = OptionBuilder.withArgName( "buildfile" )
						                .hasArg()
						                .withDescription(  "build the Markov model from a text file" )
						                .create( "build" );
    	
    	Option out = OptionBuilder.withArgName( "outfile" )
                .hasArg()
                .withDescription(  "The file name to save the built model" )
                .create( "out" );
    	
    	Option generate = OptionBuilder.withArgName( "startWord" )
                .hasArg()
                .withDescription(  "Generate random crap" )
                .create( "generate" );
    	
    	Option modelFile = OptionBuilder.withArgName( "modelFile" )
                .hasArg()
                .withDescription(  "Load from this model file" )
                .create( "modelFile" );
    	
    	Option prediction = OptionBuilder
                .withDescription(  "Predict a complete text" )
                .create( "predict" );
    	
    	Options options = new Options();

    	options.addOption(help);
    	options.addOption(build);
    	options.addOption(out);
    	options.addOption(generate);
    	options.addOption(modelFile);
    	options.addOption(prediction);
    	
    	return options;
    }
    
    public static void main(String... args) throws Exception
    {
    	Options opts = makeCLIOptions();
    	
    	HelpFormatter formatter = new HelpFormatter();
    	formatter.printHelp("markovizer", opts);
    	
    	CommandLineParser parser = new GnuParser();
    	CommandLine line = null;
    	try
    	{
            // parse the command line arguments
            line = parser.parse( opts, args );
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            System.exit(0);
        }
    	
    	TextFileParser p = new TextFileParser();
    	
    	if( line.hasOption( "build" ) )
    	{
    	    String buildfile = line.getOptionValue( "build" );
    	    if (!line.hasOption("out"))
    	    {
    	    	System.out.println("Must specify a output filename.");
    	    	return;
    	    }
    	    
    	    String outfile = line.getOptionValue("out");
    	    p.saveMarkovModelForFile(new File(buildfile), new File(outfile));
    	    
    	    return;
    	}
    	
    	if (line.hasOption("generate"))
    	{
    		String startWord = line.getOptionValue("generate");
    	    if (!line.hasOption("modelFile"))
    	    {
    	    	System.out.println("Must specify a model filename.");
    	    	return;
    	    }
    	    
    	    String modelFile = line.getOptionValue("modelFile");
    	    p.generate(new File(modelFile), startWord);
    	}
    	
    	if (line.hasOption("predict"))
    	{
    	    String modelFile = line.getOptionValue("modelFile");
            p.prediction(new File(modelFile));
    	}
    }
}
