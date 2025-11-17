package com.example.library.repository;

import com.example.library.model.ReadingHistory;
import com.example.library.model.User;
import com.example.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
    List<ReadingHistory> findByUser(User user);
    List<ReadingHistory> findByBook(Book book);
}
