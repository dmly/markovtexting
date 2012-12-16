package com.lyfam.markov;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lyfam.markov.WordProbability;

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
