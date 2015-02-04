package com.koobe.tool.ui.shell;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.koobe.tool.KoobePDFBookToolGuiMain;
import com.koobe.tool.enums.ConvertedResultsKeyEnum;
import com.koobe.tool.worker.MetadataCreationWorker;
import com.koobe.tool.worker.PdfBatchConversionWorker;

/**
 * Application server configure shell
 * @author Cloude
 * @since 2011-12-1
 */
public class KoobePDFBookToolShell extends Shell {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public static Display DISPLAY;
	private static KoobePDFBookToolShell SHELL; 
	
	private String batchConversionFolderPath;
	
	private Button btnOpenFileDialog;
	private Text textConversionImageSize;
	private Text textConversionThumbnailSize;
	private Text textConversionThread;
	private Text textUploadingThread;
	
	private List listFileResult;
	private ProgressBar progressBar;
	
	private Label lblSrvStatus;
	
	private Button btnStart;
	
	private Button btnClose;
	
	private float imageSize;
	private float thumbnailSize;
	private int convertThread;
	private int uploadThread;
	
	public static KoobePDFBookToolShell createShell() {
		if (SHELL == null || SHELL.isDisposed()) {
			DISPLAY = Display.getDefault();
			SHELL = new KoobePDFBookToolShell();
		}
		return SHELL;
	}
	
	public KoobePDFBookToolShell() {
		super(DISPLAY, SWT.SHELL_TRIM);
		setLayout(new FillLayout(SWT.VERTICAL));
		
		Composite tMainComposite = new Composite(this, SWT.NONE);
		tMainComposite.setLayout(new GridLayout(1, false));
		
		Composite tCompositeTabFolder = new Composite(tMainComposite, SWT.NONE);
		tCompositeTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		tCompositeTabFolder.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		TabFolder tPropertiesTabFolder = new TabFolder(tCompositeTabFolder, SWT.NONE);
		
		TabItem tTabGeneral = new TabItem(tPropertiesTabFolder, SWT.NONE);
		tTabGeneral.setText("資料夾批次轉檔");		
		
		Composite tGeneralComposite = new Composite(tPropertiesTabFolder, SWT.NONE);
		tTabGeneral.setControl(tGeneralComposite);
		tGeneralComposite.setLayout(new GridLayout(1, false));
		
		Composite tCompositeForm = new Composite(tGeneralComposite, SWT.NONE);
		tCompositeForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		tCompositeForm.setLayout(new GridLayout(2, false));
		
		Label lblPdfFolderPath = new Label(tCompositeForm, SWT.NONE);
		lblPdfFolderPath.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPdfFolderPath.setText("資料夾:");
		
		btnOpenFileDialog = new Button(tCompositeForm, SWT.NONE);
		btnOpenFileDialog.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnOpenFileDialog.setText("選擇資料夾...");

		Label lblConversionImageSize = new Label(tCompositeForm, SWT.NONE);
		lblConversionImageSize.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblConversionImageSize.setText("圖片比例:");
		textConversionImageSize = new Text(tCompositeForm, SWT.BORDER);
		textConversionImageSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textConversionImageSize.setText("3");
		
		Label lblConversionThumbnailSize = new Label(tCompositeForm, SWT.NONE);
		lblConversionThumbnailSize.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblConversionThumbnailSize.setText("縮圖比例:");
		textConversionThumbnailSize = new Text(tCompositeForm, SWT.BORDER);
		textConversionThumbnailSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textConversionThumbnailSize.setText("0.4");
		
		Label lblConversionThread = new Label(tCompositeForm, SWT.NONE);
		lblConversionThread.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblConversionThread.setText("轉檔執行緒:");
		textConversionThread = new Text(tCompositeForm, SWT.BORDER);
		textConversionThread.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textConversionThread.setText("4");
		
		Label lblUploadingThread = new Label(tCompositeForm, SWT.NONE);
		lblUploadingThread.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUploadingThread.setText("上傳執行緒:");
		textUploadingThread = new Text(tCompositeForm, SWT.BORDER);
		textUploadingThread.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textUploadingThread.setText("10");
		
		Label lblBlank1 = new Label(tCompositeForm, SWT.NONE);
		lblBlank1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblBlank1.setText(" ");
		
		Label lblExecutionResult = new Label(tCompositeForm, SWT.NONE);
		lblExecutionResult.setText("執行結果:");
		new Label(tCompositeForm, SWT.NONE);
		
		
		
		Composite tCompositeResult = new Composite(tCompositeForm, SWT.NONE);
		GridLayout gl_tCompositeResult = new GridLayout(1, false);
		gl_tCompositeResult.marginWidth = 0;
		gl_tCompositeResult.marginHeight = 0;
		tCompositeResult.setLayout(gl_tCompositeResult);
		tCompositeResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		listFileResult = new List(tCompositeResult, SWT.BORDER | SWT.V_SCROLL);
		listFileResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

//		listFileResult.setItems(new String[]{"a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c"});
		
		
		Composite tCompositeProgress = new Composite(tCompositeForm, SWT.NONE);
		GridLayout gl_tCompositeProgress = new GridLayout(1, false);
		gl_tCompositeProgress.marginWidth = 0;
		gl_tCompositeProgress.marginHeight = 0;
		tCompositeProgress.setLayout(gl_tCompositeProgress);
		tCompositeProgress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		progressBar = new ProgressBar(tCompositeProgress, SWT.SMOOTH);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		
//		Label lblServiceStatus = new Label(tCompositeForm, SWT.NONE);
//		lblServiceStatus.setText("狀態:");
		
		lblSrvStatus = new Label(tCompositeForm, SWT.NONE);
		lblSrvStatus.setAlignment(SWT.RIGHT);
		lblSrvStatus.setText("程式正在初始化，請稍候...");
		lblSrvStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		
		Composite tCompositeSrvCtrlButton = new Composite(tCompositeForm, SWT.NONE);
		tCompositeSrvCtrlButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		
		RowLayout rl_tCompositeSrvCtrlButton = new RowLayout(SWT.HORIZONTAL);
		rl_tCompositeSrvCtrlButton.marginLeft = 0;
		rl_tCompositeSrvCtrlButton.center = true;
		tCompositeSrvCtrlButton.setLayout(rl_tCompositeSrvCtrlButton);
		
		btnStart = new Button(tCompositeSrvCtrlButton, SWT.NONE);		
		btnStart.setLayoutData(new RowData(75, -1));
		btnStart.setText("啟動");
		btnStart.setEnabled(false);
		
		Composite tButtonComposite = new Composite(tMainComposite, SWT.NONE);
		GridLayout gl_tButtonComposite = new GridLayout(1, false);
		gl_tButtonComposite.marginWidth = 0;
		tButtonComposite.setLayout(gl_tButtonComposite);
		GridData gd_tButtonComposite = new GridData(GridData.FILL_BOTH);
		gd_tButtonComposite.grabExcessVerticalSpace = false;
		gd_tButtonComposite.grabExcessHorizontalSpace = false;
		gd_tButtonComposite.horizontalAlignment = SWT.RIGHT;
		gd_tButtonComposite.verticalAlignment = SWT.BOTTOM;
		tButtonComposite.setLayoutData(gd_tButtonComposite);
		
		Composite tButtonLayoutComposite = new Composite(tButtonComposite, SWT.NONE);
		GridLayout gl_tButtonLayoutComposite = new GridLayout(1, true);
		gl_tButtonLayoutComposite.marginWidth = 0;
		gl_tButtonLayoutComposite.marginHeight = 0;
		tButtonLayoutComposite.setLayout(gl_tButtonLayoutComposite);
		tButtonLayoutComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		btnClose = new Button(tButtonLayoutComposite, SWT.NONE);
		GridData gd_btnClose = new GridData(SWT.FILL, SWT.RIGHT, false, false, 1, 1);
		gd_btnClose.widthHint = 100;
		btnClose.setLayoutData(gd_btnClose);
		btnClose.setText("結束");
		
		
		
		/*
		 * Method setItemValues() Must before method: createContents()
		 */
		createContents();
		addListener();
	}
		
	protected void addListener() {
		
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {				
				SHELL.dispose();
			}
		});
		
		btnOpenFileDialog.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {				
				DirectoryDialog dialog = new DirectoryDialog(SHELL);
				batchConversionFolderPath = dialog.open();
				if (batchConversionFolderPath != null) {
					btnOpenFileDialog.setText(batchConversionFolderPath);
				}
			}
		});
		
		btnStart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startConverter();
			}
		});
	}
	
	
	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("E7READ PDF轉檔上傳工具");
		setSize(800, 700);
		
//		Image tImgMenuAppSrv;
//		try {
//			tImgMenuAppSrv = new Image(DISPLAY, ApplicationParameter.getMenuImagePathSetting());
//			setImage(tImgMenuAppSrv);
//		} catch (Exception e) {
//			
//		}
	}
	
	@Override
	public void close() {
		super.close();
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void executeOnUIThread(Runnable runnable) {
		DISPLAY.syncExec(runnable);
	}
	
	public List getListFileResult() {
		return listFileResult;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public Label getLblSrvStatus() {
		return lblSrvStatus;
	}

	public Button getBtnStart() {
		return btnStart;
	}


	protected void startConverter() {
		
		new Thread(new Runnable() {
			public void run() {
				
				if (batchConversionFolderPath != null && !batchConversionFolderPath.trim().equals("")) {
					
					executeOnUIThread(new Runnable() {
						public void run() {
							lblSrvStatus.setText("執行中");
							btnStart.setEnabled(false);
							listFileResult.removeAll();
							
							imageSize = Float.valueOf(textConversionImageSize.getText());
							thumbnailSize = Float.valueOf(textConversionThumbnailSize.getText());
							convertThread = Integer.valueOf(textConversionThread.getText());
							uploadThread = Integer.valueOf(textUploadingThread.getText());
						}
					});
					
					PdfBatchConversionWorker batchConversionWorker = new PdfBatchConversionWorker(batchConversionFolderPath, 
							imageSize, thumbnailSize, convertThread, SHELL);
					java.util.List<Map<ConvertedResultsKeyEnum, String>> batchResults = batchConversionWorker.call();
					
					MetadataCreationWorker creationWorker = new MetadataCreationWorker(batchResults, KoobePDFBookToolGuiMain.koobeApplication, "koobecloudbook", uploadThread, SHELL);
					creationWorker.call();
					
					executeOnUIThread(new Runnable() {
						public void run() {
							lblSrvStatus.setText("執行完成");
							btnStart.setEnabled(true);
						}
					});
				} else {
					
					executeOnUIThread(new Runnable() {
						public void run() {
							lblSrvStatus.setText("請先選擇資料夾");
						}
					});
				}
				
			}
		}).start();
		
		
		
		
	}
}
