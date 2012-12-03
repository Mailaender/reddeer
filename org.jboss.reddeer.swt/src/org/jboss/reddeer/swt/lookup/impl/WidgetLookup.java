package org.jboss.reddeer.swt.lookup.impl;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.finders.ControlFinder;
import org.hamcrest.Matcher;
import org.jboss.reddeer.swt.exception.SWTLayerException;
import org.jboss.reddeer.swt.util.Display;
import org.jboss.reddeer.swt.util.ObjectUtil;
import org.jboss.reddeer.swt.util.ResultRunnable;

/**
 * Widget Lookup methods
 * @author Jiri Peterka
 *
 */
public class WidgetLookup {
	
	private static WidgetLookup instance = null;
	
	private WidgetLookup() {
	}
	
	public static WidgetLookup getInstance() {
		if (instance == null) instance = new WidgetLookup();
		return instance;
	}
	
	/**
	 * Checks if widget is enabled
	 * @param widget
	 * @return
	 */
	public boolean isEnabled(Widget widget) {
		boolean ret = true;
		Object o = null;
		try {
			o = ObjectUtil.invokeMethod(widget, "isEnabled");
		} catch (RuntimeException e) {
			return true;
		}
		if (o == null) return ret;
		if (o instanceof Boolean) {
			ret = ((Boolean)o).booleanValue();
		}
		return ret;
	}
	
	/**
	 * Send click notification to a widget
	 * @param widget
	 */
	public void sendClickNotifications(Widget widget) {
		notify(SWT.MouseEnter, widget);
		notify(SWT.MouseMove, widget);
		notify(SWT.Activate, widget);
		notify(SWT.MouseDown, widget);
		notify(SWT.MouseUp, widget);
		notify(SWT.Selection, widget);
		notify(SWT.MouseHover, widget);
		notify(SWT.MouseMove, widget);
		notify(SWT.MouseExit, widget);
		notify(SWT.Deactivate, widget);
		notify(SWT.FocusOut, widget);
	}

	private void notify(int eventType, Widget widget) {
		Event event = createEvent(widget);
		notify(eventType, event, widget);
		
	}

	private Event createEvent(Widget widget) {
		Event event = new Event();
		event.time = (int) System.currentTimeMillis();
		event.widget = widget;
		event.display = Display.getDisplay();
		return event;
	}
	
	private void notify(final int eventType, final Event createEvent, final Widget widget) {
		createEvent.type = eventType;
		
		Display.syncExec(new Runnable() {
			public void run() {
				if ((widget == null) || widget.isDisposed()) {
					return;
				}
				if (!WidgetLookup.getInstance().isEnabled(widget)) {
					// do nothing here (it may be expected state (e.g Clear Console))
				}
				
				widget.notifyListeners(eventType, createEvent);
			}
		});

		// Wait for synchronization
		Display.syncExec(new Runnable() {
			public void run() {
				// do nothing here
			}
		});
	}
	
	public Widget activeShellWidget(Matcher<? extends Widget> matcher, int index) {
		List<? extends Widget> widgets = activeWidgets(new ShellLookup().getActiveShell(), matcher);
		return getProperWidget(widgets, index);
	}

	public Widget activeViewWidget(Matcher<? extends Widget> matcher, int index) {
		List<? extends Widget> widgets = activeWidgets(getFocusControl(), matcher);
		return getProperWidget(widgets, index);
	}
	
	public Widget activeWidget(Matcher<? extends Widget> matcher, Control activeControl, int index) {
		List<? extends Widget> widgets = activeWidgets(activeControl, matcher);
		return getProperWidget(widgets, index);
	}
	
	private Widget getProperWidget(List<? extends Widget> widgets, int index) {
		if (widgets.size() - index < 1) {
			throw new SWTLayerException("No matching widget found");
		}
		return widgets.get(index);
	}
	
	private List<? extends Widget> activeWidgets(Control activeControl, Matcher<? extends Widget> matcher) {
		ControlFinder finder = new ControlFinder();
		List<? extends Widget> widgets = finder.findControls(activeControl, matcher, true);
		return widgets;
	}

	/**
	 * Return control with focus
	 * 
	 * @return control with focus
	 */
	public Control getFocusControl() {
		Control c = Display.syncExec(new ResultRunnable<Control>() {

			@Override
			public Control run() {
				Control focusControl = Display.getDisplay().getFocusControl();
				return focusControl;
			}

		});
		return c;
	}

}
