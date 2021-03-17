package com.joshvm.JEDI.LCD;

import java.io.IOException;

import org.joshvm.JEDI.DriverException;
import org.joshvm.JEDI.InvalidDeviceDescriptorException;
import org.joshvm.JEDI.displaydevice.DisplayDevice;
import org.joshvm.JEDI.displaydevice.DisplayDeviceFactory;
import org.joshvm.j2me.directUI.Image;
import org.joshvm.j2me.directUI.ImageBuffer;
import org.joshvm.j2me.directUI.Text;

public class ILI9341_driver_DeviceAdaptor implements org.joshvm.j2me.directUI.DisplayDeviceAdaptor {
	private DisplayDevice driver;
	public ILI9341_driver_DeviceAdaptor()  throws InvalidDeviceDescriptorException, IOException, DriverException {
		driver = DisplayDeviceFactory.getDevice(
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
		byte[] origdata = framebuffer.getImageData(); //RGB565-LE
		byte[] data = new byte[origdata.length]; //BGR565-BE
		
		for (int i = 0; i < origdata.length; i+=2) {
			//Change from RGB-LE to BGR-BE
			int B = origdata[i] & 0x1f;
			int R = (origdata[i+1] >> 3) & 0x1f;
			int Gh = origdata[i+1] & 0x07;
			int Gl = (origdata[i] >> 5) & 0x07;
			data[i] = (byte)(B<<3|Gh);
			data[i+1] = (byte)(Gl<<5|R);
		}
		driver.update(top_left_x, top_left_y, data, framebuffer.getWidth(), framebuffer.getHeight());
	}

	public void clear(int rgb) {
        int b = (rgb & 0xff);
        int g = (rgb & 0xff00) >> 8;
        int r = (rgb & 0xff0000) >> 16;
        byte colorL = (byte)(((b >> 3) | ((g & 0xfc) << 3)) & 0xff);
        byte colorH = (byte)((g >> 5) | (r & 0xf8) & 0xff);
        int rgb16 = (colorH & 0xff) << 8 | (colorL & 0xff);
        try {
            ((ILI9341_driver)driver).clear(rgb16);
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

	public void update(int top_left_x, int top_left_y, ImageBuffer framebuffer, boolean delayshow) {
		update(top_left_x, top_left_y, framebuffer);
	}

	public void turnOffBacklight() {
		// TODO Auto-generated method stub
		
	}

	public void turnOnBacklight() {
		// TODO Auto-generated method stub
		
	}
}

