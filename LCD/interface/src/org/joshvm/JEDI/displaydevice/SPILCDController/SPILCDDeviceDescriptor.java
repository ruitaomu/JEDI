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
package org.joshvm.JEDI.displaydevice.SPILCDController;

import org.joshvm.JEDI.displaydevice.DisplayDeviceDescriptor;

public class SPILCDDeviceDescriptor implements DisplayDeviceDescriptor {
	protected int SPIControllerNumber;
	protected int CSAddress;
	protected int ClockFrequency;
	protected int ClockMode;
	protected int WordLength;
	protected int BitOrdering;
	protected int CSActiveLevel;
	protected String driverName;

	public static final int	LITTLE_ENDIAN	= 0;
	public static final int	BIG_ENDIAN		= 1;
	public static final int	MIXED_ENDIAN	= 2;


    public static final int CS_ACTIVE_HIGH = 0;
    public static final int CS_ACTIVE_LOW = 1;
    public static final int CS_NOT_CONTROLLED = 2;

	public SPILCDDeviceDescriptor(String classNameOfDriver,
												int SPIControllerNumber, 
												int CSAddress, 
												int clockFrequency, 
												int clockMode, 
												int wordLength, 
												int bitOrdering, 
												int csActiveLevel) {
		this.SPIControllerNumber = SPIControllerNumber;
		this.CSAddress = CSAddress;
		this.ClockFrequency = clockFrequency;
		this.ClockMode = clockMode;
		this.WordLength = wordLength;
		this.BitOrdering = bitOrdering;
		this.CSActiveLevel = csActiveLevel;
		this.driverName = classNameOfDriver;
	}
	
	public int getSPIControllerNumber() {return SPIControllerNumber;}
	public int getCSAddress() {return CSAddress;}
	public int getClockFrequency() {return ClockFrequency;}
	public int getClockMode() {return ClockMode;}
	public int getWordLength() {return WordLength;}
	public int getBitOrdering() {return BitOrdering;}
	public int getCSActiveLevel() {return CSActiveLevel;}
	public boolean equals(Object anObject) {
	   if (this == anObject) {
           return true;
       }
       if (anObject instanceof SPILCDDeviceDescriptor) {
	       SPILCDDeviceDescriptor another = (SPILCDDeviceDescriptor)anObject;
	       if (another.driverName.equals(driverName) && 
		   	   (another.SPIControllerNumber == SPIControllerNumber) &&
		   	   (another.CSAddress == CSAddress))
		   	   return true;
	   }
	   return false;
	}

	public DisplayDeviceDescriptor getDescriptor() {
		return this;
	}

	public Class getDisplayDeviceClass() throws ClassNotFoundException {
		return Class.forName(driverName);
	}
}


