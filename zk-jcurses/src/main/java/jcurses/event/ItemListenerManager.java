package jcurses.event;

/**
 * This class implements a listener manager to manage
 * <code>jcurses.event.ItemEvent</code> instances and listener on these. Only
 * possible type of handled events is <code>jcurses.event.ItemEvent<code>,
* of managed listeners id <code>jcurses.event.ItemListener</code>
 */
public class ItemListenerManager extends ListenerManager<ItemEvent, ItemListener> {

    protected void doHandleEvent(ItemEvent event, ItemListener listener) {
        listener.stateChanged(event);
    }

}