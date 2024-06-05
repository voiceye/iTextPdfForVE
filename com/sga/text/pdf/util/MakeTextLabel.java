/**
 * @File      : MakeTextLabel
 * @Author    : leegh
 * @Date      : 2024.02.20
 * @Desc  	  : PDF 파일에 텍스트 라벨을 추가하는 기능을 제공하는 클래스.
 */
package com.sga.text.pdf.util;

import com.sga.text.BaseColor;
import com.sga.text.DocumentException;
import com.sga.text.pdf.BaseFont;
import com.sga.text.pdf.PdfContentByte;
import com.sga.text.pdf.PdfReader;
import com.sga.text.pdf.PdfStamper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MakeTextLabel {
	private String path;
	private String srcFileName;
	private String destFileName;
	private String fontPath;
	private BaseColor fontColor;
	private float fontSize;
	private int alignment;
	private int textPositionX;
	private int textPositionY;
	private String inputText;

    /**
    * @Name : MakeTextLabel
    * @Desc : 클래스 생성자. PDF 파일의 경로, 파일 이름, 텍스트 라벨, 폰트 설정 등을 초기화한다.
    * 
    * @param path : PDF 파일이 위치한 경로
    * @param srcFileName : 원본 PDF 파일 이름
    * @param destFileName : 텍스트 라벨이 추가된 후의 PDF 파일 이름
    * @param inputText : PDF에 추가할 텍스트 라벨
    * @param fontPath : 텍스트 라벨의 폰트 경로
    * @param fontColor : 텍스트 라벨의 폰트 색상
    * @param fontSize : 텍스트 라벨의 폰트 크기
    * @param alignment : 텍스트 라벨의 정렬 방향
    * @param textPositionX : 텍스트 라벨의 x 좌표
    * @param textPositionY : 텍스트 라벨의 y 좌표
    */
	public MakeTextLabel(String path, String srcFileName, String destFileName, String inputText, 
			String fontPath,BaseColor fontColor, float fontSize, int alignment, int textPositionX, int textPositionY) throws IOException {
		if (!isPathExists(path)) {
			throw new IOException("Path does not exist: " + path);
		}
		this.path = path;
		this.srcFileName = srcFileName;
		this.destFileName = destFileName;
		this.inputText = inputText;
		this.fontPath = fontPath;
		this.fontColor = fontColor;
		this.fontSize = fontSize;
		this.alignment = alignment;
		this.textPositionX = textPositionX;
		this.textPositionY = textPositionY;
	}

	private boolean isPathExists(String path) {
		File file = new File(path);
		return file.exists();
	}

	public void setFontSettings(String fontPath, BaseColor fontColor, float fontSize) {
		this.fontPath = fontPath;
		this.fontColor = fontColor;
		this.fontSize = fontSize;
	}

	public void setTextPosition(int textPositionX, int textPositionY) {
		this.textPositionX = textPositionX;
		this.textPositionY = textPositionY;
	}

	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}
    /**
    * @Name : createPdfWithTextLabel
    * @Desc : 설정된 경로의 PDF 파일에 텍스트 라벨을 추가하고 새 PDF 파일을 생성한다.
    *
    * @return : PDF 파일 생성 성공 여부. 성공하면 true, 실패하면 false를 반환한다.
    */
	public boolean createPdfWithTextLabel() {
		try {
			PdfReader reader = new PdfReader(path + srcFileName + ".pdf");
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(path + destFileName + ".pdf"));
			PdfContentByte canvas = stamper.getOverContent(1);

			addTextToPdf(canvas, inputText, textPositionX, textPositionY, alignment, fontSize, fontColor, fontPath);

			stamper.close();
			reader.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (DocumentException e) {
			e.printStackTrace();
			return false;
		}
	}
    /**
    * @Name : addTextToPdf
    * @Desc : 주어진 설정에 따라 PDF의 캔버스에 텍스트를 추가한다.
    *
    * @param canvas : PDF의 캔버스
    * @param inputText : 추가할 텍스트
    * @param x : 텍스트의 x 좌표
    * @param y : 텍스트의 y 좌표
    * @param alignment : 텍스트의 정렬
    * @param fontSize : 텍스트의 폰트 크기
    * @param color : 텍스트의 색상
    * @param fontPath : 텍스트의 폰트 경로
    */
	private void addTextToPdf(PdfContentByte canvas, String inputText, int x, int y, int alignment, float fontSize,
			BaseColor color, String fontPath) throws DocumentException, IOException {
		BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		canvas.beginText();
		canvas.setFontAndSize(baseFont, fontSize);
		canvas.setColorFill(color);
		canvas.showTextAligned(alignment, inputText, x, y, 0);
		canvas.endText();
	}

}