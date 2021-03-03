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
package org.joshvm.JEDI.FlashROM.SPIFlash;

import org.joshvm.j2me.dio.*;
import org.joshvm.j2me.dio.spibus.*;
import org.joshvm.JEDI.FlashROM.FlashROM;
import org.joshvm.JEDI.FlashROM.FlashROMDeviceDescriptor;
import org.joshvm.j2me.dio.DeviceException;
import org.joshvm.JEDI.InvalidDeviceDescriptorException;
import java.io.IOException;

public abstract class SPIFlash implements FlashROM {

	protected SPIDevice spi = null;
	
 	protected SPIDevice getSPIDevice(int SPIControllerNumber, int CSAddress, int clockFrequency, int clockMode, int wordLength, int bitOrdering, int csActiveLevel)
		throws IOException {		

		SPIDevice spi = null;
		try {
			SPIDeviceConfig config = new SPIDeviceConfig.Builder()
									.setControllerNumber(SPIControllerNumber)
									.setAddress(CSAddress)
									.setClockFrequency(clockFrequency)
									.setClockMode(clockMode)
									.setWordLength(wordLength)
									.setBitOrdering(bitOrdering)
									.setCSActiveLevel(csActiveLevel)
									.build();
			spi = (SPIDevice)DeviceManager.open(config, DeviceManager.EXCLUSIVE);
		} catch (DeviceException e) {
			throw new IOException(e.toString());
		}
		
		return spi;
	}

	public void mount(FlashROMDeviceDescriptor desc) throws InvalidDeviceDescriptorException, IOException {
		
		if (desc instanceof SPIFlashDeviceDescriptor) {
			if (spi != null) {
				return; //Do nothing if already mounted
			}
			
			SPIFlashDeviceDescriptor spi_flash_desc = (SPIFlashDeviceDescriptor)desc;
			
			int bitordering = spi_flash_desc.getBitOrdering();
			if (bitordering == SPIFlashDeviceDescriptor.BIG_ENDIAN) {
				bitordering = Device.BIG_ENDIAN;
			} else if (bitordering == SPIFlashDeviceDescriptor.LITTLE_ENDIAN) {
				bitordering = Device.LITTLE_ENDIAN;
			} else if (bitordering == SPIFlashDeviceDescriptor.MIXED_ENDIAN) {
				bitordering = Device.MIXED_ENDIAN;
			} else {
				throw new InvalidDeviceDescriptorException("Invalid bit ordering");
			}
			
			int csactive = spi_flash_desc.getCSActiveLevel();
			if (csactive == SPIFlashDeviceDescriptor.CS_ACTIVE_LOW) {
				csactive = SPIDeviceConfig.CS_ACTIVE_LOW;
			} else if (csactive == SPIFlashDeviceDescriptor.CS_ACTIVE_HIGH) {
				csactive = SPIDeviceConfig.CS_ACTIVE_HIGH;
			} else if (csactive == SPIFlashDeviceDescriptor.CS_NOT_CONTROLLED) {
				csactive = SPIDeviceConfig.CS_NOT_CONTROLLED;
			} else {
				throw new InvalidDeviceDescriptorException("Invalid CS active level");
			}
			
			SPIDevice spi = getSPIDevice(spi_flash_desc.getSPIControllerNumber(),
					spi_flash_desc.getCSAddress(),
					spi_flash_desc.getClockFrequency(),
					spi_flash_desc.getClockMode(),
					spi_flash_desc.getWordLength(),
					bitordering,
					csactive);
			this.spi = spi;
		} else {
			throw new InvalidDeviceDescriptorException();
		}
	}
}
