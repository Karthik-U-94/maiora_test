package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.model.ReadingHistory;
import com.example.library.model.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReadingHistoryRepository;
import com.example.library.service.RecommendationService;
import com.example.library.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final BookRepository bookRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final RecommendationService recommendationService;

    public UserController(UserService userService, BookRepository bookRepository, ReadingHistoryRepository readingHistoryRepository, RecommendationService recommendationService) {
        this.userService = userService;
        this.bookRepository = bookRepository;
        this.readingHistoryRepository = readingHistoryRepository;
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userService.createUser(user);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{userId}/reading-list/{bookId}")
    public ResponseEntity<?> addToReadingList(@PathVariable Long userId, @PathVariable Long bookId) {
        Optional<User> u = userService.getUser(userId);
        Optional<Book> b = bookRepository.findById(bookId);
        if (!u.isPresent() || !b.isPresent()) return ResponseEntity.notFound().build();
        ReadingHistory rh = new ReadingHistory(bookId, u.get(), b.get(), ReadingHistory.Status.READING_LIST, LocalDateTime.now());
        readingHistoryRepository.save(rh);
        return ResponseEntity.ok(rh);
    }

    @PostMapping("/{userId}/mark-read/{bookId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long userId, @PathVariable Long bookId) {
        Optional<User> u = userService.getUser(userId);
        Optional<Book> b = bookRepository.findById(bookId);
        if (!u.isPresent() || !b.isPresent()) return ResponseEntity.notFound().build();
        ReadingHistory rh = new ReadingHistory(bookId, u.get(), b.get(), ReadingHistory.Status.READ, LocalDateTime.now());
        readingHistoryRepository.save(rh);
        return ResponseEntity.ok(rh);
    }

    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<List<Book>> getRecommendations(@PathVariable Long userId, @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<Book> recs = recommendationService.recommendForUser(userId, limit);
        return ResponseEntity.ok(recs);
    }
}
