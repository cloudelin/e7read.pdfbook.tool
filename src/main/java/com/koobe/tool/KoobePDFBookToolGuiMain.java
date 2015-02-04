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
		display = Display.getDefault();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final KoobePDFBookToolShell shell = KoobePDFBookToolShell.createShell();
		shell.open();
		
		new Thread(new Runnable() {
			public void run() {
				koobeApplication = KoobeApplication.getInstance();
				display.syncExec(new Runnable() {
					public void run() {
						shell.getBtnStart().setEnabled(true);
						shell.getLblSrvStatus().setText("就緒");
					}
				});
			}
		}).start();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		display.dispose();
		
		System.exit(0);
	}

}
