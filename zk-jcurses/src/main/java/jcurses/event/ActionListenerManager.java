package jcurses.event;

/**
 * This class implements a listener manager to manage <code>jcurses.event.ActionEvent</code> instances and listener on
 * these. Only possible type of handled events is <code>jcurses.event.ActionEvent<code>,
* of managed listeners id <code>jcurses.event.ActionListener</code>
 */
public class ActionListenerManager extends ListenerManager<ActionEvent, ActionListener> {

    protected void doHandleEvent(ActionEvent event, ActionListener listener) {
        ((ActionListener) listener).actionPerformed((ActionEvent) event);
    }

}