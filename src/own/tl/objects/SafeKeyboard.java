package own.tl.objects;

import java.awt.*;
import java.awt.event.KeyEvent;

import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by 挨踢狗 on 2017/5/25.
 */
public class SafeKeyboard implements KeyListener {
    enum State{
        TYPED,
        PRESSED,
        RELEASED
    }
    private class Event{
        KeyEvent event;
        State state;
        Event(KeyEvent event, State state){
            this.event = event;
            this.state = state;
        }
    }
    private int[] polled;
    private Event event;
    private LinkedList<Event> gamethread;
    private LinkedList<Event> innerthread;

    public SafeKeyboard(){
        polled = new int[256];
        gamethread = new LinkedList<>();
        innerthread = new LinkedList<>();
    }

    public boolean keyDownOnce(int keyCode){
        return event.event.getKeyCode() == keyCode && polled[keyCode] == 1;
    }

    public boolean keyDown(int keyCode){
        return event.event.getKeyCode() == keyCode && polled[keyCode] > 0;
    }

    public boolean processKey(){
        if (gamethread.isEmpty()){
            return false;
        }else {
            event = gamethread.poll();
            switch (event.state){
                case TYPED:
                    break;
                case PRESSED:
                    polled[event.event.getKeyCode()]++;
                    break;
                case RELEASED:
                    polled[event.event.getKeyCode()] = 0;
                    break;
            }
        }
        return true;
    }

    public Character getChar(){
        if (event == null){
            return null;
        }
        return event.event.getKeyChar();
    }
    public void poll(){
        LinkedList<Event> tmp = innerthread;
        innerthread = gamethread;
        gamethread = tmp;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        innerthread.add(new Event(e,State.TYPED));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        innerthread.add(new Event(e,State.PRESSED));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        innerthread.add(new Event(e,State.RELEASED));
    }
}
