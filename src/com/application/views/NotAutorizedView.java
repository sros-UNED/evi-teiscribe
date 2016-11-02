package com.application.views;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.application.language.Labels;

import static com.application.components.Constants.LOGINLOGOIMAGE;

/**
 * Vaadin View for not authorized user
 * 
 * @author Miguel Urízar Salinas
 */
public class NotAutorizedView extends CustomComponent implements View {
    private static final long serialVersionUID = -8557214981207424713L;

    /**
     * Constructor for he not authorized view.
     * <p>
     * Creates a view for not authorized users
     * </p>
     */
    public NotAutorizedView() {

	// Make the view full size
	//setSizeFull();
	// Add the main layout
	VerticalLayout mainLayout = new VerticalLayout();

	// Add the image and form
	addImage(mainLayout);
	addMessage(mainLayout);

	// Make the min layout full size
	//mainLayout.setSizeFull();

	// Generate global composition
	setCompositionRoot(mainLayout);
    }

    /**
     * Adds the login logo image to the view
     * 
     * @param mainLayout
     *            Layout where to insert the image
     */
    private void addImage(AbstractOrderedLayout mainLayout) {
	// Create image of the icon
	Image enterpriseIcon = new Image(null, new ThemeResource(LOGINLOGOIMAGE));
	// Set the width of the object
	enterpriseIcon.setWidth(300, Sizeable.Unit.PIXELS);
	// Add the object and put it in the middle
	mainLayout.addComponent(enterpriseIcon);
	mainLayout.setComponentAlignment(enterpriseIcon, Alignment.TOP_CENTER);
	//mainLayout.setExpandRatio(enterpriseIcon, 0);
    }

    /**
     * Adds the message to the view
     * 
     * @param mainLayout
     */
    private void addMessage(VerticalLayout mainLayout) {

	// Add label for showing error
	Label info = new Label(Labels.getString("optimizedBrowser"));
	info.setWidthUndefined();
	insertComponentInLayer(mainLayout,info, Alignment.MIDDLE_CENTER);
	
	Label content = new Label(Labels.getString("notAuthorized"));
	content.setWidthUndefined();
	content.setPrimaryStyleName("access_denied_label");
	insertComponentInLayer(mainLayout,content, Alignment.TOP_CENTER);
    }

    @Override
    public void enter(ViewChangeEvent event) {
    }
    
    /**
     * S8upport function to add elements with an alignment
     * 
     * @param layer
     *            Layer to insert the element
     * @param element
     *            Component to insert in the layer
     * @param a
     *            Alignment of the component
     */
    public void insertComponentInLayer(AbstractOrderedLayout layer, Component element, Alignment a) {
	layer.addComponent(element);
	if (a != null) {
	    layer.setComponentAlignment(element, a);
	}
    }
}
