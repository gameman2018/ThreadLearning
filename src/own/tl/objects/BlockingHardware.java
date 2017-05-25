package own.tl.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by 挨踢狗 on 2017/5/24.
 */
public class BlockingHardware {
    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private volatile boolean on = false;
    private volatile boolean started = false;
    private FakeHardware hardware;
    private List<BlockingHardwareListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public BlockingHardware(String name){
        hardware = new FakeHardware(name);
        hardware.addListenner(new FakeHardwareListener() {
            @Override
            public void event(FakeHardware source, FakeHardware.FakeHardwareEvent event) {
                handleHardwareEvent(source, event);
            }
        });
    }

    protected void handleHardwareEvent(FakeHardware source, FakeHardware.FakeHardwareEvent event){
        boolean wasStarted = started;
        lock.lock();
        try {
            if (event == FakeHardware.FakeHardwareEvent.ON){
                on = true;
            }else if (event == FakeHardware.FakeHardwareEvent.OFF){
                on = false;
            }else if (event == FakeHardware.FakeHardwareEvent.START){
                started = true;
            }else if (event == FakeHardware.FakeHardwareEvent.STOP){
                started = false;
            }
            cond.signalAll();
        }finally {
            lock.unlock();
        }
        if (wasStarted && !started){
            fireTaskFinished();
        }
    }
    public void start(int ms, int slices){
        lock.lock();
        try {
            hardware.start(ms,slices);
            while (!started){
                cond.await();
            }
            System.out.println("It's Started");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    public void stop(){
        lock.lock();
        try {
            hardware.stop();
            while (started){
                cond.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    public void turnOn(){
        lock.lock();
        try {
            hardware.turnOn();
            while (!on){
                cond.await();
            }
            System.out.println("Turned on");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    public void turnOff(){
        lock.lock();
        try {
            hardware.turnOff();
            while (on){
                cond.await();
            }
            System.out.println("Turned off");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    private void fireTaskFinished(){
        synchronized (listeners){
            for (BlockingHardwareListener listener : listeners){
                listener.taskFinished();
            }
        }
    }
    public boolean addListener(BlockingHardwareListener listener){
        return listeners.add(listener);
    }

}
