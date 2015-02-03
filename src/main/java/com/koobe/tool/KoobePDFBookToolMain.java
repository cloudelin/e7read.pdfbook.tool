package com.koobe.tool;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.koobe.common.core.KoobeApplication;
import com.koobe.tool.enums.ConvertedResultsKeyEnum;
import com.koobe.tool.worker.MetadataCreationWorker;
import com.koobe.tool.worker.PdfBatchConversionWorker;

public class KoobePDFBookToolMain {
	
	protected static Logger log = LoggerFactory.getLogger(KoobePDFBookToolMain.class);

	static KoobeApplication koobeApplication;
	
	static {
		koobeApplication = KoobeApplication.getInstance();
	}
	
	static String pdfFolderPath = "F:\\pdf";
	static String bookBucket = "koobecloudbook";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PdfBatchConversionWorker conversionWorker = new PdfBatchConversionWorker(pdfFolderPath, 3F, 0.4F, 4, null);
		List<Map<ConvertedResultsKeyEnum, String>> results = conversionWorker.call();
		log.info(results.toString());
		
		MetadataCreationWorker creationWorker = new MetadataCreationWorker(results, koobeApplication, bookBucket, 10, null);
		Map<String, Boolean> resultsMap = creationWorker.call();
		log.info(resultsMap.toString());
		
		System.exit(0);
	}

}
