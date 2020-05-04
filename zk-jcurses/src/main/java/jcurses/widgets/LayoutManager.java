package jcurses.widgets;

/**
 * This interface must be implemented by layout managers, that layout widgets within widget containers.
 */
public interface LayoutManager {

    /**
     * The method layouts a widget within a container, dependent of layout constraints.
     * 
     * @param child widget to layout
     * @param constraints layout constraints
     */
    void layout(Widget child, Object constraints);

    /**
     * The method is called by framework by binding the layout manager to an container. The implementing class must
     * register, whether this is already bound or not, and throw an exception by a try to bind it second time. This is
     * because the framework restricts, that a layout manager can be bound only to one container at a time.
     * 
     * @param container container to bind
     */
    void bindToContainer(WidgetContainer container);

    /**
     * The method is called by framework by unbinding the layout manager from its container. The implementing class must
     * register, whether this is already bound or not, and throw an exception by a try to bind it second time. This is
     * because the framework restricts, that a layout manager can be bound only to one container at a time.
     *
     */
    void unbindFromContainer();

}