package logic;

import data.Course;

import java.io.IOException;
import java.util.HashMap;

public class App {
    public static void main(String[] args) throws IOException {

        long start = System.nanoTime();

        // Start threads
        ThreadCaller.initThreads();
        HashMap<String, Course> info = AutoSIA.getCourses();

        long result = (System.nanoTime() - start) / 1_000_000_000;
        System.out.println(result + " seg / " + result / 60.0 + " min");
    }
}