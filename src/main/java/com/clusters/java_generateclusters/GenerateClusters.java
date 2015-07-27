package com.clusters.java_generateclusters;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import weka.clusterers.*;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.DataObject;
import weka.clusterers.forOPTICSAndDBScan.Databases.Database;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**	
 * Cluster Main Code using Weka API
 * <input>*.arff<input>
 * <output>ClusterResult.txt</output>
 * <p>Date: 2015-7-13 上午9:26:30</p>
 * @author Shixian.Lei
 * @version 1.0
 * 
 */

public class GenerateClusters {

	
	private String filePath;  
	private int clusterType;  
	private Instances ins;
	private FilteredClusterer fc;
	private List<Set> keyAttributes;
	private List<List> clusterID;
	private ClusterEvaluation eval;
	private StringBuffer clusterResults;	
	private Map<Long,NewsContent> newsContent;
	
	
	public GenerateClusters(){
		
	}
	
	public GenerateClusters(Map newscontent){
		this.newsContent=newscontent;
		
	}
	
	/**	
	 * 对外可调用聚类接口
	 * @param filePath 为.arff文件路径
	 * @param clusterType 为选择的聚类方法：1代表X-Means算法(KMeans算法升级版，可自动选择聚类簇数)，
	 * 								   2代表Canopy算法，
	 *                                 3代表EM算法，
	 *                                 4代表Cobweb算法，
	 *                                 5代表DBSCAN算法
	 *                                                                 
	 * @return 根据不同的聚类算法返回每类的特征词向量、包含的Instance ID、聚类时间
	 * @throws CobWeb/DBSCAN can't get keyWords Vector of each cluster
	 */
	public String doCluster(int clusterType){
		
		long startTime=System.currentTimeMillis();
		
		//setFilePath(filePath);
		setClusterType(clusterType);
		
		if(loadData()){	
			System.out.println("创建结果："+true);
			if(buildClusterByType()){
				//evaluateCluster();
				System.out.println("聚类过程成功！");
			}else{
				System.out.println("聚类过程失败，请重新再试！");
			}			
		}else{
			System.out.println("无法加载数据，请检查数据格式是否正确！");
		}
		
		long endTime=System.currentTimeMillis();
		
		clusterResults.append("聚类运行时间为："+(double)((endTime-startTime)/1000)+"秒");
		saveCluster();
		return clusterResults.toString();
	}
	
	
	
	/**	
	 * 根据filePath加载.arff数据
	 * 
	 * @return true for load success,false for load fail
	 */
	private boolean loadData(){
		
		/** read content that need to build clusters*/
		ContentSegment contentSegment=new ContentSegment(newsContent);
		
		/** Segment content into WordsVectors*/
		ins=contentSegment.contentSeg();
		
		return ins.isEmpty()?false:true;
		
		//load data through .arff files
//		File inputFile=new File(filePath);
//		ArffLoader loader=new ArffLoader();
//		try{
//			loader.setFile(inputFile);
//			ins=loader.getDataSet();
//			return true;
//		}catch(Exception e){
//			System.out.println(e.getMessage());
//			return false;
//		}
	}
	
	/**	
	 * 根据ClusterType选择聚类算法，进行聚类
	 * @param FilteredClusterer fc;
	 * 		  StringBuffer result;
	 * 		  Instances ins;
	 * <method> setFilters();
	 * 		   buildXMeansCluster(filteredInstances);
	 * 		   buildCanopyCluster(filteredInstances);
	 * 		   buildEMCluster(filteredInstances);
	 * </method>
	 * @return true for build success,false for build fail
	 */
	private boolean buildClusterByType(){
		
		clusterResults=new StringBuffer();
		fc = new FilteredClusterer();
		Instances newData=setFilters();			                                   
		
		if(clusterType==1){
			if(buildXMeansCluster(newData))
				return true;
			else return false;			
		}else if(clusterType==2){
			if(buildCanopyCluster(newData))
				return true;
			else return false;
		}else if(clusterType==3){
			if(buildEMCluster(newData))
				return true;
			else return false;
		}else if(clusterType==4){
			Cobweb coCluster=new Cobweb();
			try{
				fc.setClusterer(coCluster);
				fc.buildClusterer(ins);
				coCluster=(Cobweb) fc.getClusterer();
				clusterResults.append(coCluster.globalInfo()+"\n");
				clusterResults.append(coCluster.toString());
				return true;
			}catch(Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}else if(clusterType==5){
			DBSCAN dbCluster=new DBSCAN();
			dbCluster.setEpsilon(0.5);
			try {
				fc.setClusterer(dbCluster);
				fc.buildClusterer(ins);
				dbCluster=(DBSCAN) fc.getClusterer();				
				clusterResults.append(dbCluster.globalInfo());
				clusterResults.append(dbCluster.toString());
				
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}else{
			System.out.println("输入聚类类型不正确！！！");
			return false;
		}
		
	}
	
	/**	
	 * 对聚类结果Cluster进行评估
	 * @return true for evaluate success, false for evaluate failure
	 * @deprecated useless
	 */
	private boolean evaluateCluster(){
		//评价聚类结果
		eval=new ClusterEvaluation();
		eval.setClusterer(fc);
		try {
			eval.evaluateClusterer(ins);
			//clusterResults.append(eval.clusterResultsToString());
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**	
	 * 将聚类及聚类评估结果写入文件
	 * @category need to change
	 * 
	 */
	private void saveCluster(){
		//保存聚类结果
		try {
			BufferedWriter bw =new BufferedWriter(new FileWriter("ClusterResult.txt"));
			bw.write(clusterResults.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**	
	 * Handle the original instances(with instance ID) 
	 * to new one(remove ID)
	 * @return new instances(without instance ID)
	 */	
	private Instances setFilters(){
		
		/** set filter options */
		String[] options = new String[2];
		options[0] = "-R";                                    
		options[1] = "1";  //the index of specific attribute
		                        
		try {
			Remove remove = new Remove(); 
			remove.setOptions(options);
			remove.setInputFormat(ins);
			Instances newData=Filter.useFilter(ins, remove);
			fc.setFilter(remove);
			return newData;			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 	
		
	}
	
	/**	
	 * Build Clusters with XMeans algorithm
	 * @param newData: instances without ID
	 * <related>
	 * 	<param>	fc---FilteredCluster
	 * 			ins---original instances(with ID)
	 * 	</param>
	 * 	<method>getKeyAttributes(clusterCenterInstances)
	 * 			getInfo(Clusterer,newInstances)
	 * 	</method>
	 * </related>
	 * @return true for build success,false for build fail
	 * <p>don't mix ins and newData</p>
	 */
	private boolean buildXMeansCluster(Instances newData){
		
		try {
			
			/** Initialize XMeans Clusterer and Set Parameters */
			XMeans xCluster=new XMeans();
			xCluster.setMaxNumClusters(100);
			xCluster.setSeed(100);
			
			/** Build XMeans Clusterer with FilterCluster */
			fc.setClusterer(xCluster);
			fc.buildClusterer(ins);     //NOTE:build with original instances
			xCluster=(XMeans) fc.getClusterer();
			clusterResults.append(xCluster.globalInfo()+"\n");
			
			/** Abstract KeyWords Vectors of clusterCenters */
			Instances xmeansAssignments=xCluster.getClusterCenters();
			generateKeyAttributes(xmeansAssignments);
			
			/** Get clusterID of each instance  */
			generateClusterID(xCluster,newData); //NOTE:new instances				
			
			/** Save clusterID and keyAttributes  */
			int numberCluster=xCluster.numberOfClusters();
			saveClusterAttributes(numberCluster);
			
			return true;
	
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
		}
	}
	
	/**	
	 * Build Clusters with Canopy algorithm
	 * @param newData: instances without ID
	 * <related>
	 * 	<param>	fc---FilteredCluster
	 * 			ins---original instances(with ID)
	 * 	</param>
	 * 	<method>getKeyAttributes(clusterCenterInstances)
	 * 			getInfo(Clusterer,newInstances)
	 * 	</method>
	 * </related>
	 * @return true for build success,false for build fail
	 * <p>don't mix ins and newData</p>
	 */
	private boolean buildCanopyCluster(Instances newData){		
		
		try{	
			
			/** Initialize Canopy Clusterer and Set Parameters */
			Canopy canCluster=new Canopy();
			canCluster.setSeed(10);
			canCluster.setT2(0.001);
			//canCluster.setNumClusters(5);
			
			/** Build Canopy Clusterer with FilterCluster */
			fc.setClusterer(canCluster);
			fc.buildClusterer(ins);
			canCluster=(Canopy) fc.getClusterer();
			clusterResults.append(canCluster.globalInfo()+"\n");
			
			/** Abstract KeyWords Vectors of clusterCenters */
			Instances canopyAssignments=canCluster.getCanopies();
			generateKeyAttributes(canopyAssignments);
			
			/** Get clusterID of each instance   */
			generateClusterID(canCluster,newData);					
			
			/** Save clusterID and keyAttributes  */
			int numberCluster=canCluster.numberOfClusters();
			saveClusterAttributes(numberCluster);
			
			return true;
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**	
	 * Build Clusters with EM algorithm
	 * @param newData: instances without ID
	 * <related>
	 * 	<param>	fc---FilteredCluster
	 * 			ins---original instances(with ID)
	 * 	</param>
	 * 	<method>getKeyAttributes(clusterCenterInstances)
	 * 			getInfo(Clusterer,newInstances)
	 * 	</method>
	 * </related>
	 * @return true for build success,false for build fail
	 * <p>don't mix ins and newData</p>
	 */
	private boolean buildEMCluster(Instances newData){		
		
		try {
			
			/** Initialize EM Clusterer and Set Parameters */
			EM emCluster=new EM();
			emCluster.setMaxIterations(10);
			emCluster.setNumKMeansRuns(10);
			
			/** Build EM Clusterer with FilterCluster */
			fc.setClusterer(emCluster);
			fc.buildClusterer(ins);
			emCluster=(EM) fc.getClusterer();
			clusterResults.append(emCluster.globalInfo()+"\n");
			
			/** Abstract KeyWords Vectors of clusterCenters */
			keyAttributes=new ArrayList<Set>();
			int numberCluster=emCluster.numberOfClusters();
			int numberAttr=newData.numAttributes();
			double[][][] modelNormal=emCluster.getClusterModelsNumericAtts();
					
			for (int i = 0; i < numberCluster; i++) {
				Set<String> s=new HashSet<String>();
				Instance data=ins.get(i);
			      for (int j = 0; j < numberAttr; j++) {
			    	  double mean = Math.log(Math.abs(modelNormal[i][j][0])) / Math.log(10.0D);
			    	  if(mean>0)
			    		  s.add(data.attribute(j).name());				    		  
			      }
			      
			      keyAttributes.add(s);			      
			}
			
			/** Get clusterID of each instance   */
			generateClusterID(emCluster,newData);	
			
			/** Save clusterID and keyAttributes  */
			saveClusterAttributes(numberCluster);
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	/**	
	 * Get corresponding keyWords of each clusterCenter
	 * @param dataNodes clusterCenterInstances
	 * @return a list of keyWord sets sorted by their TDIDF values 
	 */	
	private void generateKeyAttributes(Instances dataNodes){
		
		keyAttributes=new ArrayList<Set>();		
		for(int i=0;i<dataNodes.size();i++){	
			Map<String,Double> m=new HashMap<String,Double>();
			Instance data=dataNodes.instance(i);			
			for(int j=0;j<data.numAttributes();j++){
				if(data.value(j)>=1){
					m.put(data.attribute(j).name(),data.value(j));
				}							
			}
			m=sortMapByValue(m);					
			keyAttributes.add(m.keySet());			
		}		
		
	}
	
	/**	
	 * Generate instance ids of each cluster  
	 * @param c:	Clusterer
	 * @param newData:	instances without id
	 * @related param: ins---instances with id
	 * @throws Exception
	 */
	private void generateClusterID(Clusterer c,Instances newData) throws Exception{
		
		/** Generate ClusterID */
		clusterID=new LinkedList<List>();
		int numberCluster=c.numberOfClusters();
		
		for(int i=0;i<numberCluster;i++){
			
			List<Long> temp =new LinkedList<Long>();	
			int count=0;	//count the number of each cluster
			
			for(int j=0;j<newData.size();j++){
				int	clusterNum = c.clusterInstance(newData.get(j));
					if(clusterNum==i){
						temp.add((long)ins.instance(j).value(0));	
						count++;
					}
					
			}			
			temp.add((long) count);	//NOTE: count is in the last index of a list
			clusterID.add(temp);
		}	
		
	}
	
	/**	
	 * Save ClusterID and keyAttributes  
	 * @param numberCluster:	number of clusters of Clusterer
	 * @related param:	ins---instances with id
	 * 					keyAttributes---KeyWords Vector of each cluster
	 * 					clusterID----each cluster's instance
	 * @throws Exception
	 */
	private void saveClusterAttributes(int numberCluster){
		
		/** Save ClusterID and keyAttributes*/
		clusterResults.append("共有"+ins.numInstances()+"条记录"+"\n");
		clusterResults.append("共聚成"+numberCluster+"类"+"\n");			
		for(int i=0;i<numberCluster;i++){
			clusterResults.append("第"+(i+1)+"个类特征词向量为"+keyAttributes.get(i).toString()+"\n");
			LinkedList temp=(LinkedList) clusterID.get(i);		
			clusterResults.append("第"+(i+1)+"类新闻记录共有"+temp.getLast()+"条\n"+"新闻记录ID为\n");
			for(int j=0;j<temp.size()-1;j++){
//				NewsContent nc=newsContent.get(temp.get(j));
//				System.out.println(nc.getNewsID()+nc.getTitle());
				clusterResults.append(temp.get(j)+" "+newsContent.get((Long)temp.get(j)).getTitle()+"\n");	
			}
		}
		
		/** Clear Lists*/
		keyAttributes.clear();
		clusterID.clear();
	}
	
	/**	
	 * Sort Map By Value
	 * @param map Word-TDIDF selected by getKeyAttributes(ClusterCenterInstances)
	 * @return Sorted Word-TDIDF map
	 */
	private Map<String,Double> sortMapByValue(Map<String,Double> map){
		List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {

            public int compare(Object o1, Object o2) {     
                
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });
        
        //int i=0;
        //int len=20;//选取前二十个记录
        Map result = new LinkedHashMap<String,Double>();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
	}
	
	private String getTitleByID(Long ID){
		
		String title="";
		if(newsContent.containsKey(ID))
			title=newsContent.get(ID).getTitle();
		return title;
	
	}
	
	/**	
	 * Interface of filePath
	 * @param filePath 
	 */
	
	public void setFilePath(String filePath){
		this.filePath=filePath;
	}
	
	public String getFilePath(){
		return filePath;
	}
	
	/**	
	 * Interface of cluster algorithm
	 * @param clusterType
	 */
	
	public void setClusterType(int clusterType){
		this.clusterType=clusterType;
	}
	
	public int getClusterType(){
		return clusterType;
	}
	
}
