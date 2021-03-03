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
package com.joshvm.JEDI.FlashROM.W25Q64;

import java.io.IOException;
import org.joshvm.JEDI.DriverException;
import org.joshvm.JEDI.FlashROM.UnalignedAddressException;
import org.joshvm.JEDI.FlashROM.UnsupportedPageSizeException;

import org.joshvm.JEDI.FlashROM.SPIFlash.SPIFlash;
import org.joshvm.JEDI.FlashROM.SPIFlash.SPIFlashDeviceDescriptor;
import org.joshvm.j2me.dio.*;
import org.joshvm.j2me.dio.spibus.*;
import org.joshvm.util.ByteBuffer;

public class W25Q64_driver extends SPIFlash {
	private static final int INSTRUCTION_ERASE_SECTOR = 0x20;
	private static final int INSTRUCTION_ERASE_BLOCK_32K = 0x52;
	private static final int INSTRUCTION_ERASE_BLOCK_64K = 0xD8;
	private static final int INSTRUCTION_ERASE_CHIP = 0x60;
	private static final int INSTRUCTION_WRITE_ENABLE = 0x06;
	private static final int INSTRUCTION_PROGRAM_PAGE = 0x02;
	private static final int INSTRUCTION_READ_DATA = 0x03;
	private static final int INSTRUCTION_READ_STATUS_1 = 0x05;
	private static final int INSTRUCTION_READ_STATUS_2 = 0x35;
	private static final int INSTRUCTION_READ_STATUS_3 = 0x15;
	private static final int INSTRUCTION_READ_DEVICE_INFO = 0x90;

	public static final int PAGE_SIZE = 256;
	public static final int SIZE_32K = 32*1024;
	public static final int SIZE_64K = 64*1024;

	private static final int DEFAULT_CLOCK_FREQUENCY = 20*1024*1024;

	private void SPI_writeAndRead(ByteBuffer writebuf, int skip, ByteBuffer readbuf) throws IOException, DriverException {
		try{
			spi.writeAndRead(writebuf, skip, readbuf);
		} catch (DeviceException e) {
	  		throw new DriverException(e.toString());
	  	}
	}

	private void SPI_write(ByteBuffer writebuf) throws IOException, DriverException {
		try{
			spi.write(writebuf);
		} catch (DeviceException e) {
	  		throw new DriverException(e.toString());
	  	}
	}

	public int getPageSize() {
		return PAGE_SIZE;
	}

	public byte[] readManufacturInfo() throws IOException, DriverException {
		byte[] command = new byte[4];
		command[0] = (byte)INSTRUCTION_READ_DEVICE_INFO;
		command[1] = (byte)0;
		command[2] = (byte)0;
		command[3] = (byte)0;
		ByteBuffer command_buf = new ByteBuffer(command);

		ByteBuffer result_buf = ByteBuffer.allocate(2);
		SPI_writeAndRead(command_buf, 4, result_buf);
		result_buf.rewind();
		byte[] result = new byte[1];
		result[0] = result_buf.get();
		return result;
	}

	public byte[] readDeviceInfo() throws IOException, DriverException {
		byte[] command = new byte[4];
		command[0] = (byte)INSTRUCTION_READ_DEVICE_INFO;
		command[1] = (byte)0;
		command[2] = (byte)0;
		command[3] = (byte)1;
		ByteBuffer command_buf = new ByteBuffer(command);

		ByteBuffer result_buf = ByteBuffer.allocate(2);
		SPI_writeAndRead(command_buf, 4, result_buf);
		result_buf.rewind();
		byte[] result = new byte[1];
		result[0] = result_buf.get();
		return result;
	}

	public byte readStatusByte(int offset) throws IOException, DriverException {
		byte[] command = new byte[1];
		switch (offset) {
			case 0:
				command[0] = (byte)INSTRUCTION_READ_STATUS_1;
				break;
			case 1:
				command[0] = (byte)INSTRUCTION_READ_STATUS_2;
				break;
			case 2:
				command[0] = (byte)INSTRUCTION_READ_STATUS_3;
				break;
			default:
				throw new IllegalArgumentException("Invalid offset of reading Status Register");
		}
		ByteBuffer command_buf = new ByteBuffer(command);
		ByteBuffer result_buf = ByteBuffer.allocate(1);
		SPI_writeAndRead(command_buf, 1, result_buf);
		result_buf.rewind();
		return result_buf.get();
	}

	protected byte readStatusByte() throws IOException, DriverException {
		return readStatusByte(0);
	}

	public byte[] readStatusBytes() throws IOException, DriverException {
		byte[] result = new byte[3];
		result[0] = readStatusByte(0);
		result[1] = readStatusByte(1);
		result[2] = readStatusByte(2);
		return result;
	}

	public void writeEnable() throws IOException, DriverException {
		byte[] command = new byte[1];
		command[0] = (byte)INSTRUCTION_WRITE_ENABLE;
		ByteBuffer command_buf = new ByteBuffer(command);
		SPI_write(command_buf);
	}

	public void chipErase() throws IOException, DriverException {
		writeEnable();
		byte[] command = new byte[1];
		command[0] = (byte)INSTRUCTION_ERASE_CHIP;
		ByteBuffer command_buf = new ByteBuffer(command);
		SPI_write(command_buf);
		waitWIPClear();
	}

	public void pageProgram(int address, byte[] data) throws IOException, DriverException, UnsupportedPageSizeException {
		pageProgram(address, data, 0, data.length);
	}

	public void pageProgram(int address, byte[] data, int offset, int size) throws IOException, DriverException, UnsupportedPageSizeException {
		if (size != PAGE_SIZE) {
			throw new UnsupportedPageSizeException("The size of data must be " + PAGE_SIZE);
		}

		if ((offset + size > data.length) || (offset < 0)) {
			throw new IllegalArgumentException();
		}

		writeEnable();

		byte[] command = new byte[4];
		address = address & 0x00ffffff;
		command[0] = (byte)INSTRUCTION_PROGRAM_PAGE;
		command[1] = (byte)((address>>16)&0xff);
		command[2] = (byte)((address>>8)&0xff);
		command[3] = (byte)(address&0xff);
		ByteBuffer command_buf = ByteBuffer.allocate(4+size);
		ByteBuffer data_buf = new ByteBuffer(data, offset, offset, offset+size);
		command_buf.put(command);
		command_buf.put(data_buf);
		command_buf.flip();
		SPI_write(command_buf);
		waitWIPClear();
	}

	public byte[] read(int address, int size) throws IOException, DriverException {
		byte[] command = new byte[4];
		address = address & 0x00ffffff;
		command[0] = (byte)INSTRUCTION_READ_DATA;
		command[1] = (byte)((address>>16)&0xff);
		command[2] = (byte)((address>>8)&0xff);
		command[3] = (byte)(address&0xff);
		ByteBuffer command_buf = new ByteBuffer(command);

		ByteBuffer result_buf = ByteBuffer.allocate(size);
		SPI_writeAndRead(command_buf, 4, result_buf);
		return result_buf.array();
	}

	public void erase(int address, int size) throws IOException, UnalignedAddressException, DriverException {
		switch (size) {
			case PAGE_SIZE:
				eraseSector(address);
				break;
			case SIZE_32K:
				erase32KBlock(address);
				break;
			case SIZE_64K:
				erase64KBlock(address);
				break;
			default:
				throw new UnalignedAddressException("Invalid address: " + address);
		}
	}

	public long getTotalSize() {
		return 8*1024L*1024L;
	}

	private void eraseSector(int address) throws IOException, DriverException {
		eraseCommonInternal(address, (byte)INSTRUCTION_ERASE_SECTOR);
	}

	private void erase32KBlock(int address) throws IOException, DriverException {
		eraseCommonInternal(address, (byte)INSTRUCTION_ERASE_BLOCK_32K);
	}

	private void erase64KBlock(int address) throws IOException, DriverException {
		eraseCommonInternal(address, (byte)INSTRUCTION_ERASE_BLOCK_64K);
	}

	private void eraseCommonInternal(int address, byte inst) throws IOException, DriverException {
		writeEnable();

		byte[] command = new byte[4];
		address = address & 0x00ffffff;
		command[0] = inst;
		command[1] = (byte)((address>>16)&0xff);
		command[2] = (byte)((address>>8)&0xff);
		command[3] = (byte)(address&0xff);
		ByteBuffer command_buf = new ByteBuffer(command);

		SPI_write(command_buf);
		waitWIPClear(0);
	}

	public void waitWIPClear() throws IOException, DriverException {
		waitWIPClear(0);
	}

	private void waitWIPClear(int interval) throws IOException, DriverException {
		byte byteStatus;
		do {
			byteStatus = readStatusByte();
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
			}
		} while ((byteStatus & 0x01) == 0x01);
	}

	public static SPIFlashDeviceDescriptor getDeviceDescriptor (int SPIContollerNumber, int CSAddress) {
		return getDeviceDescriptor(SPIContollerNumber, CSAddress, DEFAULT_CLOCK_FREQUENCY);
	}

	public static SPIFlashDeviceDescriptor getDeviceDescriptor (int SPIContollerNumber, int CSAddress, int clockFrequency) {
		return new SPIFlashDeviceDescriptor("com.joshvm.JEDI.FlashROM.W25Q64.W25Q64_driver",
											SPIContollerNumber,
											CSAddress,
											clockFrequency,
											0, /*Clock mode 0*/
											8, /*Word length 8*/
											SPIFlashDeviceDescriptor.BIG_ENDIAN,
											SPIFlashDeviceDescriptor.CS_ACTIVE_LOW);
	}
}
