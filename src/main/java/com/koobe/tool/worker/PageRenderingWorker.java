package com.koobe.tool.worker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jmupdf.enums.ImageType;
import com.jmupdf.interfaces.Page;
import com.jmupdf.page.PageRenderer;
import com.jmupdf.pdf.PdfDocument;

public class PageRenderingWorker implements Callable<Boolean> {
	
	protected Logger log = LoggerFactory.getLogger(getClass());

	private PdfDocument pdfDocument;
	private Page page;
	private Integer pageIdx;
	private String outputPath;
	private Float zoom;
	
	public PageRenderingWorker(PdfDocument pdfDocument, Integer pageIdx, Float zoom, String outputPath) {
		this.pdfDocument = pdfDocument;
		this.pageIdx = pageIdx;
		this.outputPath = outputPath;
		this.zoom = zoom;
	}
	
	public Boolean call() {
		
		Boolean result = true;

		File outputFile = new File(outputPath);
		
		if (!outputFile.exists()) {
			PageRenderer pageRenderer = null;
			try {
				this.page = pdfDocument.getPage(pageIdx);
				log.info("Rendering page to {}", outputPath);
				pageRenderer = new PageRenderer(page, zoom, 0, ImageType.IMAGE_TYPE_RGB);
				pageRenderer.run();
				BufferedImage bim = pageRenderer.getImage();
				ImageIO.write(bim, "jpg", outputFile);
			} catch (Exception e) {
				result = false;
				e.printStackTrace();
			} finally {
				if (pageRenderer != null) {
					pageRenderer.dispose();
				}
				page.dispose();
			}
		} else {
			log.info("File {} has already exists, skipped", outputPath);
		}
		
		return result;
	}
}
