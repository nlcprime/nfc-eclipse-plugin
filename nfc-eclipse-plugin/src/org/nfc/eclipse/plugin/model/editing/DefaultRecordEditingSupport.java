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

package org.nfc.eclipse.plugin.model.editing;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfc.eclipse.plugin.model.NdefRecordModelNode;
import org.nfc.eclipse.plugin.model.NdefRecordModelPropertyList;
import org.nfc.eclipse.plugin.model.NdefRecordModelRecord;
import org.nfc.eclipse.plugin.model.NdefRecordType;
import org.nfc.eclipse.plugin.operation.NdefModelOperation;
import org.nfctools.ndef.Record;


public class DefaultRecordEditingSupport implements RecordEditingSupport {
	
	/**
	 * 
	 */
	protected final TreeViewer treeViewer;

	/**
	 * @param ndefRecordModelEditingSupport
	 */
	public DefaultRecordEditingSupport(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	protected final Object EMPTY_STRING = "";

	@Override
	public boolean canEdit(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelPropertyList) {
			return false;
		}
		return true;
	}
	
	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		if(node instanceof NdefRecordModelRecord) {
			String stringValue = (String)value;

			Record record = node.getRecord();
			
			if(!stringValue.equals(record.getKey())) {
				return new NdefModelOperation() {
					
					private Record record;
					private String previous;
					private String next;
					
					@Override
					public void revoke() {
						record.setKey(previous);
					}

					@Override
					public void execute() {
						record.setKey(next);
					}
					
					public NdefModelOperation init(Record record, String previous, String next) {
						this.record = record;
						
						this.previous = previous;
						this.next = next;
						
						return this;
					}
					
				}.init(record, record.getKey(), stringValue);
			}
		} else {
			throw new RuntimeException(node.getClass().getSimpleName());
		}
		return null;
	}
	
	@Override
	public Object getValue(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelRecord) {
			Record record = node.getRecord();
			
			return record.getKey();
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public CellEditor getCellEditor(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelRecord) {
			return new TextCellEditor(treeViewer.getTree());
		} else {
			throw new RuntimeException();
		}
	}	
	
	protected ComboBoxCellEditor getComboBoxCellEditor(Object[] values, boolean nullable) {

		String[] strings;
		if(nullable) {
			strings = new String[values.length + 1];
			strings[0] = "-";
			
			for(int i = 0; i < values.length; i++) {
				strings[1 + i] = values[i].toString();
			}
		} else {
			strings = new String[values.length];
			
			for(int i = 0; i < values.length; i++) {
				strings[i] = values[i].toString();
			}
		}
		
		return new ComboBoxCellEditor(treeViewer.getTree(), strings, ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION) {
			
		};
	}

	protected ComboBoxCellEditor getComboBoxCellEditor(NdefRecordType[] values, boolean nullable) {
		
		String[] strings = new String[values.length];
		for(int i = 0; i < values.length; i++) {
			strings[i] = values[i].getRecordLabel();
		}
		
		return getComboBoxCellEditor(strings, nullable);
	}
	
	protected int getIndex(Object[] values, Object value) {
		if(value != null) {
			for(int i = 0; i < values.length; i++) {
				if(values[i] == value || values[i].equals(value)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static byte[] load(String path) {
		File file = new File(path);

		int length = (int)file.length();
		
		byte[] payload = new byte[length];
		
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			DataInputStream din = new DataInputStream(in);
			
			din.readFully(payload);
			
			return payload;
		} catch(IOException e) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(shell, "Error", "Could not read file '" + file + "', reverting to previous value.");
			
			return null;
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}
	}

}