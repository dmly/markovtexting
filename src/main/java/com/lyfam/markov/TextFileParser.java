package com.lyfam.markov;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.SerializationUtils;

public class TextFileParser implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private File inputFIle;
    
    public TextFileParser(File input)
    {
        this.inputFIle = input;
    }
    
    public Map<String, WordProbability[]> parse()
    {
        try
        {
            Map<String, WordFrequency> transitionMap = new HashMap<String, WordFrequency>();
            LineIterator li = FileUtils.lineIterator(inputFIle);
            
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
                        TextFileParser.WordFrequency wf = new WordFrequency();
                        transitionMap.put(word, wf);
                    }
                    else
                    {
                        TextFileParser.WordFrequency wf = transitionMap.get(prevWord);
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
        Map<String, WordProbability[]> res = new HashMap<String, TextFileParser.WordProbability[]>();
        for (Entry<String, WordFrequency> entry : wordFreqMap.entrySet())
        {
        	WordProbability[] array = entry.getValue().getSortedProbWordArray();
        	if (array.length > 0)
        		res.put(entry.getKey(), array);
        }
        
        return res;
    }
    
    public void generate(File file, String startWith) throws Exception
    {
        System.out.println(" -- Loading Words --");
        Map<String, WordProbability[]> wordMap = (Map<String, WordProbability[]>) SerializationUtils.deserialize(new FileInputStream(file));
        System.out.println(" -- done --");
        
        TextFileParser p = new TextFileParser(null);
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
            int index = Arrays.binarySearch(array, p.new WordProbability("", var), new Comparator<WordProbability>()
            {
                @Override
                public int compare(WordProbability o1, WordProbability o2)
                {
                    if (o1.word.equals(o2.word))
                        return 0;
                    
                    if (o1.prob > o2.prob)
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
        
        while (!"quit".equals(text))
        {
        	WordProbability[] array = wordMap.get(text);
        	if (array != null && array.length > 0)
        	{        		
        		int idx = array.length - 1;
        		System.out.print(text + ": ");
        		int count = 0;
        		while (idx >= 0)
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
    
    public static void main(String... args) throws Exception
    {
//        File file = new File("/Users/dmly/Downloads/christmascarol.txt");
//        TextFileParser p = new TextFileParser(file);
//        Map<String, WordProbability[]> res = p.parse();
//
//        FileOutputStream out = new FileOutputStream("/Users/dmly/Downloads/christmascarol.bin");
//        SerializationUtils.serialize((Serializable) res, out);
        
        TextFileParser p = new TextFileParser(null);
        File file = new File("/Users/dmly/Downloads/christmascarol.bin");
        //p.generate(file, "christmas");
        
        p.prediction(file);
    }
    
    class WordFrequency implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private Map<String, Long> wordMap;

        private long sum = 0L;
        
        public long getSum()
        {
            return sum;
        }

        public void setSum(long sum)
        {
            this.sum = sum;
        }

        public WordFrequency()
        {
            wordMap = new HashMap<String, Long>();
        }
        
        public void addWord(String word)
        {
            sum += 1;
            
            Long count = wordMap.get(word);
            if (count == null)
            {
                wordMap.put(word, 1L);
            }
            else
            {
                wordMap.put(word, count + 1);
            }
        }
        
        public WordProbability[] getSortedProbWordArray()
        {
            WordProbability[] res = new WordProbability[this.wordMap.size()];
            
            int i = 0;
            for (Entry<String, Long> entry : this.wordMap.entrySet())
            {
                res[i] = new WordProbability(entry.getKey(), (double) entry.getValue() / this.sum);
                i++;
            }
            
            Arrays.sort(res, new Comparator<WordProbability>()
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
            
            for (int idx = 1; idx < res.length; idx++)
            {
                res[idx].setProb(res[idx - 1].getProb() + res[idx].getProb());
            }
            
            return res;
        }
    }    
    
    class WordProbability implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        private String word;
        private double prob;
        
        public WordProbability(String word, double prob)
        {
            this.word = word;
            this.prob = prob;
        }
        
        public void setWord(String word)
        {
            this.word = word;
        }

        public void setProb(double prob)
        {
            this.prob = prob;
        }
        
        public String getWord()
        {
            return word;
        }

        public double getProb()
        {
            return prob;
        }
        
    }
}
