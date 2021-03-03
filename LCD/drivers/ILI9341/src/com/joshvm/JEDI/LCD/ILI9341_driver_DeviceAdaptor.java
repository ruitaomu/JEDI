package com.joshvm.JEDI.LCD;

import org.joshvm.JEDI.displaydevice.DisplayDevice;
import java.io.IOException;

import org.joshvm.j2me.directUI.Image;
import org.joshvm.j2me.directUI.ImageBuffer;
import org.joshvm.j2me.directUI.Text;
import org.joshvm.JEDI.displaydevice.DisplayDeviceFactory;
import org.joshvm.JEDI.DriverException;
import org.joshvm.JEDI.InvalidDeviceDescriptorException;

public class ILI9341_driver_DeviceAdaptor implements org.joshvm.j2me.directUI.DisplayDeviceAdaptor {
	private ILI9341_driver driver;
	public ILI9341_driver_DeviceAdaptor()  throws InvalidDeviceDescriptorException, IOException, DriverException {
		driver = (ILI9341_driver) DisplayDeviceFactory.getDevice(
			ILI9341_driver.getDeviceDescriptor(ILI9341_driver.DEFAULT_SPI_CONTROLLER, ILI9341_driver.DEFAULT_SPI_CS_ADDRESS, ILI9341_driver.DEFAULT_SPI_CLOCK_FREQUENCY));
	}

	public int getDisplayWidth() {
		return driver.getDisplayWidth();
	}

	public int getDisplayHeight() {
		return driver.getDisplayHeight();
	}

	public int getColorMode() {
		return driver.getColorMode();
	}

	public void update(int top_left_x, int top_left_y, ImageBuffer framebuffer) {
		driver.update(top_left_x, top_left_y, framebuffer.getImageData(), framebuffer.getWidth(), framebuffer.getHeight());
	}

	public void clear(int rgb) {
		try {
			driver.clear(rgb);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void flush() {
		// TODO Auto-generated method stub
		
	}

	public void showImage(int top_left_x, int top_left_y, Image image, boolean delayshow) {
		// TODO Auto-generated method stub
		
	}

	public void showText(int top_left_x, int top_left_y, Text text, boolean delayshow) {
		// TODO Auto-generated method stub
		
	}

	public void turnOffBacklight() {
		// TODO Auto-generated method stub
		
	}

	public void turnOnBacklight() {
		// TODO Auto-generated method stub
		
	}

	public void update(int top_left_x, int top_left_y, ImageBuffer framebuffer, boolean delayshow) {
		// TODO Auto-generated method stub
		
	}
}

