package own.tl.objects;

import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by 挨踢狗 on 2017/5/24.
 */
public class BlockingQueue {
    class Producer implements Callable<Void>{
        private Random random = new Random();
        private int numberOfMsgs;
        private int sleep;
        public Producer(int numberOfMsgs, int sleep){
            this.numberOfMsgs = numberOfMsgs;
            this.sleep = sleep;
        }
        @Override
        public Void call() throws Exception {
            Message[] messages = Message.values();
            for (int i = 0; i < numberOfMsgs; i++){
                try{
                    int index = random.nextInt(messages.length - 2);
                    queue.put(messages[index]);
                    System.out.println("PUT(" + (i+1) + ") " + messages[index]);
                    sleep(sleep);
                }catch (InterruptedException ex){
                    ex.printStackTrace();
                }
            }
            queue.put(messages[messages.length - 1]);
            return null;
        }
    }
    class Consumer implements Callable<Integer>{
        private int messageCount = 0;
        @Override
        public Integer call() throws Exception {
            while (true){
                Message msg = queue.take();
                messageCount++;
                System.out.println("Received: " + msg);
                if (msg == Message.POISON_PILL){
                    break;
                }
            }
            return new Integer(messageCount);
        }
    }

    enum Message{
        MSG_ONE,
        MSG_TWO,
        MSG_THREE,
        POISON_PILL
    }

    private ExecutorService pool;
    private LinkedBlockingQueue<Message> queue;

    public BlockingQueue(){
        pool = Executors.newCachedThreadPool();
        queue = new LinkedBlockingQueue<Message>();
        runTask();
    }

    public void runTask(){
        int numberOfMsgs = 100;
        int sleep = 100;
        pool.submit(new Producer(numberOfMsgs,sleep));
        sleep(2000);
        try {
            Future<Integer> consumer = pool.submit(new Consumer());
            System.out.println("Messages Processed:" + consumer.get());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pool.shutdown();
                pool.awaitTermination(10,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    protected void sleep(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
