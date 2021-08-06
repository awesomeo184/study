package Chapter2.item1;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Student  {
    private String id;
    private String name;


    public static Student withId(String id) {
        Student student = new Student();
        student.id = id;
        return student;
    }

    public static Student withName(String name) {
        Student student = new Student();
        student.name = name;
        return student;
    }

    public static void main(String[] args) {
    }
}
