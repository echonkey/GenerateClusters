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

	
	private FastVector attr;
	private Instances ins;
	private List<Term> unFilterWords;
	private Map<String,Double> stems;
	
	private Map<Long,NewsContent> newsContent;
	
	public ContentSegment(){
		
	}
	
	public ContentSegment(Map newscontent){
		this.newsContent=newscontent;
		
	}
	
	/**	
	 * 从文件中读入新闻内容
	 * @param path：新闻内容的存储路径
	
	public void readData(String path){
		
		content=new ArrayList<String>();
		try { 
            BufferedReader reader = new BufferedReader(new FileReader(path));//换成你的文件名
            reader.readLine();
            String line = null; 
            while((line=reader.readLine())!=null){ 
                String item[] = line.split(",");                 
                String last = item[item.length-1];  
                content.add(last);                
            }
            System.out.println(content.size()); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
	}
	 */
	
	/**	
	 * Main Function of segment content
	 * 
	 * 
	 */
	public Instances contentSeg(){
		
		
		Map<Long,Map> contentWord=new HashMap<Long,Map>();
		List<String> wordVectors=new ArrayList<String>();
		
		//Pattern p=Pattern.compile("[0-9]+[^\\x00-\\xff]+");	//去除年、月
		for(Entry<Long, NewsContent> entry:newsContent.entrySet()){
			Long key=entry.getKey();
			NewsContent temp=entry.getValue();
			
			getKeyWords(temp.getContent(),temp.getTitle(),10);			
			contentWord.put(key,stems);
			
			if(wordVectors.isEmpty()){
				wordVectors.addAll(stems.keySet());
			}else{
				for(String s:stems.keySet() ){
					//Matcher m=p.matcher(s);
					if(!wordVectors.contains(s) && stems.get(s)>=2){
						wordVectors.add(s);
					}
				}
			}						
		}

				
		createArff(contentWord,wordVectors);
		contentWord.clear();
		wordVectors.clear();
		stems.clear();	
		return ins;
	}
	
	
	/**	
	 * 
	 */
	public void getKeyWords(String text,String title,int numKeys){
		
		stems = new HashMap<String, Double>();
		//keyWords=new ArrayList<KeyWord>();
		KeyWordComputer key=new KeyWordComputer(numKeys);  
        Iterator it = key.computeArticleTfidf(text,title).iterator() ;  
        while(it.hasNext()) {  
            KeyWord key2=(KeyWord)it.next(); 
            //keyWords.add(key2);
            stems.put(key2.toString(), key2.getScore());
           // System.out.println(key2.toString()+key2.getScore());      
        }    
	}
	
	/**	
	 * 对分词后的词组进行去词停用和词频统计
	 * @deprecated useless
	 * @return true for filter succeed, false for filter fail
	 */
	private boolean filterWords(){
		
		String stopWordPath="StopWords.txt";		
		List<String> stopWordList=new ArrayList<String>();
		String stopWords=new String();
		stems = new HashMap<String, Double>();
		
		double count;
		
		try {
			//读入停用词
			BufferedReader stopWordReader=new BufferedReader(new FileReader(stopWordPath));			
			while((stopWords=stopWordReader.readLine()) != null){
				stopWordList.add(stopWords);				
			}
			stopWordReader.close();
			
			//int len=unFilterWords.size();
			for (int i = 0; i < unFilterWords.size(); i++) {
				String key = unFilterWords.get(i).getName();
				
				//unFilterWords.get(i).getNatrue() ;
				//去停用词				
				if (stopWordList.contains(key)){					
					unFilterWords.remove(unFilterWords.get(i));	
				}else{
					//统计词频
					if (stems.containsKey(key)) {
						count = stems.get(key) + 1;
						stems.put(key, count);						
					} else {
						stems.put(key, 1.0);						
					}
				}
			}
			return true;			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**	
	 * 根据词组-词频建立arff文件
	 * @param contentWord：词组-词频
	 * @return true for create succeed,false for create fail
	 */
	public void createArff(Map<Long,Map> contentWord,List<String> wordVectors){
		
		
		attr=new FastVector();
		//设置属性栏，读入每个属性对应的值
		Iterator<String> it=wordVectors.iterator();		
		while(it.hasNext()){
			String attributes=it.next().toString();
			attr.addElement(new Attribute(attributes));
		}
		//wordVectors.clear();
		
		//读入数据,建立Instances实例
		double[][] values=new double[contentWord.size()][];
		ins=new Instances("news-content",attr,0);
		int i=0;
		for(Entry<Long,Map> entry:contentWord.entrySet()){
			//Long key=entry.getKey();
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
				//ins.add(m.get(attributes));
				j++;
			}
			ins.add(new DenseInstance(1.0, values[i]));
			i++;
		}
	
		
		//加入Instance ID
		ins.insertAttributeAt(new Attribute("auto_id"), 0);
		Set<Long> key=contentWord.keySet();
		int k=0;
		for(Iterator<Long> it_id=key.iterator();it_id.hasNext();k++){
			ins.instance(k).setValue(0,it_id.next());			
		}
		
		
		//return ins;
		
		//保存arff文件
//		ArffSaver save=new ArffSaver();
//		save.setInstances(ins);
//		try {
//			save.setFile(new File("news-content.arff"));
//			save.writeBatch();
//			return true;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
		
		
	}

//	public Map<Long,String> getContent(){
//		return this.content;		
//	}
//	
//	public void setContent(Map<Long,String> content){
//		this.content=content;
//	}
//	
//	public List<Term> getUnFilterWordList(){
//		return unFilterWords;
//	}	
//	
//	public Map<Long,Map> getWordFrequency(){
//		return contentWord;
//	}
//	
//	public List<String> getWordVectors(){
//		return wordVectors;
//	}
//	
}
