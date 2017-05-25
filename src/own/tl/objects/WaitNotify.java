package own.tl.objects;

/**
 * Created by 挨踢狗 on 2017/5/24.
 */
public class WaitNotify implements FakeHardwareListener {

    private final WaitNotify waitNotify;
    public WaitNotify() throws Exception {
        waitNotify = this;
        runTest();
    }
    private void runTest() throws Exception{
        FakeHardware hardware = new FakeHardware("g");
        hardware.addListenner(this);
        synchronized (waitNotify){
            hardware.turnOn();
            while (!hardware.isOn()){
                wait();
            }
        }
        System.out.println("Hardware is on");
        synchronized (waitNotify){
            hardware.start(1000,4);
            while (!hardware.isRunning()){
                wait();
            }
        }
        System.out.println("Hardware is running");
        synchronized (waitNotify){
            while (hardware.isRunning()){
                wait();
            }
        }
        System.out.println("Hardware has stopped");
        synchronized (waitNotify){
            hardware.turnOff();
            while (hardware.isOn()){
                wait();
            }
        }

    }
    @Override
    public void event(FakeHardware source, FakeHardware.FakeHardwareEvent event) {
        System.out.println("Got Event: " + event);
        synchronized (waitNotify){
            notifyAll();
        }
    }
}
