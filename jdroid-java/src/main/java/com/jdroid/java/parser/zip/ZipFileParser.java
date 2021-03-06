package com.jdroid.java.parser.zip;

import com.jdroid.java.exception.UnexpectedException;
import com.jdroid.java.parser.Parser;
import com.jdroid.java.utils.LoggerUtils;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Parser used to handle a file contained inside a zip.
 */
public class ZipFileParser implements Parser {
	
	private static final Logger LOGGER = LoggerUtils.getLogger(ZipFileParser.class);
	
	private Parser innerParser;
	private String fileName;
	
	/**
	 * @param innerParser {@link Parser} to use to handle the extracted file.
	 * @param fileName Name of the file to extract of the zip.
	 */
	public ZipFileParser(Parser innerParser, String fileName) {
		this.innerParser = innerParser;
		this.fileName = fileName;
	}
	
	/**
	 * @see com.jdroid.java.parser.Parser#parse(java.io.InputStream)
	 */
	@SuppressWarnings("resource")
	@Override
	public Object parse(InputStream inputStream) {
		
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		try {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				if (entry.getName().equals(fileName)) {
					LOGGER.debug("Starting to parse " + fileName + " file.");
					return innerParser.parse(zipInputStream);
				}
			}
		} catch (IOException e) {
			throw new UnexpectedException(e);
		}
		return null;
	}
	
	/**
	 * @see com.jdroid.java.parser.Parser#parse(java.lang.String)
	 */
	@Override
	public Object parse(String input) {
		return null;
	}
}
