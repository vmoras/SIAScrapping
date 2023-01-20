package logic;

public class ThreadCaller {
    public static void initThreads(){
        // Degrees to be called
        String[] degrees = {"2541 INGENIERÍA AGRÍCOLA",
                "2542 INGENIERÍA CIVIL",
                "2879 INGENIERÍA DE SISTEMAS Y COMPUTACIÓN",
                "2545 INGENIERÍA ELECTRÓNICA",
                "2546 INGENIERÍA INDUSTRIAL",
                "2547 INGENIERÍA MECÁNICA",
                "2549 INGENIERÍA QUÍMICA",
                "2548 INGENIERÍA MECATRÓNICA"};

        // Create a thread for each one of the degrees
        Thread[] threads = new Thread[100];
        for (int i = 0; i < degrees.length; i++){
            threads[i] = new Thread(new ThreadManager(degrees[i]));
            threads[i].start();
        }
        for (int i = 0; i < degrees.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}