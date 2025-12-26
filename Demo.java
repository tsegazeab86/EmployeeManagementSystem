public class Demo extends Thread {
    public static void main(String[] args) {
        Demo t1 = new Demo();
        Demo t2 = new Demo();
         System.out.println("the thread promer" + t1.getPriority());
        System.out.println("the thread promer" + t2.getPriority());
             t1.setPriority(4);
        System.out.println("my "+t1.getPriority());
  t2.setPriority(6);
        System.out.println("my "+t2.getPriority());
        System.out.print(Thread.currentThread().getName());
        System.out.println("The thread priority of main thread is : " + Thread.currentThread().getPriority());
        Thread.currentThread().setPriority(10);
        System.out.println("The thread priority of main thread is : " +
                Thread.currentThread().getPriority());
    }
}
