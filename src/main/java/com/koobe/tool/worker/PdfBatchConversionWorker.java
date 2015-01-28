package com.koobe.tool.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.koobe.tool.enums.ConvertedResultsKeyEnum;

public class PdfBatchConversionWorker implements Callable<List<Map<ConvertedResultsKeyEnum, String>>> {

	private String pdfSourceFolderPath;
	private Float zoom;
	private Float thumbnailZoom;
	private int threadPool;
	
	private List<Map<ConvertedResultsKeyEnum, String>> resultsMapList = new ArrayList<Map<ConvertedResultsKeyEnum,String>>();
	
	public PdfBatchConversionWorker(String pdfSourceFolderPath, Float zoom, Float thumbnailZoom, int threadPool) {
		this.pdfSourceFolderPath = pdfSourceFolderPath;
		this.zoom = zoom;
		this.thumbnailZoom = thumbnailZoom;
		this.threadPool = threadPool;
	}
	
	public List<Map<ConvertedResultsKeyEnum, String>> call() {
		
		File sourceFolderFile = new File(pdfSourceFolderPath);
		
		List<File> files = (List<File>) FileUtils.listFiles(sourceFolderFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		
		for (File file : files) {
			if (file.isFile() && FilenameUtils.getExtension(file.getName()).toLowerCase().equals("pdf")) {
				PdfConversionWorker conversionWorker = new PdfConversionWorker(file, zoom, thumbnailZoom, threadPool);
				Map<ConvertedResultsKeyEnum, String> result = conversionWorker.call();
				if (result != null) {
					resultsMapList.add(result);
				}
			}
		}
		
		return resultsMapList;
	}
}
