# LibraryAPI

## Overview
LibraryAPI is a Spring Boot application that provides a RESTful API for managing a library. The API allows users to perform CRUD operations on books and authors. The API also provides endpoints for searching books by title, author, and genre.

## Prerequisites
- Java 23


mvn clean install

## Access the Application:
Once the application is running, you can access the API at
http://localhost:8080.

## API Endpoints
The following endpoints are available:

- GET /books: Get all books
- GET /books/{id}: Get a book by ID
- POST /books: Create a new book
- PUT /books/{id}: Update a book
- DELETE /books/{id}: Delete a book by ID

