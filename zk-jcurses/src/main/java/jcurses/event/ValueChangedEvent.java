package jcurses.event;

import jcurses.widgets.Widget;

/**
 * Instances of this class are generated by widgets, that manage a value, an
 * example is the textfield widget. Such events are generated, if an user has
 * modified the value assosiated with the widget.
 */
public class ValueChangedEvent extends Event {

    public ValueChangedEvent(Widget source) {
        super(source);
    }

}