package own.tl.objects;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by 挨踢狗 on 2017/5/25.
 */
public class OnShotEvent implements Runnable{
    public enum State{
        WAITING,
        RUNNING
    }

    public enum Event{
        FIRE,
        DONE
    }

    private int ms;
    private int slices;
    private LinkedBlockingQueue<Event> queue = new LinkedBlockingQueue<>();
    private List<BlockingHardwareListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private State currentState = State.WAITING;
    private Thread consumer;
    private boolean started = false;
    private BlockingHardware hardware;

    public OnShotEvent(int ms, int slices){
        this.ms = ms;
        this.slices = slices;
    }

    public void initialize(){
        hardware = new BlockingHardware("Name");
        hardware.addListener(getListener());
        consumer = new Thread(this);
        consumer.start();
    }

    private BlockingHardwareListener getListener(){
        return new BlockingHardwareListener() {
            @Override
            public void taskFinished() {
                try {
                    queue.put(Event.DONE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void fire(){
        try {
            queue.put(Event.FIRE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void done(){
        try {
            queue.put(Event.DONE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void shutDown(){
        Thread tmp = consumer;
        consumer = null;
        try {
            tmp.join(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void processEvent(Event event){
        if (State.WAITING == currentState){
            if (event == Event.FIRE){
                hardware.start(ms,slices);
                hardware.turnOn();
                currentState = State.RUNNING;
            }
        }else if (State.RUNNING == currentState){
            if (event == Event.DONE){
                hardware.turnOff();
                hardware.stop();
                currentState = State.WAITING;
            }
        }
    }
    @Override
    public void run() {
        while (Thread.currentThread() == consumer){
            try {
                processEvent(queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
