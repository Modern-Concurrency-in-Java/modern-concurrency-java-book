# Modern Concurrency in Java

Source code repository for the book "Modern Concurrency in Java" by A N M Bazlur Rahman.

## Book Links

- [O'Reilly Learning Platform](https://learning.oreilly.com/library/view/modern-concurrency-in/9781098165406/)
- [O'Reilly Library](https://www.oreilly.com/library/view/modern-concurrency-in/9781098165406/)
- [GitHub Repository](https://github.com/ModernConcurrency-in-Java)

## About

This repository contains all the code examples from the book, organized by chapter. Each chapter folder includes practical examples demonstrating modern Java concurrency features including:

- Virtual Threads
- Structured Concurrency
- Scoped Values
- Reactive Programming with Project Reactor
- Thread pools and executors
- Modern concurrency patterns and best practices

## Structure

```
src/ca/bazlur/modern/concurrency/
├── c01/  # Chapter 1 examples
├── c02/  # Chapter 2 examples
├── c03/  # Chapter 3 examples
├── c04/  # Chapter 4 examples
├── c05/  # Chapter 5 examples
├── c06/  # Chapter 6 examples
└── ...
```

## Requirements

- Java 25 (early-access) with preview features enabled
- Maven 3.9+

## Build & Run with Maven

The Maven build uses the standard layout (`src/main/java`) and enables Java preview features automatically.

- Compile everything: `mvn -DskipTests compile`
- Run an example: `mvn -Dexec.mainClass=ca.bazlur.modern.concurrency.c01.HelloWorld exec:java`
  - Add `-Dexec.args="arg1 arg2"` if the sample accepts arguments.

## Running the Examples

Each example class contains a `main` method that can be run independently. Navigate to the specific chapter folder and run the desired example.

## License

This book's example code is offered for use in your programs and documentation. You do not need to contact us for permission unless you're reproducing a significant portion of the code. For example:

- ✅ Writing a program that uses several chunks of code from this book does not require permission
- ✅ Answering a question by citing this book and quoting example code does not require permission
- ❌ Selling or distributing examples from O'Reilly books does require permission
- ❌ Incorporating a significant amount of example code from this book into your product's documentation does require permission

We appreciate, but generally do not require, attribution. An attribution usually includes the title, author, publisher, and ISBN:

**"Modern Concurrency in Java by A N M Bazlur Rahman (O'Reilly). Copyright 2025 A N M Bazlur Rahman, 978-1-098-16541-3."**

If you feel your use of code examples falls outside fair use or the permission given above, feel free to contact O'Reilly at permissions@oreilly.com.

For technical questions or problems using the code examples, please send an email to support@oreilly.com.

## Author

A N M Bazlur Rahman

## Support

For questions and discussions about the code examples, please refer to the book's official resources on the O'Reilly platform.
