package com.koobe.tool.worker;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jmupdf.interfaces.Page;
import com.jmupdf.page.PageRect;
import com.jmupdf.page.PageText;
import com.jmupdf.pdf.PdfDocument;


public class ExtractPageTextWorker implements Callable<Boolean> {
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	private PdfDocument pdfDocument;
	private Integer pageIdx;
	private String outputPath;
	
	private PdfConversionWorker parent;
	
	public ExtractPageTextWorker(PdfDocument pdfDocument, Integer pageIdx, String outputPath, PdfConversionWorker parent) {
		this.pdfDocument = pdfDocument;
		this.pageIdx = pageIdx;
		this.outputPath = outputPath;
		this.parent = parent;
	}

	public Boolean call() {

		log.info("Extracting text on page {}", pageIdx);
		
		Boolean result = false;
		
		Page page = null;
		try {
			page = pdfDocument.getPage(pageIdx);
			
			StringBuilder textBuilder = new StringBuilder();
			PageText[] texts = page.getTextSpan(new PageRect(0, 0, page.getWidth(), page.getHeight()));
			if (texts != null) {
				for (PageText text : texts) {
					textBuilder.append(text.getText());
					textBuilder.append("\n\n");
				}
			}
			
			File file = new File(outputPath);
			FileUtils.writeStringToFile(file, textBuilder.toString());
			
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (page != null) {
				page.dispose();
			}
		}
		
		if (this.parent != null) {
			this.parent.increaseProgress();
		}
		
		return result;
	}
}
