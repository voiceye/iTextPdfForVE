package com.sga.text.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.sga.text.Image;
import com.sga.text.Rectangle;
import com.sga.text.pdf.PdfSignatureAppearance.RenderingMode;

public class TrustCertificateSign {
	private String strLastErr;
	private boolean m_bDebug;
	private String m_logClass = "[Java TC Sign] ";

	public TrustCertificateSign() {
		// strLastErr = "";
		m_bDebug = false;
	}

	public int SetDebug(boolean bDebug) {
		m_bDebug = bDebug;
		return 11111;
	}

	private Provider _prov = null;

	// Return Message [ErrorCode & Result]
	private void TRACE(String msg) {
		System.out.println(msg); //
	}

	public byte[] ExecSGAextractSigData(String src, String dest, int sigNum, String reason, String location,
			Rectangle rect, int page, int visible, String img) {
		byte[] input_to_sign_hash = null;
		try {
			if (m_bDebug) {
				System.out.println(m_logClass + "Start");
			}

			String my_cn = "sga_signature_" + sigNum;

			Certificate[] chain = null;

			PdfReader reader = new PdfReader(src);
			// PdfStamper stp = PdfStamper.createSignature(reader, new
			// FileOutputStream(dest), '\0');
			PdfStamper stp = PdfStamper.createSignature(reader, new FileOutputStream(dest), '\0', null, true);
			PdfSignatureAppearance sap = stp.getSignatureAppearance();
			if (visible == 1)
				sap.setVisibleSignature(rect, page, null);

			sap.setSignDate(new GregorianCalendar());
			sap.setCrypto(null, chain, null, null);

			sap.setReason(reason);
			sap.setLocation(location);
			sap.setCN(my_cn);
			sap.setAcro6Layers(true);
			sap.setRenderingMode(RenderingMode.GRAPHIC);
			Image image = Image.getInstance(img);
			// sap.setImage(image);
			sap.setSignatureGraphic(image);
			// sap.setRenderingMode(RenderingMode.SGASTAMP);
			PdfSignature signer = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_SHA1);
			signer.setDate(new PdfDate(sap.getSignDate()));
			// dic.setName(PdfPKCS7.getSubjectFields((X509Certificate)chain[0]).getField("CN"));
			signer.setName(my_cn);
			if (sap.getReason() != null)
				signer.setReason(sap.getReason());
			if (sap.getLocation() != null)
				signer.setLocation(sap.getLocation());
			sap.setCryptoDictionary(signer);
			int csize = 4000;
			HashMap exc = new HashMap();
			exc.put(PdfName.CONTENTS, new Integer(csize * 2 + 2));
			sap.preClose(exc);

			InputStream in = sap.getRangeStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int next = in.read();
			while (next > -1) {
				bos.write(next);
				next = in.read();
			}
			bos.flush();
			byte[] input_to_sign = bos.toByteArray();
			if (m_bDebug) {
				System.out.println(m_logClass + "input_to_sign.length = " + input_to_sign.length);
			}
			input_to_sign_hash = sha1(input_to_sign);

			byte[] outc = new byte[csize];
			// System.out.println(outc.length);
			byte[] signed_data = outc;
			PdfDictionary dic2 = new PdfDictionary();

			System.arraycopy(signed_data, 0, outc, 0, signed_data.length);

			dic2.put(PdfName.CONTENTS, new PdfString(outc).setHexWriting(true));
			sap.close(dic2);

			// sap.close(dic);
		} catch (Exception e) {
			if (m_bDebug) {
				System.out.println(m_logClass + "ExecSign(82003) : Exception = " + e.getMessage());
			}
			strLastErr = e.getMessage();
			e.printStackTrace();
		}

		return input_to_sign_hash;
	}

	public void writeToFile(String filename, byte[] pData)
	{
	    if(pData == null){
	        return;
	    }
	    int lByteArraySize = pData.length;
	    System.out.println(filename);
	    try{

	        File lOutFile = new File("C:/Users/ghpark/Downloads/"+filename);
	        FileOutputStream lFileOutputStream = new FileOutputStream(lOutFile);
	        lFileOutputStream.write(pData);
	        lFileOutputStream.close();
	    }catch(Throwable e){
	        e.printStackTrace(System.out);
	    }
	}
	
	public int embeddedSignature(String src, String dest, String sig, int sig_num) {
		// 서명 정보 넣는구간..
		int csize = 4000;
		byte[] outc = new byte[csize];
		byte[] sigbyte = new java.math.BigInteger(sig, 16).toByteArray();
		//writeToFile("sigdata" ,sigbyte);
		String my_cn = "sga_signature_" + sig_num;
		// String my_cn = "FoxitKoreaSign";
		try {
			PdfReader reader = new PdfReader(src);

			//PdfStamper stamper = PdfStamper.createSignature(reader, new FileOutputStream(dest), '\0');
			
			//PdfStamper stamper = PdfStamper.createSignature(reader, new FileOutputStream(dest), '\0', null, true);
			//PdfSignatureAppearance sap = stamper.getSignatureAppearance();
			//PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
			//AcroFields af = stamper.getAcroFields();
			

			AcroFields af = reader.getAcroFields();
			ArrayList names = af.getSignatureNames();
			
			// af.getBlankSignatureNames();
			for (int k = 0; k < names.size(); ++k) {
				String name = (String) names.get(k);
				if (m_bDebug) {
					System.out.println(m_logClass + "ExecValidation : Signature name : " + name);
				}
				System.out.println("Signature name: " + name);
				System.out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
				System.out.println("Document revision: " + af.getRevision(name) + " of " + af.getTotalRevisions());
				if (af.signatureCoversWholeDocument(name)) {
					PdfDictionary v = af.getSignatureDictionary(name);
					System.out.println(v.getAsString(PdfName.NAME));
					// contents...
					if (my_cn.equals(v.getAsString(PdfName.NAME).toString())) {
						//System.arraycopy(sigbyte, 0, outc, 0, sigbyte.length);
						//v.put(PdfName.CONTENTS, new PdfString(outc).setHexWriting(true));
						PdfArray b = v.getAsArray(PdfName.BYTERANGE);
						//System.out.println(v.getAsNumber(PdfName.BYTERANGE));
						File myFile = new File (dest);
		                //Create the accessor with read-write access.
		                RandomAccessFile accessor = new RandomAccessFile (myFile, "rws");
		                int start, length, next, nCnt = 0;
		                start = b.getAsNumber(0).intValue();
		                length = b.getAsNumber(1).intValue();
		                accessor.seek(start+length+1);
		                accessor.writeBytes(sig);
		                accessor.close();
					
					}
				}

			}
			//stamper.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		int nRet = 11111;

		return nRet;
	}
/*
	public int ExecTCSign(String src, String dest, String cert_path, String key_path, String key_password,
			String reason, String location, Rectangle rect, int page, int visible, String img) {
		int nRet = 11111;
		// strLastErr = "";

		try {
			if (m_bDebug) {
				System.out.println(m_logClass + "Start");
			}

			String strPlanText = "This is the original text.";
			byte[] sigdata = null;

			String my_cn = "sga_signature";
			Certificate[] chain = null;

			String strSignedData = "";
			// base64���ڵ��� ����
			sigdata = strSigniture(strPlanText);
			strSignedData = new String(sigdata);
			if (m_bDebug) {
				System.out.println(m_logClass + strSignedData);
				PdfReader reader = new PdfReader(src);
				// PdfStamper stp = PdfStamper.createSignature(reader, new
				// FileOutputStream(dest), '\0');
				PdfStamper stp = PdfStamper.createSignature(reader, new FileOutputStream(dest), '\0', null, true);
				PdfSignatureAppearance sap = stp.getSignatureAppearance();
				if (visible == 1)
					sap.setVisibleSignature(rect, page, null);

				sap.setSignDate(new GregorianCalendar());
				sap.setCrypto(null, chain, null, null);

				sap.setReason(reason);
				sap.setLocation(location);
				sap.setCN(my_cn);
				sap.setAcro6Layers(true);
				sap.setRenderingMode(RenderingMode.GRAPHIC);
				Image image = Image.getInstance(img);
				// sap.setImage(image);
				sap.setSignatureGraphic(image);
				// sap.setRenderingMode(RenderingMode.SGASTAMP);
				PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_SHA1);
				dic.setDate(new PdfDate(sap.getSignDate()));
				
				// dic.setName(PdfPKCS7.getSubjectFields((X509Certificate)chain[0]).getField("CN"));
				dic.setName(my_cn);
				if (sap.getReason() != null)
					dic.setReason(sap.getReason());
				if (sap.getLocation() != null)
					dic.setLocation(sap.getLocation());
				sap.setCryptoDictionary(dic);
				int csize = 4000;
				HashMap exc = new HashMap();
				exc.put(PdfName.CONTENTS, new Integer(csize * 2 + 2));
				sap.preClose(exc);

				InputStream in = sap.getRangeStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int next = in.read();
				while (next > -1) {
					bos.write(next);
					next = in.read();
				}
				bos.flush();
				byte[] input_to_sign = bos.toByteArray();
				if (m_bDebug) {
					System.out.println(m_logClass + "input_to_sign.length = " + input_to_sign.length);
				}

				byte[] input_to_sign_hash = sha1(input_to_sign);

				byte[] signed_data;
				signed_data = binarySigniture(input_to_sign_hash);

				if (signed_data.length == 0) {
					if (m_bDebug) {
						System.out.println(m_logClass + "ExecSign(82007) : signeddata length 0.");
					}

					nRet = 82005;
					strLastErr = "CQJava.CQJSignData���� ������ �����Ͽ����ϴ�.";
				} else {
					if (signed_data.length == 0) {
						if (m_bDebug) {
							System.out.println(m_logClass + "ExecSign(82007) :  signeddata length 0.");
						}
						nRet = 82007;
						strLastErr = " signeddata length 0.";
					} else {
						byte[] outc = new byte[csize];
						// System.out.println(outc.length);

						PdfDictionary dic2 = new PdfDictionary();

						System.arraycopy(signed_data, 0, outc, 0, signed_data.length);

						dic2.put(PdfName.CONTENTS, new PdfString(outc).setHexWriting(true));
						sap.close(dic2);
						nRet = 11111;
					}
					// }
				}
			}
		} catch (Exception e) {
			if (m_bDebug) {
				System.out.println(m_logClass + "ExecSign(82003) : Exception = " + e.getMessage());
			}
			nRet = 82003;
			strLastErr = e.getMessage();
			e.printStackTrace();
		}
		return nRet;
	}
*/
	public String GetLastErr() {
		return strLastErr;
	}

	public static byte[] sha1(byte[] msg) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(msg);

		return md.digest();
	}

	public static byte[] sha256(byte[] msg) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(msg);

		return md.digest();
	}

	public static byte[] sha256(String msg) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(msg.getBytes());

		return md.digest();
	}

	public static String bytesToHex1(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (byte b : bytes) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}

}
