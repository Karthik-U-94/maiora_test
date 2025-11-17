# Library Management System (Spring Boot)

This is a minimal Spring Boot application that exposes REST APIs for managing books and users, plus a simple book recommendation feature.

Features implemented:
- Add, list, update, delete books
- Retrieve books by author or category
- Ensure ISBN uniqueness
- Create users and mark books as read or add them to reading list
- Simple recommendation algorithm based on reading history and popular books

Run (requires Maven and Java 11+):

```powershell
mvn package
mvn spring-boot:run
```

The app uses an in-memory H2 database. H2 console is available at http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:librarydb).

Example endpoints:
- POST /api/books - add a book
- GET /api/books - list books (query params: author, category)
- PUT /api/books/{id} - update book
- DELETE /api/books/{id} - delete book
- POST /api/users - create user
- POST /api/users/{userId}/reading-list/{bookId} - add to reading list
- POST /api/users/{userId}/mark-read/{bookId} - mark book as read
- GET /api/users/{userId}/recommendations - get personalized recommendations
