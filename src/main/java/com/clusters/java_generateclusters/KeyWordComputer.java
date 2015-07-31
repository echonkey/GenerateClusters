package com.clusters.java_generateclusters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.ansj.domain.Term;
import casia.basic.groupfour.mlseg.analysis.ZhAnalyzer;

import org.ansj.splitWord.analysis.NlpAnalysis;

public class KeyWordComputer {
    private int nKeyword = 10;
    //default constructor keyword number=10
    public KeyWordComputer() {
        nKeyword = 10;
    }
    // constructor set keyword number
    public KeyWordComputer(int nKeyword) {
        this.nKeyword = nKeyword;

    }
    //get keywords object list
    private List<KeyWord> computeArticleTfidf(String content, int titleLength) {
        Map<String, KeyWord> tm = new HashMap<String, KeyWord>();
       
        //LearnTool learn = new LearnTool();
        List<Term> parse = ZhAnalyzer.parse(content);
        //parse = NlpAnalysis.parse(content, learn);
        parse=filterWords(parse);
        if(parse != null){
        	for (Term term : parse) {
                int weight = getWeight(term, content.length(), titleLength);
                if (weight == 0)
                    continue;
                KeyWord keyword = tm.get(term.getName());
                if (keyword == null) {
                    keyword = new KeyWord(term.getName(), term.getNatrue().allFrequency, weight);
                    tm.put(term.getName(), keyword);
                } else {
                    keyword.updateWeight(1);
                }
            }
        }
        
        TreeSet<KeyWord> treeSet = new TreeSet<KeyWord>(tm.values());
        ArrayList<KeyWord> arrayList = new ArrayList<KeyWord>(treeSet);
        if (treeSet.size() < nKeyword) {
            return arrayList;
        } else {
            return arrayList.subList(0, nKeyword);
        }
    }
    
    //filter meaningless words
    private List<Term> filterWords(List<Term> unFilterWords){
		
		String stopWordPath="StopWords.txt";		
		List<String> stopWordList=new ArrayList<String>();
		String stopWords=new String();	
		
		try {
			//读入停用词
			BufferedReader stopWordReader=new BufferedReader(new FileReader(stopWordPath));			
			while((stopWords=stopWordReader.readLine()) != null){
				stopWordList.add(stopWords);				
			}
			stopWordReader.close();
			
			int len=unFilterWords.size();
			for (int i = 0; i < len; i++) {
				String key = unFilterWords.get(i).getName();
				//去停用词				
				if (stopWordList.contains(key)){	
					unFilterWords.remove(i);
					--len;
					--i;
				}
			}
			return unFilterWords;			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
    
    //get keywords,need title and content
    public Collection<KeyWord> computeArticleTfidf(String title, String content) {
        return computeArticleTfidf(title + "\t" + content, title.length());
    }
    //get keywords, just need content
    public Collection<KeyWord> computeArticleTfidf(String content) {
        return computeArticleTfidf(content, 0);
    }
    //get keywords weight
    private int getWeight(Term term, int length, int titleLength) {
        if (term.getName().matches("(?s)\\d.*")) {
            return 0;
        }
        if (term.getName().trim().length() < 2) {
            return 0;
        }
        String pos = term.getNatrue().natureStr;
        if (!pos.startsWith("n")|| "num".equals(pos)) {
            return 0;
        }
        int weight = 0;
        if (titleLength > term.getOffe()) {
            return 20;
        }
        
        // position
        double position = (term.getOffe() + 0.0) / length;
        if (position < 0.05)
            return 10;
        weight += (5 - 5 * position);
        return weight;
    }
}