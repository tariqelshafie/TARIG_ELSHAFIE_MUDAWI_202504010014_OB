# TARIG_ELSHAFIE_MUDAWI_202504010014_OB

# Java Object-Oriented Programming Tutorials

A collection of Java programming tutorials and practical exercises developed as part of my **Object-Oriented Programming (OOP)** coursework.

This repository contains several Java exercises covering fundamental programming concepts such as classes, objects, constructors, methods, inheritance, and GUI development.

---

## 📚 Contents

The repository is organized into separate tutorial folders, with each tutorial focusing on different Java programming concepts.

| Tutorial       | Main Content                                            |
| -------------- | ------------------------------------------------------- |
| `tutorial_1`   | Classes, objects, attributes, constructors, and methods |
| `tutorial_2`   | Java class and object exercises                         |
| `tutorial_3`   | Inheritance and relationships between classes           |
| `tutorial_5`   | Object-oriented programming practice and documentation  |
| `tutorial_6`   | Inheritance using employee and lecturer classes         |
| `tutorial_7`   | Abstract classes / inheritance using appliances         |
| `tutolrial_10` | Java GUI quiz application                               |

---

## 🗂️ Project Structure

```text
TARIG_ELSHAFIE_MUDAWI_202504010014_OB/
│
├── tutorial_1/
│   ├── Student.java
│   └── main.java
│
├── tutorial_2/
│   ├── Student.java
│   └── main.java
│
├── tutorial_3/
│   ├── Person.java
│   ├── Student.java
│   ├── Lecturer.java
│   └── Main.java
│
├── tutorial_5/
│   ├── Student.java
│   ├── main.java
│   └── docementation.txt
│
├── tutorial_6/
│   ├── employee.java
│   ├── lecturer.java
│   └── main.java
│
├── tutorial_7/
│   ├── Applience.java
│   ├── Microwave.java
│   ├── WashingMachine.java
│   └── Main.java
│
├── tutolrial_10/
│   ├── Questions.java
│   └── QuizBattleGUI.java
│
├── .gitignore
├── LICENSE
└── README.md
```

> **Note:** The folder `tutolrial_10` intentionally retains the spelling used in the original repository.

---

## 💻 Technologies Used

* **Java**
* Object-Oriented Programming (OOP)
* Java Classes & Objects
* Constructors
* Methods
* Inheritance
* Polymorphism
* GUI Programming
* Git & GitHub

---

## 🧠 Concepts Practiced

### 1. Classes and Objects

The early tutorials introduce the basic structure of Java classes.

For example, `tutorial_1` contains a `Student` class with attributes such as:

* Name
* Age
* GPA

It also includes methods for displaying student information and performing actions such as studying and taking an exam.

```java
Student s1 = new Student("tariq", 20, 3.8);

s1.displayInfo();
s1.study();
s1.takeExsam();
```

---

### 2. Constructors

Constructors are used to initialize objects when they are created.

Example:

```java
Student(String studentName, int studentAge, double studentGpa) {
    name = studentName;
    age = studentAge;
    gpa = studentGpa;
}
```

---

### 3. Methods

The projects demonstrate how methods can be used to define behaviors for objects.

Examples include:

```java
displayInfo()
study()
takeExsam()
```

---

### 4. Inheritance

Later tutorials introduce relationships between classes.

For example, `tutorial_3` contains:

* `Person`
* `Student`
* `Lecturer`
* `Main`

This provides practice with creating related classes and applying object-oriented design concepts.

---

### 5. Employee and Lecturer Classes

`tutorial_6` continues working with object-oriented relationships through:

```text
employee
   │
   └── lecturer
```

The tutorial contains separate classes for employees and lecturers together with a main program for testing them.

---

### 6. Abstract Classes and Appliances

`tutorial_7` works with appliance-related classes:

* `Applience`
* `Microwave`
* `WashingMachine`
* `Main`

This exercise provides practice with designing a parent class and specialized child classes.

---

### 7. Java GUI Programming

The final tutorial in the repository contains a small quiz application.

The `tutolrial_10` folder includes:

```text
Questions.java
QuizBattleGUI.java
```

The project demonstrates how Java can be used to create a graphical user interface for an interactive quiz.

---

## 🚀 How to Run

### Requirements

Install the following:

* Java Development Kit (JDK)
* A Java IDE such as IntelliJ IDEA, Eclipse, or NetBeans

You can also compile and run the programs directly from the command line.

### Clone the Repository

```bash
git clone https://github.com/tariqelshafie/TARIG_ELSHAFIE_MUDAWI_202504010014_OB.git
```

Move into the project directory:

```bash
cd TARIG_ELSHAFIE_MUDAWI_202504010014_OB
```

### Compile a Tutorial

For example:

```bash
cd tutorial_1
javac Student.java main.java
```

Then run the main class:

```bash
java main
```

The exact command may vary depending on the class name and the Java environment being used.

---

## 📖 Learning Objectives

The purpose of this repository is to practice and demonstrate fundamental Java and Object-Oriented Programming concepts.

Through these exercises, I worked with:

* Creating Java classes
* Creating and using objects
* Defining attributes
* Creating constructors
* Creating methods
* Passing values to objects
* Using inheritance
* Designing parent and child classes
* Applying OOP principles
* Building a simple Java GUI
* Organizing Java projects using Git and GitHub

---

## 👨‍💻 Author

**Tariq Elshafie Mudawi**

Student ID: `202504010014`

This repository contains coursework and practical exercises developed during my Java Object-Oriented Programming studies.

---

## 📄 License

This project is licensed under the **MIT License**.

See the [`LICENSE`](LICENSE) file for more information.

---

## 🔗 Repository

The complete source code is available on GitHub:

[**TARIG_ELSHAFIE_MUDAWI_202504010014_OB**](https://github.com/tariqelshafie/TARIG_ELSHAFIE_MUDAWI_202504010014_OB)
