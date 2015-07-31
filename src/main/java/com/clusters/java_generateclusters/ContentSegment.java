package com.clusters.java_generateclusters;

import java.io.BufferedReader;
import java.io.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

import weka.core.*;
import weka.core.converters.ArffSaver;

import org.ansj.domain.Term;

/**	
 * Segment content and get keyWords using ansj
 * <input>Map<id,content></input>
 * <output>news-content.arff</output>
 * <q>Date: 2015-7-16 上午10:00</p>
 * @author Shixian,Lei
 * @Version 1.0
 *
 */

public class ContentSegment {

	private Instances ins;	
	private Map<Long,NewsContent> newsContent;
	
	public ContentSegment(){
		
	}
	
	public ContentSegment(Map newscontent){
		this.newsContent=newscontent;
		
	}
	
	
	/**	
	 * <p>
	 * Main Function of segment content
	 * 1.Get the keyWords and TFIDF as a map of each contentWord
	 * 2.Get the whole keyWords Vectors of all the contentWord
	 * </p>
	 * <related>
	 * 	<param>	
	 * 		Map	contentWord:Key->contentID,value->Map<keyWords,TFIDF> stems
	 * 		List wordVectors
	 * 	</param>
	 * 	<method>
	 * 		getKeyWords(content,title,num)
	 * 	</method>
	 * </related>
	 * 
	 * @return Instances:Attributes->wordVectors,Instance->TFIDF
	 * 
	 */
	public Instances contentSeg(){		
		
		/** Initialize*/
		Map<Long,Map> contentWord=new HashMap<Long,Map>();
		List<String> wordVectors=new ArrayList<String>();
		
		for(Entry<Long, NewsContent> entry:newsContent.entrySet()){
			
			/**Get KeyWords of each content*/
			Long key=entry.getKey();
			NewsContent temp=entry.getValue();	
			Map<String,Double> stems=new HashMap<String, Double>();
			stems=getKeyWords(temp.getContent(),temp.getTitle(),10);			
			contentWord.put(key,stems);
			
			/**Generate wordVectors*/
			if(wordVectors.isEmpty()){
				wordVectors.addAll(stems.keySet());
			}else{
				for(String s:stems.keySet() ){
					//Matcher m=p.matcher(s);
					if(!wordVectors.contains(s)){
						wordVectors.add(s);
					}
				}
			}	
			//stems.clear();
			
		}
		
		/**Save wordVectors and contentWord*/
		createArff(contentWord,wordVectors);
		
		/**Clear List*/
		contentWord.clear();
		wordVectors.clear();
			
		return ins;
	}
	
	
	/**	
	 * Generate Key Words of one article mainly with its content and title
	 * @param	text:content of the article
	 * 			title:title of the article
	 * 			numKeys: the number of keys to generate
	 * <related>
	 * 	<method>
	 * 		KeyWordComputer.computeArticleTfidf(text,title)
	 * 	</method>
	 * </related>
	 * @return	Map<String,Double>:key->Key Word,value->TFIDF
	 * 
	 */
	public Map<String,Double> getKeyWords(String text,String title,int numKeys){
		
		Map<String,Double> stems = new HashMap<String, Double>();
		KeyWordComputer key=new KeyWordComputer(numKeys);  
        Iterator it = key.computeArticleTfidf(text,title).iterator() ;  
        while(it.hasNext()) {  
            KeyWord key2=(KeyWord)it.next();             
            stems.put(key2.toString(), key2.getScore());               
        }  
        
        return stems;
	}
	
	/**	
	 * create original instances with ID
	 * @param 	Map contentWord:Key->contentID,value->Map<keyWords,TFIDF> stems
	 * 			List wordVectors
	 * 
	 */
	public void createArff(Map<Long,Map> contentWord,List<String> wordVectors){
		
		/** Set the attributes vector of instances*/
		FastVector attr=new FastVector();	
		Iterator<String> it=wordVectors.iterator();		
		while(it.hasNext()){
			String attributes=it.next().toString();
			attr.addElement(new Attribute(attributes));
		}
		
		/** Read the values of each instance*/
		double[][] values=new double[contentWord.size()][];
		ins=new Instances("news-content",attr,0);
		int i=0;
		for(Entry<Long,Map> entry:contentWord.entrySet()){
			Map m=entry.getValue();	
			values[i]=new double[wordVectors.size()]; 
			Iterator<String> it_key=wordVectors.iterator();
			int j=0;
			while(it_key.hasNext()){
				String attributes=it_key.next().toString();
				if(m.containsKey(attributes)){	
					values[i][j]=(Double) m.get(attributes) ;				
				}else{
					values[i][j]=0;
				}
				
				j++;
			}
			ins.add(new DenseInstance(1.0, values[i]));
			i++;
		}
	
		
		/** Add instance ID for instances*/
		ins.insertAttributeAt(new Attribute("auto_id"), 0);
		Set<Long> key=contentWord.keySet();
		int k=0;
		for(Iterator<Long> it_id=key.iterator();it_id.hasNext();k++){
			ins.instance(k).setValue(0,it_id.next());			
		}
		
		/** save the instances*/
		ArffSaver save=new ArffSaver();
		save.setInstances(ins);
		try {
			save.setFile(new File("news-content.arff"));
			save.writeBatch();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}	
		
	}

}
