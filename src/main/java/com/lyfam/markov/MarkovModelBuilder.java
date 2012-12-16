package com.lyfam.markov;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.lyfam.markov.WordFrequency;
import com.lyfam.markov.WordProbability;

public class MarkovModelBuilder
{
	public Map<String, WordProbability[]> parse(File inputFile)
    {
        try
        {
            Map<String, WordFrequency> transitionMap = new HashMap<String, WordFrequency>();
            LineIterator li = FileUtils.lineIterator(inputFile);
            
            String prevWord = null;
            
            while (li.hasNext())
            {
                String line = (String) li.next();
                StringTokenizer wordTokenizer = new StringTokenizer(line);
                while (wordTokenizer.hasMoreTokens())
                {
                    String word = wordTokenizer.nextToken().toLowerCase();
                    
                    if (prevWord == null)
                    {
                        WordFrequency wf = new WordFrequency();
                        transitionMap.put(word, wf);
                    }
                    else
                    {
                        WordFrequency wf = transitionMap.get(prevWord);
                        if (wf == null)
                        {
                            wf = new WordFrequency();
                            wf.addWord(word);
                            transitionMap.put(prevWord, wf);
                        }
                        else
                        {
                            wf.addWord(word);
                        }
                    }
                    
                    prevWord = word;
                }
            }
            
            return this.getCalculateProb(transitionMap);
        }
        catch (Throwable t)
        {
            throw new RuntimeException(t);
        }
    }
    
    private Map<String, WordProbability[]> getCalculateProb(Map<String, WordFrequency> wordFreqMap)
    {
        Map<String, WordProbability[]> res = new HashMap<String, WordProbability[]>();
        for (Entry<String, WordFrequency> entry : wordFreqMap.entrySet())
        {
        	WordProbability[] array = entry.getValue().getSortedProbWordArray();
        	if (array.length > 0)
        		res.put(entry.getKey(), array);
        }
        
        return res;
    }
}
