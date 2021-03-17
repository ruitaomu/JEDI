/*
 * Copyright (C) Max Mu
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Please visit www.joshvm.org if you need additional information or
 * have any questions.
 */
package com.joshvm.JEDI.LCD;

import java.io.IOException;

import org.joshvm.JEDI.InvalidDeviceDescriptorException;
import org.joshvm.JEDI.displaydevice.DisplayDeviceDescriptor;
import org.joshvm.JEDI.displaydevice.SPILCDController.SPILCDController;
import org.joshvm.JEDI.displaydevice.SPILCDController.SPILCDDeviceDescriptor;
import org.joshvm.j2me.dio.DeviceManager;
import org.joshvm.j2me.dio.gpio.GPIOPin;
import org.joshvm.j2me.dio.gpio.GPIOPinConfig;
import org.joshvm.j2me.directUI.DisplayDeviceAdaptor;
import org.joshvm.util.ByteBuffer;

public class ILI9341_driver extends SPILCDController {
	public static final int COLOR_MODE_RGB565 = 1;
	public static final int COLOR_MODE_RGB666 = 2;
	public static final int COLOR_MODE_RGB888 = 3;
	public static final int COLOR_MODE_ARGB8888 = 4;

	// public static final int DEFAULT_PIN_DC = 21;
	// public static final int DEFAULT_PIN_RST = 18;
	// public static final int DEFAULT_PIN_BLK = 5;

	public static final int DEFAULT_PIN_DC = 5;
	public static final int DEFAULT_PIN_RST = 0;
	public static final int DEFAULT_PIN_BLK = 22;

	public static final int DEFAULT_SPI_CONTROLLER = 1;
	public static final int DEFAULT_SPI_CS_ADDRESS = 0;
	public static final int DEFAULT_SPI_CLOCK_FREQUENCY = 26 * 1000 * 1000;

	private GPIOPin pin_dc, pin_rst, pin_blk;
	private final int LCD_W = 240;
	private final int LCD_H = 320;

	private void delay_ms(long millisec) {
		try {
			Thread.sleep(millisec);
		} catch (Exception e) {

		}
	}

	private void LCD_Writ_Bus(int data) throws Exception {
		data = data & 0xff;
		spi.write(data);
	}

	private void LCD_Writ_Bus(ByteBuffer data) throws Exception {
		while (data.remaining() > 0) {
			spi.write(data);
		}
	}

	private void OLED_DC_Clr() throws Exception {
		pin_dc.setValue(false);
	}

	private void OLED_DC_Set() throws Exception {
		pin_dc.setValue(true);
	}

	private void OLED_RST_Clr() throws Exception {
		pin_rst.setValue(false);
	}

	private void OLED_RST_Set() throws Exception {
		pin_rst.setValue(true);
	}

	private void OLED_BLK_Clr() throws Exception {
		pin_blk.setValue(false);
	}

	private void OLED_BLK_Set() throws Exception {
		pin_blk.setValue(true);
	}

	private void LCD_WR_DATA8(int da) throws Exception // ��������-8λ����
	{ // OLED_CS_Clr();
		OLED_DC_Set();
		LCD_Writ_Bus(da);
		// OLED_CS_Set();
	}

	private void LCD_WR_DATA(int da) throws Exception {// OLED_CS_Clr();
		OLED_DC_Set();
		LCD_Writ_Bus(da >> 8);
		LCD_Writ_Bus(da);
		// OLED_CS_Set();
	}

	private void LCD_WR_DATA(ByteBuffer buff) throws Exception {// OLED_CS_Clr();
		OLED_DC_Set();
		LCD_Writ_Bus(buff);
		// OLED_CS_Set();
	}

	private void LCD_WR_REG(int da) throws Exception { // OLED_CS_Clr();
		OLED_DC_Clr();
		LCD_Writ_Bus(da);
		// OLED_CS_Set();
	}

	private void LCD_WR_REG_DATA(int reg, int da) throws Exception { // OLED_CS_Clr();
		LCD_WR_REG(reg);
		LCD_WR_DATA(da);
		// OLED_CS_Set();
	}

	private void Address_set(int x1, int y1, int x2, int y2) throws Exception {
		LCD_WR_REG(0x2a);
		LCD_WR_DATA8(x1 >> 8);
		LCD_WR_DATA8(x1);
		LCD_WR_DATA8(x2 >> 8);
		LCD_WR_DATA8(x2);

		LCD_WR_REG(0x2b);
		LCD_WR_DATA8(y1 >> 8);
		LCD_WR_DATA8(y1);
		LCD_WR_DATA8(y2 >> 8);
		LCD_WR_DATA8(y2);

		LCD_WR_REG(0x2C);
	}

	private void Lcd_Init() throws Exception {
		// OLED_CS_Clr(); //��Ƭѡʹ��
		OLED_RST_Clr();
		delay_ms(20);
		OLED_RST_Set();
		delay_ms(20);
		OLED_BLK_Set();

		// ************* Start Initial Sequence **********//
		LCD_WR_REG(0xCF);
		LCD_WR_DATA8(0x00);
		LCD_WR_DATA8(0xD9);
		LCD_WR_DATA8(0X30);

		LCD_WR_REG(0xED);
		LCD_WR_DATA8(0x64);
		LCD_WR_DATA8(0x03);
		LCD_WR_DATA8(0X12);
		LCD_WR_DATA8(0X81);

		LCD_WR_REG(0xE8);
		LCD_WR_DATA8(0x85);
		LCD_WR_DATA8(0x10);
		LCD_WR_DATA8(0x78);

		LCD_WR_REG(0xCB);
		LCD_WR_DATA8(0x39);
		LCD_WR_DATA8(0x2C);
		LCD_WR_DATA8(0x00);
		LCD_WR_DATA8(0x34);
		LCD_WR_DATA8(0x02);

		LCD_WR_REG(0xF7);
		LCD_WR_DATA8(0x20);

		LCD_WR_REG(0xEA);
		LCD_WR_DATA8(0x00);
		LCD_WR_DATA8(0x00);

		LCD_WR_REG(0xC0); // Power control
		LCD_WR_DATA8(0x21); // VRH[5:0]

		LCD_WR_REG(0xC1); // Power control
		LCD_WR_DATA8(0x12); // SAP[2:0];BT[3:0]

		LCD_WR_REG(0xC5); // VCM control
		LCD_WR_DATA8(0x32);
		LCD_WR_DATA8(0x3C);

		LCD_WR_REG(0xC7); // VCM control2
		LCD_WR_DATA8(0XC1);

		LCD_WR_REG(0x36); // Memory Access Control
		LCD_WR_DATA8(0x08);

		LCD_WR_REG(0x3A);
		LCD_WR_DATA8(0x55);

		LCD_WR_REG(0xB1);
		LCD_WR_DATA8(0x00);
		LCD_WR_DATA8(0x18);
		
		
		LCD_WR_REG(0xB6); // Display Function Control
		LCD_WR_DATA8(0x0A);
		LCD_WR_DATA8(0xA2);

		LCD_WR_REG(0xF2); // 3Gamma Function Disable
		LCD_WR_DATA8(0x00);

		LCD_WR_REG(0x26); // Gamma curve selected
		LCD_WR_DATA8(0x01);

		LCD_WR_REG(0xE0); // Set Gamma
		LCD_WR_DATA8(0x0F);
		LCD_WR_DATA8(0x20);
		LCD_WR_DATA8(0x1E);
		LCD_WR_DATA8(0x09);
		LCD_WR_DATA8(0x12);
		LCD_WR_DATA8(0x0B);
		LCD_WR_DATA8(0x50);
		LCD_WR_DATA8(0XBA);
		LCD_WR_DATA8(0x44);
		LCD_WR_DATA8(0x09);
		LCD_WR_DATA8(0x14);
		LCD_WR_DATA8(0x05);
		LCD_WR_DATA8(0x23);
		LCD_WR_DATA8(0x21);
		LCD_WR_DATA8(0x00);

		LCD_WR_REG(0XE1); // Set Gamma
		LCD_WR_DATA8(0x00);
		LCD_WR_DATA8(0x19);
		LCD_WR_DATA8(0x19);
		LCD_WR_DATA8(0x00);
		LCD_WR_DATA8(0x12);
		LCD_WR_DATA8(0x07);
		LCD_WR_DATA8(0x2D);
		LCD_WR_DATA8(0x28);
		LCD_WR_DATA8(0x3F);
		LCD_WR_DATA8(0x02);
		LCD_WR_DATA8(0x0A);
		LCD_WR_DATA8(0x08);
		LCD_WR_DATA8(0x25);
		LCD_WR_DATA8(0x2D);
		LCD_WR_DATA8(0x0F);

		LCD_WR_REG(0x11); // Exit Sleep
		delay_ms(120);
		LCD_WR_REG(0x29); // Display on
		
		
	}

	// ��������
	// Color:Ҫ���������ɫ
	public void clear(int Color) throws Exception {
		int i, j, k;
		byte[] buffer = new byte[2 * LCD_H * LCD_W];

		Address_set(0, 0, LCD_W - 1, LCD_H - 1);
		k = 0;
		for (i = 0; i < LCD_W; i++) {
			for (j = 0; j < LCD_H; j++) {
				// LCD_WR_DATA(Color);
				buffer[k++] = (byte) ((Color >> 8) & 0xff);
				buffer[k++] = (byte) (Color & 0xff);
			}

		}
		ByteBuffer bb = new ByteBuffer(buffer);
		LCD_WR_DATA(bb);
	}

	private byte[] Lcd_getID() throws Exception {
		OLED_DC_Clr();
		spi.write(0x04);
		OLED_DC_Set();
		byte[] id = new byte[3];
		ByteBuffer bb = new ByteBuffer(id);
		spi.read(bb);
		return bb.array();
	}

	public void fill(int x, int y, int w, int h, int Color) throws Exception {
		int i, j, k;
		byte[] buffer = new byte[2 * w * h];

		Address_set(x, y, x + w - 1, y + h - 1);
		k = 0;
		for (i = 0; i < w; i++) {
			for (j = 0; j < h; j++) {
				// LCD_WR_DATA(Color);
				buffer[k++] = (byte) ((Color >> 8) & 0xff);
				buffer[k++] = (byte) (Color & 0xff);
			}

		}
		ByteBuffer bb = new ByteBuffer(buffer);
		LCD_WR_DATA(bb);
	}

	public int getDisplayWidth() {
		return LCD_W;
	}

	public int getDisplayHeight() {
		return LCD_H;
	}

	public int getColorMode() {
		return COLOR_MODE_RGB565;
	}

	public void init(DisplayDeviceDescriptor desc) throws InvalidDeviceDescriptorException, IOException {
		try {
			if (!(desc instanceof ILI9341_DeviceDescriptor)) {
				throw new InvalidDeviceDescriptorException(
						"The input DisplayDeviceDescriptor is not ILI9341_DeviceDescriptor");
			}

			super.init(desc);

			GPIOPinConfig cfg = new GPIOPinConfig(GPIOPinConfig.UNASSIGNED, ((ILI9341_DeviceDescriptor) desc).dc, // DC-PCM_CLK
					GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE,
					false);
			pin_dc = (GPIOPin) DeviceManager.open(cfg, DeviceManager.EXCLUSIVE);

			cfg = new GPIOPinConfig(GPIOPinConfig.UNASSIGNED, ((ILI9341_DeviceDescriptor) desc).rst, // RST-PCM_SYNC
					GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE,
					false);

			pin_rst = (GPIOPin) DeviceManager.open(cfg, DeviceManager.EXCLUSIVE);

			cfg = new GPIOPinConfig(GPIOPinConfig.UNASSIGNED, ((ILI9341_DeviceDescriptor) desc).blk, // BLK-PCM_DIN
					GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE,
					false);

			pin_blk = (GPIOPin) DeviceManager.open(cfg, DeviceManager.EXCLUSIVE);
			Lcd_Init();
			// System.out.println("LCD ID:");
			// byte[] id = Lcd_getID();
			// for (int i = 0; i < id.length; i++) {
			// System.out.println(Integer.toString(id[i], 16));
			// }

			// clear(0xf);
			// fill(0, 0, 10, 10, 0x11);

		} catch (InvalidDeviceDescriptorException idde) {
			throw idde;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Can't open display device");
		}
	}

	public void update(int top_left_x, int top_left_y, byte[] imageData, int w, int h) {
		try {
			Address_set(top_left_x, top_left_y, top_left_x + w - 1, top_left_y + h - 1);

			LCD_WR_DATA(new ByteBuffer(imageData));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DisplayDeviceAdaptor getDevice() {
		return null;
	}

	public void showQRCode(int top_left_x, int top_left_y, boolean[][] matrix) {
		try {
			int w = matrix.length;
			int h = matrix.length;
			byte[] buffer = new byte[2 * w * h * 4 * 4];
			int k = 0;
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					if (matrix[j][i]) {
						int t = k;
						for (int m = 0; m < 4; m++) {
							buffer[t++] = (byte) 0x00;
							buffer[t++] = (byte) 0x00;
							buffer[t++] = (byte) 0x00;
							buffer[t++] = (byte) 0x00;
							buffer[t++] = (byte) 0x00;
							buffer[t++] = (byte) 0x00;
							buffer[t++] = (byte) 0x00;
							buffer[t++] = (byte) 0x00;
							t += (w * 4 * 2 - 8);
						}
						k += 2 * 4;
					} else {
						int t = k;
						for (int n = 0; n < 4; n++) {
							buffer[t++] = (byte) 0xff;
							buffer[t++] = (byte) 0xff;
							buffer[t++] = (byte) 0xff;
							buffer[t++] = (byte) 0xff;
							buffer[t++] = (byte) 0xff;
							buffer[t++] = (byte) 0xff;
							buffer[t++] = (byte) 0xff;
							buffer[t++] = (byte) 0xff;
							t += (w * 4 * 2 - 8);
						}
						k += 2 * 4;
					}
				}
				k += w * 2 * 4 * 3;
			}
			Address_set(top_left_x, top_left_y, top_left_x + w * 4 - 1, top_left_y + h * 4 - 1);
			ByteBuffer bb = new ByteBuffer(buffer);
			LCD_WR_DATA(bb);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static SPILCDDeviceDescriptor getDeviceDescriptor(int SPIContollerNumber, int CSAddress) {
		return getDeviceDescriptor(SPIContollerNumber, CSAddress, DEFAULT_SPI_CLOCK_FREQUENCY);
	}

	public static SPILCDDeviceDescriptor getDeviceDescriptor(int SPIContollerNumber, int CSAddress,
			int clockFrequency) {
		return new ILI9341_DeviceDescriptor("com.joshvm.JEDI.LCD.ILI9341_driver", SPIContollerNumber, CSAddress,
				clockFrequency, 0, /* Clock mode 0 */
				8, /* Word length 8 */
				SPILCDDeviceDescriptor.BIG_ENDIAN, SPILCDDeviceDescriptor.CS_NOT_CONTROLLED, DEFAULT_PIN_DC,
				DEFAULT_PIN_RST, DEFAULT_PIN_BLK);
	}

	static class ILI9341_DeviceDescriptor extends SPILCDDeviceDescriptor {
		int dc;
		int rst;
		int blk;

		ILI9341_DeviceDescriptor(String classNameOfFlashROMDriver, int SPIControllerNumber, int CSAddress,
				int clockFrequency, int clockMode, int wordLength, int bitOrdering, int csActiveLevel, int pin_dc,
				int pin_rst, int pin_blk) {
			super(classNameOfFlashROMDriver, SPIControllerNumber, CSAddress, clockFrequency, clockMode, wordLength,
					bitOrdering, csActiveLevel);
			dc = pin_dc;
			rst = pin_rst;
			blk = pin_blk;

		}
	}
}
