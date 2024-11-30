package server;



public class ServerPrincipal extends Thread {

    public void run(){
        new S1(8000, "S1", 24, 56).start();
        new S2(8001, "S2", 943.3,0.2).start();
        new S3(8002, "S3", 15.2, 58).start();
    }
}