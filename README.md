# LibraryAPI

## Overview
LibraryAPI is a Spring Boot application that provides a RESTful API for managing a library. The API allows users to perform CRUD operations on books and authors. The API also provides endpoints for searching books by title, author, and genre.

## Prerequisites
- Java 23
- Maven

## Setup
Build the Project:
```sh
mvn clean install
```
Run the Application:
 ```sh
mvn spring-boot:run
   ``` 
Run the Tests:
```sh
mvn test
```

## Access the Application:
Once the application is running, you can access the API at
http://localhost:8080

## API Endpoints
The following endpoints are available:

- **Books**
- `GET /bookse`: Get all books
- `GET /books/{id}`: Get a book by ID
- `POST /books`: Create a new book
- `PUT /books/{id}`: Update a book
- `DELETE /books/{id}`: Delete a book by ID

- **Authors**
    - `GET /authors`: Get all authors
    - `GET /authors/{id}`: Get an author by ID
    - `POST /authors`: Create a new author
    - `PUT /authors/{id}`: Update an author
    - `DELETE /authors/{id}`: Delete an author by ID

## License
This project is licensed under the MIT License.

