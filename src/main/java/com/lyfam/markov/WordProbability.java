package com.lyfam.markov;

import java.io.Serializable;

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
