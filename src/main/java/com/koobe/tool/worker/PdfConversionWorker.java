package com.koobe.tool.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jmupdf.pdf.PdfDocument;
import com.koobe.tool.enums.ConvertedResultsKeyEnum;

public class PdfConversionWorker implements Callable<Map<ConvertedResultsKeyEnum, String>> {
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	protected ExecutorService executor;
	
	private File pdfFile;
	private Float zoom;
	private Float thumbnailZoom;
	
	public PdfConversionWorker(File pdfFile, Float zoom, Float thumbnailZoom, int threadPool) {
		this.pdfFile = pdfFile;
		this.zoom = zoom;
		this.thumbnailZoom = thumbnailZoom;
		executor = Executors.newFixedThreadPool(threadPool);
	}

	public Map<ConvertedResultsKeyEnum, String> call() {
		
		Map<ConvertedResultsKeyEnum, String> result = null;
		
		String baseFolderName = FilenameUtils.getBaseName(pdfFile.getName());
		
		byte[] byteData;
		try {
			byteData = FileUtils.readFileToByteArray(pdfFile);
			PdfDocument pdfDocument = new PdfDocument(byteData);
			
			File tempFolder = new File(pdfFile.getParentFile().getAbsolutePath() + File.separator + baseFolderName);
			if (!tempFolder.exists()) {
				tempFolder.mkdir();
			}
			File fullsizePagesFolder = new File(tempFolder.getAbsolutePath() + File.separator + "page_fullsize");
			if (!fullsizePagesFolder.exists()) {
				fullsizePagesFolder.mkdir();
			}
			File thumbnailPagesFolder = new File(tempFolder.getAbsolutePath() + File.separator + "page_thumbnail");
			if (!thumbnailPagesFolder.exists()) {
				thumbnailPagesFolder.mkdir();
			}
			
			List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
			
			for (int i=1; i<=pdfDocument.getPageCount(); i++) {
				String outputImagePath = fullsizePagesFolder.getAbsolutePath() + File.separator + "page_" + String.format("%04d", i) + ".jpg";
				String outputThumbnailPath = thumbnailPagesFolder.getAbsolutePath() + File.separator + "page_" + String.format("%04d", i) + ".jpg";
				PageRenderingWorker renderingWorker = new PageRenderingWorker(pdfDocument, i, zoom, outputImagePath);
				PageRenderingWorker renderingThumbWorker = new PageRenderingWorker(pdfDocument, i, thumbnailZoom, outputThumbnailPath);
				tasks.add(renderingWorker);
				tasks.add(renderingThumbWorker);
			}
			
			boolean success = true;
			
			List<Future<Boolean>> futures = executor.invokeAll(tasks);
			for (Future<Boolean> future : futures) {
				if (!future.get()) {
					success = false;
				}
			}
						
			if (success) {
				result = new HashMap<ConvertedResultsKeyEnum, String>();
				result.put(ConvertedResultsKeyEnum.PDF_FILE_PATH, pdfFile.getAbsolutePath());
				result.put(ConvertedResultsKeyEnum.IMAGES_FOLDER_PATH, tempFolder.getAbsolutePath());
			}
			
			pdfDocument.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			executor.shutdown();
		}
		
		return result;
	}
}
