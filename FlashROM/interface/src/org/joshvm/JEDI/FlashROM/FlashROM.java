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


public interface FlashROM {
	public byte[] readManufacturInfo() throws IOException, DriverException;

	public byte[] readDeviceInfo() throws IOException, DriverException;

	public byte readStatusByte(int offset) throws IOException, DriverException;

	public byte[] readStatusBytes() throws IOException, DriverException;

	public void writeEnable() throws IOException, DriverException;

	public void chipErase() throws IOException, DriverException;

	public void pageProgram(int address, byte[] data) throws IOException, DriverException, UnsupportedPageSizeException;

	public void pageProgram(int address, byte[] data, int offset, int size) throws IOException, DriverException, UnsupportedPageSizeException;

	public byte[] read(int address, int size) throws IOException, DriverException;

	public void erase(int address, int size) throws IOException, DriverException, UnalignedAddressException;

	public void mount(FlashROMDeviceDescriptor desc) throws InvalidDeviceDescriptorException, IOException, DriverException ;

	public int getPageSize();

	public long getTotalSize();
}
