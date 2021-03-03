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
package com.joshvm.JEDI.sensor.accelerometer;

import java.io.IOException;

import org.joshvm.j2me.dio.ClosedDeviceException;
import org.joshvm.j2me.dio.DeviceManager;
import org.joshvm.j2me.dio.UnavailableDeviceException;
import org.joshvm.j2me.dio.gpio.GPIOPin;
import org.joshvm.j2me.dio.gpio.GPIOPinConfig;
import org.joshvm.JEDI.sensor.accelerometer.*;
import org.joshvm.j2me.sensor.*;

public class BMA250_driver implements Accelerometer, SensorDevice {
	public  static final byte REG_ID = 0x00;
	public  static final byte REG_X_LSB = 0x02;
	public  static final byte REG_X_MSB = 0x03;
	public  static final byte REG_Y_LSB = 0x04;
	public  static final byte REG_Y_MSB = 0x05;
	public  static final byte REG_Z_LSB = 0x06;
	public  static final byte REG_Z_MSB = 0x07;
	public  static final byte REG_INT_STATUS = 0x09;
	public  static final byte REG_TRIG_STATUS = 0x0C;
	public  static final byte REG_INT_SETTING0 = 0x16;
	public  static final byte REG_INT_SETTING1 = 0x17;
	public  static final byte REG_INT_MAPPING0 = 0x19;
	public  static final byte REG_INT_MAPPING1 = 0x1A;
	public  static final byte REG_INT_MAPPING2 = 0x1B;
	public  static final byte REG_LOW_DUR = 0x22;
	public  static final byte REG_LOW_TH = 0x23;
	public  static final byte REG_LOW_MODE = 0x24;
	public  static final byte REG_HIGH_DUR = 0x25;
	public  static final byte REG_HIGH_TH = 0x26;
	public  static final byte REG_SLOPE_DUR = 0x27;
	public  static final byte REG_SLOPE_TH = 0x28;
	
	public  static final byte REG_GRANGE = 0x0F;
	public  static final byte REG_BWD = 0x10;
	public  static final byte REG_PM = 0x11;
	
	public  static final byte ADDR_W = 0x30;
	public  static final byte ADDR_R = 0x31;

	public	static final byte BMP_AS_RANGE_2 = 0x03;
	public  static final byte BMP_AS_RANGE_4 = 0x05;
	public  static final byte BMP_AS_BANDWIDTH_125 = 0x0C;
	public  static final byte BMP_AS_SLEEPPHASE_2 = 0x4e;
	
	private GPIOPin SDA = null;
	private GPIOPin SCL = null;
	private Thread listenThread = null;
	private SensorEventListener eventHandler = null;
	
	private void startbit() throws IOException, UnavailableDeviceException, ClosedDeviceException {
		SDA.setValue(true);
		SCL.setValue(true);
		SDA.setValue(false);
		SCL.setValue(false);
	}

	private void stopbit() throws IOException, UnavailableDeviceException, ClosedDeviceException {
		SDA.setValue(false);
		SCL.setValue(true);
		SDA.setValue(true);
	}
	
	private void sendNack() throws IOException, UnavailableDeviceException, ClosedDeviceException {
		writebit(true);
	}
	
	private void sendAck() throws IOException, UnavailableDeviceException, ClosedDeviceException {
		writebit(false);
	}

	private void readAck() throws IOException, UnavailableDeviceException, ClosedDeviceException {
		readbit();
	}

	private void writebit(boolean value) throws IOException, UnavailableDeviceException, ClosedDeviceException {
		if (value) {
			SDA.setValue(true);
		} else {
			SDA.setValue(false);
		}
		SCL.setValue(true);
		SCL.setValue(false);
	}

	private boolean readbit() throws IOException, UnavailableDeviceException, ClosedDeviceException {
		boolean val;
		SDA.setValue(true); //Release SDA
		SCL.setValue(true);	
		SDA.setDirection(GPIOPin.INPUT);
		val = SDA.getValue();
		SDA.setDirection(GPIOPin.OUTPUT);
		SCL.setValue(false);
		return val;
	}

	private void writebyte(byte cmd) throws IOException, UnavailableDeviceException, ClosedDeviceException {
		int bitmask = 0x80;
		while (bitmask != 0) {
			if ((cmd & (byte)(bitmask&0xff)) == 0) {
				writebit(false);
			} else {
				writebit(true);
			}
			bitmask >>>= 1;
		}
	}

	private byte readbyte() throws IOException, UnavailableDeviceException, ClosedDeviceException {
		byte val = 0;
		int bitmask = 0x80;
		while (bitmask != 0) {
			if (readbit()) {
				val |= (byte)(bitmask&0xff);
			}
			bitmask >>>= 1;
		}

		return val;
	}
	
	private void send(byte cmd, byte value) throws IOException, UnavailableDeviceException, ClosedDeviceException {
		startbit();
		writebyte(ADDR_W);
		readAck();
		writebyte(cmd);
		readAck();
		writebyte(value);
		readAck();
		stopbit();
	}

	private byte read(byte cmd) throws IOException, UnavailableDeviceException, ClosedDeviceException {
		byte b;
		startbit();
		writebyte(ADDR_W);
		readAck();
		writebyte(cmd);
		readAck();
		stopbit();
		startbit();
		writebyte(ADDR_R);
		readAck();
		b = readbyte();
		sendNack();
		stopbit();
		return b;
	}

	private byte[] read_burst(byte cmd, int len) throws IOException, UnavailableDeviceException, ClosedDeviceException {
		byte[] buf = new byte[len];
		startbit();
		writebyte(ADDR_W);
		readAck();
		writebyte(cmd);
		readAck();
		stopbit();
		startbit();
		writebyte(ADDR_R);
		readAck();
		for (int i = 0; i < len; i++) {
			buf[i] = readbyte();
			if (i == (len - 1)) {
				sendNack();
			} else {
				sendAck();
			}
		}
		stopbit();
		return buf;
	}
	
	public synchronized byte readID() throws IOException, UnavailableDeviceException, ClosedDeviceException {
		return read(REG_ID);
	}

	public synchronized int[] readXYZAcc() throws IOException, UnavailableDeviceException, ClosedDeviceException  {
		int[] xyz = new int[3];
		byte[] buf = read_burst(REG_X_LSB, 6);
		xyz[0] = ((buf[0]>>6)&0x03)+(((int)buf[1])*4);
		xyz[1] = ((buf[2]>>6)&0x03)+(((int)buf[3])*4);
		xyz[2] = ((buf[4]>>6)&0x03)+(((int)buf[5])*4);
		return xyz;
	}

	private void initBMA250() throws IOException, UnavailableDeviceException, ClosedDeviceException {
		send(REG_GRANGE, BMP_AS_RANGE_2); //Grange
		send(REG_BWD, BMP_AS_BANDWIDTH_125); //bwd
		send(REG_PM, BMP_AS_SLEEPPHASE_2); //PM
	}

	private synchronized byte readStatus()  throws IOException, UnavailableDeviceException, ClosedDeviceException {
		return read(REG_INT_STATUS);
	}

	private SensorEventListener dummyListener = null;
	private int m_interval_ms = 100;

	public void setIntervalTime(int interval_ms) {
		m_interval_ms = interval_ms;
	}

	public SensorDevice getDevice() throws UnavailableDeviceException {
		return this;
	}

	public Sensor getSensor(SensorDescriptor sensorDeviceDescriptor) throws UnavailableDeviceException {
		return this;
	}
	
 	public void startDevice(SensorDescriptor sensorDeviceDescriptor) throws IOException {
		if (closed == false) {
			//Already start
			return;
		}
		
		try {			
		
		GPIOPinConfig cfgSCL = new GPIOPinConfig(GPIOPinConfig.UNASSIGNED, 
												23, //SCL2
												GPIOPinConfig.DIR_OUTPUT_ONLY,
												GPIOPinConfig.MODE_OUTPUT_OPEN_DRAIN,
												GPIOPinConfig.TRIGGER_NONE,
												true);
		SCL = (GPIOPin)DeviceManager.open(cfgSCL, DeviceManager.EXCLUSIVE);
		GPIOPinConfig cfgSDA = new GPIOPinConfig(GPIOPinConfig.UNASSIGNED, 
												22, //SDA2
												GPIOPinConfig.DIR_OUTPUT_ONLY,
												GPIOPinConfig.MODE_OUTPUT_OPEN_DRAIN,
												GPIOPinConfig.TRIGGER_NONE,
												true);
		SDA = (GPIOPin)DeviceManager.open(cfgSDA, DeviceManager.EXCLUSIVE);
		
		initBMA250();
		byte id = readID();
		//System.out.println("Read key value: "+id);

		send(REG_INT_SETTING0, (byte)0x07); //Enable Slope INT xyz
		send(REG_INT_SETTING1, (byte)0x07); //Enable High-g INT xyz

		closed = false;

		eventHandler = null;
		dummyListener = new SensorEventListener() {public void eventDetected(SensorEvent evt){}};

		listenThread = new Thread ( new Runnable() {
			public void run() {
			try {
				while(!closed) {
					byte int_status = readStatus();
					boolean flag = false;
					if ((int_status & 0x04) != 0) {
						flag = true;
						//System.out.println("Slope detected");
					}
					if ((int_status & 0x02) != 0) {
						flag = true;
						//System.out.println("High-g detected");
					}
					if ((int_status & 0x01) != 0) {
						flag = true;
						//System.out.println("Low-g detected");
					}
					if (flag) {
						//int[] xyz = readXYZAcc();
						//System.out.println("x:"+xyz[0]+"\ty:"+xyz[1]+"\tz:"+xyz[2]);
						if (eventHandler != null) {
							eventHandler.eventDetected(new AccelerometerEvent(int_status));
						}
					}
					try {
						Thread.sleep(m_interval_ms);
					} catch (InterruptedException e) {
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (eventHandler != null) {
				eventHandler.eventDetected(new DeviceStopEvent());
			}
			} //end of run()
		});

		listenThread.start();
		
		} catch (Exception e) {
			stopDevice();
			e.printStackTrace();
			throw new IOException(e.toString());
		}

		return;
 	}

	public void setListener(SensorEventListener e) {
		if (e == null) {
			eventHandler = dummyListener;
		} else {
			eventHandler = e;
		}
	}

	private boolean closed = true;
	public void stopDevice() throws IOException, UnavailableDeviceException {
		closed = true;
		if (listenThread != null) {			
			try {
				listenThread.join();
			} catch (InterruptedException e) {
			}
			listenThread = null;
			eventHandler = null;
		}
		
		if (SCL != null) {
			SCL.close();
			SCL = null;
		}

		if (SDA != null) {
			SDA.close();
			SDA = null;
		}
	}

	public boolean isSupportListener() {
		return true;
	}

	public static SensorDescriptor getDeviceDescriptor() {
		return new SensorDescriptor("com.joshvm.JEDI.sensor.accelerometer.BMA250_driver");
	}

}

