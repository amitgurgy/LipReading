package edu.lipreading.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.JPanel;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.VideoInputFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.lipreading.Utils;
import edu.lipreading.vision.ColoredStickersVideoExtractor;

public class VideoCapturePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String videoInput = "";
	protected BufferedImage image = null;
	protected VideoCanvas canvas;
	protected FrameGrabber grabber = null;
	protected String sampleName;
	protected ColoredStickersVideoExtractor stickersExt;
	protected Thread videoGrabber;
	protected Boolean threadStop;
	
	/**
	 * Create the panel.
	 * @throws com.googlecode.javacv.FrameGrabber.Exception 
	 */
	public VideoCapturePanel() {
		stickersExt = new ColoredStickersVideoExtractor();
		canvas = new VideoCanvas();
		canvas.setBackground(Color.LIGHT_GRAY);
        
		canvas.setBounds(129, 10, 456, 362);
		this.add(canvas);
        canvas.setVisible(true);
        canvas.createBufferStrategy(1);

	}

	public void startVideo() {
		try {
        	if (grabber == null)
        	{
        		grabber = getGrabber(videoInput);
        	}
    		grabber.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//TODO Improve
			try {
				grabber.stop();
			} catch (com.googlecode.javacv.FrameGrabber.Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
		threadStop = false;
		videoGrabber = new Thread(new Runnable()
		{

			public void run()
		    {
		    	try {
					getVideoFromCamera();
				} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		});
		 
		videoGrabber.start();
	}
	
	
	public void stopVideo(){
		synchronized (threadStop) {
			threadStop = true;
			try {
				grabber.stop();
			} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	protected void getVideoFromCamera() throws com.googlecode.javacv.FrameGrabber.Exception {
		try {
			
			IplImage grabbed;		
			while((grabbed = grabber.grab()) != null && !threadStop){
				synchronized (threadStop) {
					image = grabbed.getBufferedImage();
					canvas.setImage(image);
					canvas.paint(null);
				}
				
			}
		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	private FrameGrabber getGrabber(String source)
			throws MalformedURLException, IOException, Exception {
		FrameGrabber grabber = null;
		if(isSourceUrl(source)){		
			Utils.get(source);
			sampleName = Utils.getFileNameFromUrl(source);
			grabber = FFmpegFrameGrabber.createDefault(sampleName);
		}
		else if(isSourceFile(source)){
			sampleName = Utils.getFileName(source);
			grabber = FFmpegFrameGrabber.createDefault(source);
		}
		else{
			//try open the default camera
			grabber = VideoInputFrameGrabber.createDefault(0);
			sampleName = "liveCam";
		}
		return grabber;
	}

	private boolean isSourceFile(String source) {
		return null != source && !isSourceUrl(source) && source.contains(".");
	}

	private boolean isSourceUrl(String source) {
		return null != source && source.contains("://");
	}

	
	
	
	
}
