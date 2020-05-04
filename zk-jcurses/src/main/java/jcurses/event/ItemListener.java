package jcurses.event;

/**
 * The interface has to be implemented to listen on instances of <code>ItemEvent</code>
 */
public interface ItemListener extends EventListener {

    /**
     * The method will be called by an widget, generating <code>ItemEvent</code> instances, if the listener has been
     * registered by it.
     * 
     * @param event the event occured
     */
    void stateChanged(ItemEvent event);

}