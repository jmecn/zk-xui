package jcurses.event;

import jcurses.widgets.Widget;

/**
 * This is the basic class for all events, that are generated by jcurses
 * widgets.
 */
public class Event {

    Widget source = null;

    /**
     * The constructor
     * 
     * @param source the widgets, that has generated this event
     */
    public Event(Widget source) {
        this.source = source;
    }

    /**
     * The method returns the widget, that has generated this event
     * 
     * @return the widget, that has generated this event
     */
    public Widget getSource() {
        return source;
    }
}