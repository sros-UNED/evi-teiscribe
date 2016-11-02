package com.application.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;

import com.application.language.Labels;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import static com.application.components.Constants.MAXUPLOADSIZE;

/**
 * Class for uploading files.
 * <p>
 * Includes a progress bar and a percentage
 * </p>
 *
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class FileUploader implements Serializable {
    private static final long serialVersionUID = -3400537175371589916L;

    File tempFile;
    boolean bigFile;

    /**
     * Function that shows the upload window.
     * <p>
     * It creates a new window to select the file to upload. If the file is too big the upload stops. Shows errors when the upload fails (max file error or
     * impossible to upload file). If the upload is successful, it calls the {@link com.application.components.FileUploaded} function.
     * </p>
     * 
     * @param header
     *            Caption of the new window to open.
     * @param iconName
     *            Path of the icon to show in the new window
     * @param fileProcessor
     *            class with the function to call when successfully uploaded the file
     */
    public FileUploader(String header, String iconName, final FileUploaded fileProcessor) {

	final Upload upload;
	final Window uploadWindow;

	// Make the receiver for files (is the thread that saves the file)
	Receiver receiver = new Receiver() {
	    private static final long serialVersionUID = 8873039059696761328L;

	    @Override
	    public OutputStream receiveUpload(String filename, String mimeType) {
		try {
		    // Generate a temporary file
		    tempFile = File.createTempFile("newXML", ".xml");
		    return new FileOutputStream(tempFile);
		} catch (IOException e) {
		    return null;
		}
	    }
	};

	// Create the uploader that includes the interface and processes
	upload = new Upload(null, receiver);
	upload.setButtonCaption(Labels.getString("Upload"));

	// Create the upload layout
	final VerticalLayout windowLayout = new VerticalLayout();
	windowLayout.setMargin(true);
	windowLayout.addComponent(upload);

	// Create progressBar
	final ProgressBar progressBar = new ProgressBar(0.0f);
	progressBar.setCaption("0%");
	progressBar.setWidth("100%");

	// Create a sub-window and set the content
	uploadWindow = new Window(header);
	// Don't allow to interact with background
	uploadWindow.setModal(true);
	uploadWindow.setContent(windowLayout);
	// Center window in the browser
	uploadWindow.center();
	// Set icon
	if ((iconName != null) && !(iconName.isEmpty()))
	    uploadWindow.setIcon(new ThemeResource(iconName));
	// Adjust size
	uploadWindow.setSizeUndefined();
	// Don't allow to resize
	uploadWindow.setResizable(false);

	// Listener for showing the progress bar
	upload.addStartedListener(new StartedListener() {
	    private static final long serialVersionUID = -5295453569799277469L;

	    @Override
	    public void uploadStarted(StartedEvent event) {
		// If the file to upload is too big we cancel it
		if (upload.getUploadSize() > MAXUPLOADSIZE) {
		    upload.interruptUpload();
		    bigFile = true;
		} else {
		    bigFile = false;
		}
		windowLayout.addComponent(progressBar);
	    }
	});

	// Listener for updating the progress bar
	upload.addProgressListener(new ProgressListener() {
	    private static final long serialVersionUID = 7282164037299773477L;

	    @Override
	    public void updateProgress(long readBytes, long contentLength) {
		progressBar.setValue((float) readBytes / (float) contentLength);
		progressBar.setCaption(Long.toString((readBytes * 100 / contentLength)) + "%");
	    }
	});

	// Add listener when error uploading
	upload.addFailedListener(new FailedListener() {
	    private static final long serialVersionUID = -4778153632413653685L;

	    @Override
	    public void uploadFailed(FailedEvent event) {
		// Inform there was an error uploading
		if (tempFile != null)
		    tempFile.delete();
		windowLayout.removeComponent(progressBar);
		if (bigFile == true)
		    Notification.show(Labels.getString("error"),
			    Labels.getString("UploadFileTooBig") + " " + new DecimalFormat("##0.0").format((double) MAXUPLOADSIZE / (double) (1024 * 1024))
				    + " " + Labels.getString("MegaBytes") + ".", Notification.Type.ERROR_MESSAGE);
		else
		    Notification.show(Labels.getString("error"), Labels.getString("UploadFileFailed") + " \"" + event.getFilename() + "\".",
			    Notification.Type.ERROR_MESSAGE);
		// There is a problem when something fails, uploading afterwards will fail
		UI.getCurrent().removeWindow(uploadWindow);
	    }
	});

	// Add listener when a file is uploaded
	upload.addSucceededListener(new SucceededListener() {
	    private static final long serialVersionUID = -3678102753399602626L;

	    @Override
	    public void uploadSucceeded(SucceededEvent finishedEvent) {
		if (finishedEvent.getFilename().isEmpty()) {
		    Notification.show(Labels.getString("error"), Labels.getString("UploadNoFile"), Notification.Type.ERROR_MESSAGE);
		    windowLayout.removeComponent(progressBar);
		    return;
		}
		// remove the window
		UI.getCurrent().removeWindow(uploadWindow);
		fileProcessor.FileUploadedWork(tempFile, finishedEvent.getFilename());
		return;
	    }
	});

	// Open window in the UI
	UI.getCurrent().addWindow(uploadWindow);
    }
}
