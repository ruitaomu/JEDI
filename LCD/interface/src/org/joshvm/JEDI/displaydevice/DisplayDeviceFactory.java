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
package org.joshvm.JEDI.displaydevice;

import java.io.IOException;
import java.util.Hashtable;
import org.joshvm.JEDI.DriverException;
import org.joshvm.JEDI.InvalidDeviceDescriptorException;

public final class DisplayDeviceFactory {
	private static Hashtable deviceList = new Hashtable(1);
		
	public static synchronized DisplayDevice getDevice(DisplayDeviceDescriptor desc)  throws InvalidDeviceDescriptorException, IOException, DriverException {
		try {
			DisplayDevice device = (DisplayDevice)deviceList.get(desc);
			if (device == null) {			
				Class cls = desc.getDisplayDeviceClass();
				device = (DisplayDevice)cls.newInstance();
				device.init(desc);
				deviceList.put(desc, device);
			}
			return device;
		} catch (ClassNotFoundException cnfe) {
			throw new DriverException("Driver class not found:"+cnfe.toString());
		} catch (InstantiationException ie) {
			throw new DriverException("Driver init error:"+ie.toString());
		} catch (IllegalAccessException iae) {
			throw new DriverException("Driver init error:"+iae.toString());
		}
	}
}
