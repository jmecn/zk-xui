package jcurses.event;

/**
 * The interface has to be implemented to listen on instances of
 * <code>ValueChangedEvent</code>
 */
public interface ValueChangedListener extends EventListener {

    /**
     * The method will be called by an widget, generating
     * <code>ValueChangedEvent</code> instances, if the listener has been registered
     * by it.
     * 
     * @param event the event occured
     */
    void valueChanged(ValueChangedEvent event);

}