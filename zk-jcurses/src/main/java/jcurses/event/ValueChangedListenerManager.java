package jcurses.event;

/**
 * This class implements a listener manager to manage
 * <code>jcurses.event.ValueChangedEvent</code> instances and listener on these.
 * Only possible type of handled events is
 * <code>jcurses.event.ValueChangedEvent<code>,
* of managed listeners id <code>jcurses.event.ValueChangedListener</code>
 */
public class ValueChangedListenerManager extends ListenerManager<ValueChangedEvent, ValueChangedListener> {

    protected void doHandleEvent(ValueChangedEvent event, ValueChangedListener listener) {
        listener.valueChanged(event);
    }

}