package com.koobe.tool;

import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.koobe.common.core.KoobeApplication;
import com.koobe.tool.ui.shell.KoobePDFBookToolShell;

public class KoobePDFBookToolGuiMain {
	
	protected static Logger log = LoggerFactory.getLogger(KoobePDFBookToolMain.class);

	public static KoobeApplication koobeApplication;
	
	static Display display;
	
	static {
		koobeApplication = KoobeApplication.getInstance();
		display = Display.getDefault();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		KoobePDFBookToolShell shell = KoobePDFBookToolShell.createShell();
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		display.dispose();
		
		System.exit(0);
	}

}
