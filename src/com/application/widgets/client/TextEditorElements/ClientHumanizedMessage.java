package com.application.widgets.client.TextEditorElements;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Class to generate an humanized message Popup in client side
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class ClientHumanizedMessage extends PopupPanel {
    /**
     * Enumeration to define the behavior
     * 
     * @author Miguel Urízar Salinas
     *
     */
    public enum HIDE_BEHAVIOR {
	ON_CLICK, ON_CLICK_FADE, SHOW_AND_FADE, FADE_AFTER_MOUSE_MOVE
    }

    /**
     * Enumeration to define the massage type
     * 
     * @author Miguel Urízar Salinas
     *
     */
    public enum MESSAGE_TYPE {
	INFO, WARNING, ERROR
    }

    private static final String BASE_CSS_CLASS = "clientHumanizedMessage";
    private static final String ERROR_CSS_CLASS = "clientHumanizedMessageError";
    private static final String WARNING_CSS_CLASS = "clientHumanizedMessageWarning";
    private static final String INFO_CSS_CLASS = "clientHumanizedMessageInfo";
    /** Opacity changes over time */
    private static final float DELTA = 0.05f;
    /** Time to make the popup disappear */
    private static final int HIDE_TIMER_DELAY = 50;
    /** Time showing message */
    private static final int SHOW_MESSAGE_DELAY = 1000;
    /** Inital opacity */
    private static final float INITAL_OPACITY = 0.8f;
    /** When the popup hides */
    private final HIDE_BEHAVIOR hideBehavior;

    private HandlerRegistration handlerRegistration;

    /**
     * Constructor for client humanized message
     * 
     * @param message
     *            Text to show inside the humanized message
     * @param messageType
     *            Type of message to show. Can be INFO, WARNING, ERROR.
     * @param hideBehavior
     *            Behaviour the popup has. Can be ON_CLICK, ON_CLICK_FADE, SHOW_AND_FADE, FADE_AFTER_MOUSE_MOVE.
     */
    public ClientHumanizedMessage(String message, MESSAGE_TYPE messageType, HIDE_BEHAVIOR hideBehavior) {
	// Assignments
	this.hideBehavior = hideBehavior;
	boolean autoHide = false;
	boolean animate = false;
	// Make necessary changes depending on behavior
	switch (hideBehavior) {
	case ON_CLICK:
	    autoHide = true;
	    break;
	case SHOW_AND_FADE:
	    animate = false;
	    break;

	default:
	    break;
	}
	// Assignments
	setAutoHideEnabled(autoHide);
	setAnimationEnabled(animate);
	setOpacity(INITAL_OPACITY);
	// Include the defined text
	HTML html = new HTML(message);
	setStylePrimaryName(BASE_CSS_CLASS);
	// Change background depending on the type of message
	switch (messageType) {
	case ERROR:
	    html.addStyleName(ERROR_CSS_CLASS);
	    addStyleName(ERROR_CSS_CLASS);
	    break;
	case WARNING:
	    html.addStyleName(WARNING_CSS_CLASS);
	    addStyleName(WARNING_CSS_CLASS);
	    break;

	default:
	    html.addStyleName(INFO_CSS_CLASS);
	    addStyleName(INFO_CSS_CLASS);
	    break;
	}
	// Anchor the text to the widget
	setWidget(html);
    }

    /**
     * Creates the timer and starts it, making small fades each interval
     * @return Initiated timer
     */
    private Timer getHideTimer() {
	// Remove previous event handlers
	if (handlerRegistration != null) {
	    handlerRegistration.removeHandler();
	    handlerRegistration = null;
	}
	// Create a timer
	final Timer hideTimer = new Timer() {
	    @Override
	    public void run() {
		// Decrease opacity slowly and hides when 0
		float opacity = getOpacity();
		opacity -= DELTA;
		if (opacity > DELTA) {
		    setOpacity(opacity);
		} else {
		    hide();
		    cancel();
		}
	    }
	};
	// return the timer
	return hideTimer;
    }

    /**
     * Shows the humanized message
     */
    @Override
    public void show() {
	// Call parent method
	super.show();
	// Timer for waiting before fading
	Timer waitTimer = null;
	// Depending on thebehavior
	switch (hideBehavior) {
	// Shows and fades afterwards
	case SHOW_AND_FADE:
	    waitTimer = new Timer() {
		@Override
		public void run() {
		    getHideTimer().scheduleRepeating(HIDE_TIMER_DELAY);
		}
	    };
	    break;
	// Shows, waits until on click event and then fades
	case ON_CLICK_FADE:
	    waitTimer = new Timer() {
		@Override
		public void run() {
		    addNativePreviewHandler(Event.ONCLICK);
		}
	    };
	    break;
	// Shows, waits until mouse move event and then fades
	case FADE_AFTER_MOUSE_MOVE:
	    waitTimer = new Timer() {
		@Override
		public void run() {
		    addNativePreviewHandler(Event.ONMOUSEMOVE);
		}
	    };
	    break;
	default:
	    break;
	}
	// We wait the timer for the time showing message
	if (waitTimer != null) {
	    waitTimer.schedule(SHOW_MESSAGE_DELAY);
	}
    }

    /**
     * Implements the handler for the selected event and afterwards begins the fader
     * 
     * @param eventType
     */
    private void addNativePreviewHandler(final int eventType) {
	if (handlerRegistration == null) {
	    handlerRegistration = Event.addNativePreviewHandler(new NativePreviewHandler() {
		@Override
		public void onPreviewNativeEvent(NativePreviewEvent event) {
		    int type = event.getTypeInt();
		    // If is the selected event we start to fade
		    if (type == eventType)
			getHideTimer().scheduleRepeating(HIDE_TIMER_DELAY);
		}
	    });
	}
    }

    /**
     * Setter for the opacity of the Humanized message
     * 
     * @param opacity
     *            New opacity value, from 0 to 1
     */
    private void setOpacity(double opacity) {
	if (opacity < 0) {
	    opacity = 0;
	}

	getElement().getStyle().setOpacity(opacity);
    }

    /**
     * Getter for the opacity of the Humanized message
     * 
     * @return Actual opacity of the Humanized message
     */
    private float getOpacity() {
	String opacityString = getElement().getStyle().getOpacity();

	try {
	    float opacity = Float.parseFloat(opacityString);
	    return opacity;
	} catch (NumberFormatException nfe) {
	    throw new RuntimeException("could not parse opacity, expected float but got '" + opacityString + "'");
	}

    }

    /**
     * Show a message for {@link ClientHumanizedMessage#SHOW_MESSAGE_DELAY} milliseconds and hide it when the user clicks outside of it
     * 
     * @param message
     *            the message
     * @param messageType
     *            the message type
     */
    public static void showMessageUntilClick(String message, MESSAGE_TYPE messageType) {
	ClientHumanizedMessage humanizedMessagePopup = new ClientHumanizedMessage(message, messageType, HIDE_BEHAVIOR.ON_CLICK);
	humanizedMessagePopup.center();
    }

    /**
     * Show a message for {@link ClientHumanizedMessage#SHOW_MESSAGE_DELAY} milliseconds and fade it out when the user clicks outside of it
     * 
     * @param message
     *            the message
     * @param messageType
     *            the message type
     */
    public static void showMessageAndFadeAfterClick(String message, MESSAGE_TYPE messageType) {
	ClientHumanizedMessage humanizedMessagePopup = new ClientHumanizedMessage(message, messageType, HIDE_BEHAVIOR.ON_CLICK_FADE);
	humanizedMessagePopup.center();
    }

    /**
     * Show a message for {@link ClientHumanizedMessage#SHOW_MESSAGE_DELAY} milliseconds and fade it after first mouse move
     * 
     * @param message
     *            the message
     * @param messageType
     *            the message type
     */
    public static void showMessageAndFadeAfterMouseMove(String message, MESSAGE_TYPE messageType) {
	ClientHumanizedMessage humanizedMessagePopup = new ClientHumanizedMessage(message, messageType, HIDE_BEHAVIOR.FADE_AFTER_MOUSE_MOVE);
	humanizedMessagePopup.center();
    }

    /**
     * Show a message for {@link ClientHumanizedMessage#SHOW_MESSAGE_DELAY} milliseconds and fade it out afterwards
     * 
     * @param message
     *            the message
     * @param messageType
     *            the message type
     */
    public static void showMessageAndFade(String message, MESSAGE_TYPE messageType) {
	ClientHumanizedMessage humanizedMessagePopup = new ClientHumanizedMessage(message, messageType, HIDE_BEHAVIOR.SHOW_AND_FADE);
	humanizedMessagePopup.center();
    }

}