package com.example.library.repository;

import com.example.library.model.Book;
import com.example.library.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);
    List<Book> findByAuthor(String author);
    List<Book> findByCategory(Category category);
}
