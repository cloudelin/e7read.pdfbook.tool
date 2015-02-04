package com.koobe.tool.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.util.logging.resources.logging;

import com.koobe.tool.enums.ConvertedResultsKeyEnum;
import com.koobe.tool.ui.shell.KoobePDFBookToolShell;

public class PdfBatchConversionWorker implements Callable<List<Map<ConvertedResultsKeyEnum, String>>> {

	protected Logger log = LoggerFactory.getLogger(getClass());
	
	private KoobePDFBookToolShell shell;
	
	private String pdfSourceFolderPath;
	private Float zoom;
	private Float thumbnailZoom;
	private int threadPool;
	
	private List<Map<ConvertedResultsKeyEnum, String>> resultsMapList = new ArrayList<Map<ConvertedResultsKeyEnum,String>>();
	
	public PdfBatchConversionWorker(String pdfSourceFolderPath, Float zoom, Float thumbnailZoom, int threadPool, KoobePDFBookToolShell shell) {
		log.info("Create PDF batch conversion worker...");
		this.pdfSourceFolderPath = pdfSourceFolderPath;
		this.zoom = zoom;
		this.thumbnailZoom = thumbnailZoom;
		this.threadPool = threadPool;
		this.shell = shell;
	}
	
	public List<Map<ConvertedResultsKeyEnum, String>> call() {
		
		File sourceFolderFile = new File(pdfSourceFolderPath);
		
		List<File> files = (List<File>) FileUtils.listFiles(sourceFolderFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		
		for (final File file : files) {
			if (file.isFile() && FilenameUtils.getExtension(file.getName()).toLowerCase().equals("pdf")) {
				
				File finishedFile = new File(file.getAbsolutePath() + ".finished");
				
				if (finishedFile.exists()) {
					
					if (shell != null) {
						shell.executeOnUIThread(new Runnable() {
							public void run() {
								shell.getListFileResult().add("標示已完成，略過執行：" + file.getName());
							}
						});
					}
				} else {
					
					if (shell != null) {
						shell.executeOnUIThread(new Runnable() {
							public void run() {
								shell.getListFileResult().add("執行檔案轉換：" + file.getName());
							}
						});
					}
					
					long start = System.currentTimeMillis();
					PdfConversionWorker conversionWorker = new PdfConversionWorker(file, zoom, thumbnailZoom, threadPool, shell);
					Map<ConvertedResultsKeyEnum, String> result = conversionWorker.call();
					final double elapsed = (System.currentTimeMillis() - start) / 1000D;
					
					if (result != null) {
						resultsMapList.add(result);
						
						if (shell != null) {
							shell.executeOnUIThread(new Runnable() {
								public void run() {
									shell.getListFileResult().add("檔案轉換成功：" + file.getName() + " (" + elapsed + "s)");
								}
							});
						}
					} else {
						if (shell != null) {
							shell.executeOnUIThread(new Runnable() {
								public void run() {
									shell.getListFileResult().add("檔案轉換失敗：" + file.getName() + " (" + elapsed + "s)");
								}
							});
						}
					}
				}
			}
		}
		
		return resultsMapList;
	}
}
