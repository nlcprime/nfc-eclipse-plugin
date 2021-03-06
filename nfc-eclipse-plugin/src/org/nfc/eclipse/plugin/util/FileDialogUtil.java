/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ****************************************************************************/

package org.nfc.eclipse.plugin.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.nfc.eclipse.plugin.Activator;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;

/**
 * 
 * Utility for remembering used mime types and extensions, so that they can be
 * used in file dialogs
 * 
 * @author thomas
 * 
 */

public class FileDialogUtil {

	private static Set<String> mimeTypes;
	private static Set<String> exts;

	private static String lastMimeType = null;
	private static String lastExt = null;

	static {
		mimeTypes = Collections.synchronizedSet(new HashSet<String>());
		exts = Collections.synchronizedSet(new HashSet<String>());
	}

	public static boolean registerMimeType(String e) {
		return mimeTypes.add(e);
	}

	public static boolean registerExtension(String e) {
		return exts.add(e);
	}

	public static Set<String> getMimeTypes() {
		return mimeTypes;
	}

	public static Set<String> getExts() {
		return exts;
	}

	public static String getLastMimeType() {
		return lastMimeType;
	}

	public static void setLastMimeType(String lastMimeType) {
		FileDialogUtil.lastMimeType = lastMimeType;
	}

	public static void setMimeTypes(Set<String> mimeTypes) {
		FileDialogUtil.mimeTypes = mimeTypes;
	}

	public static String getLastExt() {
		return lastExt;
	}

	public static void setLastExt(String lastExt) {
		FileDialogUtil.lastExt = lastExt;
	}

	public static String open(FileDialog fileDialog, String mimeTypeHint) {

		List<String> filterNames = new ArrayList<String>();
		List<String> filterExtensions = new ArrayList<String>();

		if (mimeTypeHint != null) {
			// guess file ext from mime type
			Set<String> extensions = ExtensionMimeDetector.getExtensions(mimeTypeHint);
			if (extensions != null && !extensions.isEmpty()) {
				StringBuffer buffer = new StringBuffer();

				for (String ext : extensions) {
					buffer.append("*.");
					buffer.append(ext);
					buffer.append(";");
				}

				buffer.setLength(buffer.length() - 1);

				filterNames.add(mimeTypeHint);
				filterExtensions.add(buffer.toString());

				Activator.info("Default filter " + mimeTypeHint + " -> " + buffer);
			}
		}

		if (!exts.isEmpty()) {
			ExtensionMimeDetector extensionMimeDetector = new ExtensionMimeDetector();

			for (String ext : exts) {
				Collection<MimeType> extMimeTypes = extensionMimeDetector.getMimeTypesExt(ext);
				if(extMimeTypes != null && !extMimeTypes.isEmpty()) {
					StringBuffer buffer = new StringBuffer();

					for (MimeType extMimeType : extMimeTypes) {
						buffer.append(extMimeType.toString());
						buffer.append(", ");
					}

					buffer.setLength(buffer.length() - 2);
					
					filterNames.add(buffer.toString());
					filterExtensions.add(ext);
				} else {
					// add raw
					filterNames.add(ext.toUpperCase() + " file");
					filterExtensions.add("*." + ext);
				}
			}
		}
		

		if (!mimeTypes.isEmpty()) {
			for (String mimeType : mimeTypes) {
				if(mimeType.equals(mimeTypeHint)) continue;
				
				Set<String> extensions = ExtensionMimeDetector.getExtensions(mimeType);
				if (extensions != null && !extensions.isEmpty()) {
					StringBuffer buffer = new StringBuffer();

					for (String ext : extensions) {
						buffer.append("*.");
						buffer.append(ext);
						buffer.append(";");
					}

					buffer.setLength(buffer.length() - 1);

					filterNames.add(mimeType);
					filterExtensions.add(buffer.toString());
				}
			}
		}

		filterNames.add("All Files");

		// Set filter
		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterExtensions.add("*.*");
		} else {
			filterExtensions.add("*");
		}

		fileDialog.setFilterNames(filterNames.toArray(new String[filterNames
				.size()]));
		fileDialog.setFilterExtensions(filterExtensions
				.toArray(new String[filterNames.size()]));

		final String fileString = fileDialog.open();

		int filterIndex = fileDialog.getFilterIndex();
		if (filterIndex != -1 && filterIndex != filterNames.size() - 1) {
			Activator
					.info("Last mime type was " + filterNames.get(filterIndex));
			FileDialogUtil.setLastMimeType(filterNames.get(filterIndex));
		}

		return fileString;
	}

}
