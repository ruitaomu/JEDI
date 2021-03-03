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
package org.joshvm.JEDI.FlashROM;

import java.io.IOException;

import org.joshvm.JEDI.DriverException;
import org.joshvm.JEDI.InvalidDeviceDescriptorException;


public final class FlashROMDeviceFactory {
	public static FlashROM getDevice(FlashROMDeviceDescriptor desc)  throws InvalidDeviceDescriptorException, IOException, DriverException {
		FlashROM device;
		try {
			Class cls = desc.getFlashROMDeviceClass();
			device = (FlashROM)cls.newInstance();
		} catch (ClassNotFoundException cnfe) {
			throw new DriverException(cnfe.getMessage());
		} catch (Exception e) {
			throw new DriverException(e.toString());
		}
		device.mount(desc);
		return device;
	}
}
