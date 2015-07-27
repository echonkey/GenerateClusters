package com.clusters.java_generateclusters;

import java.sql.Date;

public class NewsContent {
	
	private long auto_id;
	private String content;
	private String title;
	private Date pubtime;
	
	public NewsContent(){
		
	}
	
	public void setNewsID(long id){
		this.auto_id=id;
	}
	
	public long getNewsID(){
		return this.auto_id;
	}
	
	public void setContent(String c){
		this.content=c;
	}
	
	public String getContent(){
		return this.content;
	}
	
	public void setTitle(String c){
		this.content=c;
	}
	
	public String getTitle(){
		return this.content;
	}
	
	public void setPublishTime(Date time){
		this.pubtime=time;
	}
	
	public Date getPublishTime(){
		return this.pubtime;
	}
	
	public String getTitleByID(int ID){
		return this.title;
	}
}
