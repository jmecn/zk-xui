package jcurses.event;

/**
 * This class implements a listener manager to manage
 * <code>jcurses.event.ValueChangedEvent</code> instances and listener on these.
 * Only possible type of handled events is
 * <code>jcurses.event.ValueChangedEvent<code>,
* of managed listeners id <code>jcurses.event.ValueChangedListener</code>
 */
public class WindowListenerManager extends ListenerManager<WindowEvent, WindowListener> {

    protected void doHandleEvent(WindowEvent event, WindowListener listener) {
        listener.windowChanged(event);
    }
}