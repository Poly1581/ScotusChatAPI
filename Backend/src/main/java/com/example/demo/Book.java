package com.example.demo;

import com.google.cloud.spring.data.datastore.core.mapping.Entity;
import org.springframework.data.annotation.Id;

@Entity(name = "books")
public class Book {
  @Id
  Long id;

  String userID;

  String title;

  String author;

  int year;

  public Book(String userID, String title, String author, int year) {
    this.userID = userID;
    this.title = title;
    this.author = author;
    this.year = year;
  }

  public long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
  	this.id=id;
  }

  public String getUserID() {
    return this.userID;
  }

  public void setUserID(String userID) {
    this.userID = userID;
  }
  
  public String getTitle() {
  	return this.title;
  }
  
  public void setTitle(String title) {
  	this.title=title;
  }
   public String getAuthor() {
  	return this.author;
  }
  
  public void setAuthor(String author) {
  	this.author=author;
  }
  
  public int getYear() {
  	return this.year;
  }
  
  public void setYear(int year) {
  	this.year=year;
  }
  

  @Override
  public String toString() {
    return "{" +
            "id:" + this.id +
            ", userID:'" + this.userID + '\'' +
            ", title:'" + this.title + '\'' +
            ", author:'" + this.author + '\'' +
            ", year:" + this.year +
            '}';
  }
}