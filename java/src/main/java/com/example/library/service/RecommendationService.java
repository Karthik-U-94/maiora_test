package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.model.Category;
import com.example.library.model.ReadingHistory;
import com.example.library.model.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReadingHistoryRepository;
import com.example.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReadingHistoryRepository readingHistoryRepository;

    public RecommendationService(UserRepository userRepository, BookRepository bookRepository, ReadingHistoryRepository readingHistoryRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.readingHistoryRepository = readingHistoryRepository;
    }

    /**
     * Simple recommendation algorithm:
     * 1) Find categories the user reads most.
     * 2) Recommend books from those categories the user hasn't read.
     * 3) If not enough, recommend popular books (by overall read count) excluding user's read books.
     */
    public List<Book> recommendForUser(Long userId, int limit) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) return Collections.emptyList();
        User user = userOpt.get();

        List<ReadingHistory> userHistory = readingHistoryRepository.findByUser(user);
        Set<Long> readBookIds = userHistory.stream()
                .filter(r -> r.getStatus() == ReadingHistory.Status.READ)
                .map(r -> r.getBook().getId())
                .collect(Collectors.toSet());

        // count categories
        Map<Category, Long> categoryCounts = userHistory.stream()
                .map(ReadingHistory::getBook)
                .filter(Objects::nonNull)
                .map(Book::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<Book> allBooks = bookRepository.findAll();
        List<Book> recommendations = new ArrayList<>();

        // prefer categories user likes
        List<Category> favoriteCategories = categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<Category, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (Category c : favoriteCategories) {
            for (Book b : allBooks) {
                if (recommendations.size() >= limit) break;
                if (b.getCategory() == c && !readBookIds.contains(b.getId()) && !recommendations.contains(b)) {
                    recommendations.add(b);
                }
            }
            if (recommendations.size() >= limit) break;
        }

        if (recommendations.size() < limit) {
            // fallback: popular books by overall read count
            Map<Long, Long> popularity = readingHistoryRepository.findAll().stream()
                    .filter(r -> r.getStatus() == ReadingHistory.Status.READ)
                    .collect(Collectors.groupingBy(r -> r.getBook().getId(), Collectors.counting()));

            List<Long> popularBookIds = popularity.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            for (Long id : popularBookIds) {
                if (recommendations.size() >= limit) break;
                if (readBookIds.contains(id)) continue;
                bookRepository.findById(id).ifPresent(b -> { if (!recommendations.contains(b)) recommendations.add(b); });
            }
        }

        // final fallback: any unread books
        if (recommendations.size() < limit) {
            for (Book b : allBooks) {
                if (recommendations.size() >= limit) break;
                if (!readBookIds.contains(b.getId()) && !recommendations.contains(b)) recommendations.add(b);
            }
        }

        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }
}
