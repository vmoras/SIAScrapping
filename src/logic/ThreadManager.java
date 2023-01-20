package logic;

public class ThreadManager extends Thread{
    public String nameDegree;

    public ThreadManager(String nameDegree){
        this.nameDegree = nameDegree;
    }

    @Override
    public void run(){
        AutoSIA autoSearch = new AutoSIA(this.nameDegree);
        autoSearch.getInfo();
    }
}