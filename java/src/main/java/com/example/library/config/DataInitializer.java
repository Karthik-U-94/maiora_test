package com.example.library.config;

import com.example.library.model.Book;
import com.example.library.model.Category;
import com.example.library.model.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(BookRepository bookRepository, UserRepository userRepository) {
        return args -> {
            if (bookRepository.count() == 0) {
                bookRepository.save(new Book(1L, "The Time Machine", "H. G. Wells", "ISBN-001", LocalDate.of(1895,1,1), Category.SCIENCE));
                bookRepository.save(new Book(2L, "A Brief History of Time", "Stephen Hawking", "ISBN-002", LocalDate.of(1988,4,1), Category.SCIENCE));
                bookRepository.save(new Book(3L, "The Hobbit", "J.R.R. Tolkien", "ISBN-003", LocalDate.of(1937,9,21), Category.FANTASY));
                bookRepository.save(new Book(4L, "1984", "George Orwell", "ISBN-004", LocalDate.of(1949,6,8), Category.FICTION));
                bookRepository.save(new Book(5L, "Sapiens", "Yuval Noah Harari", "ISBN-005", LocalDate.of(2011,1,1), Category.HISTORY));
            }

            if (userRepository.count() == 0) {
                userRepository.save(new User(1L, "Alice", "alice@example.com"));
                userRepository.save(new User(2L, "Bob", "bob@example.com"));
            }
        };
    }
}
