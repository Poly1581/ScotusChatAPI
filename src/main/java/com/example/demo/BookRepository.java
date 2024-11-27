package com.example.demo;

import java.util.List;

import com.google.cloud.spring.data.datastore.repository.DatastoreRepository;


public interface BookRepository extends DatastoreRepository<Book, Long> {

  List<Book> findByAuthor(String author);

  List<Book> findByYear(int year);

  List<Book> findByTitle(String title);

  List<Book> findByYearGreaterThan(int year);

  List<Book> findByAuthorAndYear(String author, int year);
}