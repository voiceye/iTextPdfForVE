package com.sga.text.pdf.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sga.text.pdf.PRStream;
import com.sga.text.pdf.PdfDictionary;
import com.sga.text.pdf.PdfName;
import com.sga.text.pdf.PdfObject;
import com.sga.text.pdf.PdfReader;
import com.sga.text.pdf.PdfStream;

/**
 * @File      : ExtractBarcodeFromPdf
 * @Author    : chyoon
 * @Date      : 2023.04.20
 * @Desc  	  :  전자문서에 내장된 2D바코드(SGA_BCD), VE바코드(SGA_VED)를 BMP이미지로 추출하는 기능. (정부24, 유니닥스 모바일 연동을 위해 사용)
 */

public class ExtractBarcodeFromPdf {
	
	/**
	* @Name : getSgaObjInfo
	* @Desc : PDF에 내장된 2D바코드(SGA_BCD), VE바코드(SGA_VED) 필드에 대해 바코드 이름과, 실제 데이터가 있는 obj번호 추출
	* 			1. object의 타입이 dictionary인 것 중에 "F"항목의 값이 "SGA_BCD", "SGA_VED"인 object를 찾는다. 
	* 			2. 해당 object에서 dictionary형태의 "EF"항목을 찾고, 해당 dictionary에서 "F"항목의 값을 가져온다.
	* 
	* @param pdfPath : 바코드가 내장된 PDF파일 경로 
	* @return 		 : 바코드 이름과, 실제 데이터 obj번호를 담고 있는 HashMap (ex, {SGA_BCD_1=20, SGA_VED_1=21})
	*/
	public Map<String, String> getSgaObjInfo(String pdfPath) {
	    LinkedHashMap<String, String> sgaObjInfo = new LinkedHashMap();
	    PdfReader reader = null;
		try {
			reader = new PdfReader(pdfPath);
			for (int i = 0; i < reader.getXrefSize(); i++) {
		        PdfObject pdfObj = reader.getPdfObject(i);
		    	String field = getFieldOfDictionaryFromObj(pdfObj, "F");
		        if (field != null && (field.contains("SGA_BCD") || field.contains("SGA_VED"))) {
		            PdfDictionary dictionary = ((PdfDictionary)pdfObj).getAsDict(new PdfName("EF"));
		            String value = dictionary.get(new PdfName("F")).toString().split(" ")[0];
		            sgaObjInfo.put(field, value);
		        }
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) reader.close();
		}
	    return sgaObjInfo;
	}
	
	/**
	* @Name : getFieldOfDictionaryFromObj
	* @Desc : dictionary object로부터 특정 필드의 값을 추출하여 반환한다. 
	* 
	* @param object
	* @param fieldName : 찾으려는 필드 이름
	* @return
	*/
	private String getFieldOfDictionaryFromObj(PdfObject object, String fieldName) {
	    if (object == null || !object.isDictionary()) {
	        return null;
	    }
	    PdfDictionary dictionary = (PdfDictionary) object;
	    PdfObject fObj = dictionary.get(new PdfName(fieldName));
	    if (fObj == null) {
	        return null;
	    }
	    return fObj.toString();
	}
	
	
	/**
	* @Name : extractSGABarcode
	* @Desc : PDF에 내장된 2D바코드(SGA_BCD), VE바코드(SGA_VED)를 추출하여 BMP로 저장하고 바코드 이름과, BMP 저장 위치를 반환.
	* 
	* @param filePath 	: 바코드가 내장된 PDF파일
	* @param sgaObjInfo	: ex) {SGA_BCD_1=D:\BarcodeExtract\test1\지적도_20.bmp, SGA_VED_1=D:\BarcodeExtract\test1\지적도_21.bmp}
	* @return
	*/
	public Map<String, String> extractSGABarcode(String filePath, Map<String, String> sgaObjInfo) {
	    LinkedHashMap<String, String> sgaBarcodeMap = new LinkedHashMap();
	    PdfReader reader = null;
		try {
			reader = new PdfReader(filePath);
			for (int i = 0; i < reader.getXrefSize(); i++) {
		        PdfObject pdfobj = reader.getPdfObject(i);
		        if (pdfobj == null || !pdfobj.isStream()) {
		            continue;
		        }
		        PdfStream stream = (PdfStream) pdfobj;
		        PdfObject pdfsubtype = stream.get(PdfName.TYPE);
		        if (pdfsubtype != null && pdfsubtype.toString().indexOf("SGAEmbeddedFile") > -1) { // dictionary의 Type이 "SGAEmbeddedFile"인 것을 찾는다.	            
		        	String decImgFilePath = String.format("%s_%d.bmp", filePath.replace(".pdf", ""), i);
		        	
		        	byte[] decImgData = decodeStream(stream, decImgFilePath, false); // true/false: 원본 stream데이터 저장 여부
		        	if(decImgData == null) {
		        		return null;
		        	}
		        	
		            String searchKey = findSearchKey(sgaObjInfo, Integer.toString(i));
		            sgaBarcodeMap.put(searchKey, decImgFilePath);
		        }
		    } // end for
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) reader.close();
		}
	    return sgaBarcodeMap;
	}
	
	
	/**
	* @Name : decodeStream
	* @Desc : stream 데이터를 FlateDecode하고 반환한다. 
	* 			filePath를 지정한 경우 디코딩한 데이터를 파일로 저장한다. saveStream을 true로 설정한 경우 stream데이터를 파일로 저장한다.
	* 
	* @param stream		
	* @param filePath   : 디코딩한 데이터를 저장할 경로.
	* @param saveStream : stream 데이터 저장 여부.
	* @return 			  
	*/
	private byte[] decodeStream(PdfStream stream, String filePath, boolean saveStream) {
		byte[] streamData = null;
		byte[] decStreamData = null;
		try {
			streamData = PdfReader.getStreamBytesRaw((PRStream) stream);
			String strFilter = stream.get(PdfName.FILTER).toString();
			if(strFilter.indexOf("FlateDecode") > -1) {
				decStreamData = decodeFlateData(streamData);
			} else { // To-Do : FlateDecode가 아닌 경우에 대한 처리 필요
				decStreamData = streamData;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(decStreamData != null && filePath != null && !filePath.equals("")) {
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(new File(filePath));
				out.write(decStreamData);
				out.flush();		
				if(saveStream) {
					out = new FileOutputStream(new File(filePath + ".stream"));
					out.write(streamData);
					out.flush();
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				if(out != null) try {out.close();} catch(IOException e){e.printStackTrace();}
			}
		}
		return decStreamData;
	}
	
	/**
	* @Name : decodeFlateData
	* @Desc : stream데이터를 FlateDecode하여 반환
	* 
	* @param encData : stream 데이터
	* @return 		 : FlateDecode된 stream 데이터
	*/
	private byte[] decodeFlateData(byte[] encData) {
	    return PdfReader.FlateDecode(encData);
	}
	
	/**
	* @Name : findSearchKey
	* @Desc : HashMap의 Value로부터 Key를 도출하는 함수
	* 			바코드 데이터(stream) obj만으로는 "2D바코드/VE바코드/몇 페이지 바코드" 인지를 알 수 없기 때문에 
	* 			바코드 데이터(stream) obj와 바코드 이름(SGA_BCD_1, SGA_VED_1)을 매칭하기 위해 사용한다.
	* 
	* @param sgaObjInfo  : getSgaObjInfo 함수의 반환값
	* @param searchValue : 
	* @return
	*/
	private String findSearchKey(Map<String, String> sgaObjInfo, String searchValue) {
	    for (Map.Entry<String, String> entry : sgaObjInfo.entrySet()) {
	        if (entry.getValue().equals(searchValue)) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
		
} // end class
