package com.sga.text.pdf;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.sga.text.Image;
import com.sga.text.Rectangle;
import com.sga.text.pdf.PdfSignatureAppearance.RenderingMode;


public class TrustCertificateTS {
	private String strLastErr;
	private	  boolean m_bDebug;
	private String m_logClass = "[Java TC Timestamp] ";
	public TrustCertificateTS() {
		//strLastErr = "";
		m_bDebug = false;
    }
	
	public int SetDebug(boolean bDebug)
	{
		m_bDebug = bDebug;
		return 11111;
	}
	
	
	public int ExecTCTimestamp(String src, String dest, 
			String ts_server, int ts_port, int ts_duration, String ts_policy_id, String ts_customer_id,
			String reason, String location,
			Rectangle rect, int page, String img) {
		int nRet = 11111;
	    strLastErr = "";
	    
	    try {
	    	if(m_bDebug) {
				System.out.println(m_logClass + "Start");
			}

	    	Date date = new Date();
	    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    	String strSgaTime = formatter.format(date);
	    	String[] array;
		    array = strSgaTime.split(" ");

			String strDate = array[0];
			String strTime = array[1];
	    	
			String my_cn = "sga_timestamp";
			String my_timestamp_date = strDate; //"2011/05/20"; 
			String my_timestamp_time = strTime; //"12:00"; 
			String my_timestamp_site = "KST"; 
			Certificate[] chain = null;

	    	
	        PdfReader reader = new PdfReader(src);
	        //PdfStamper stp = PdfStamper.createSignature(reader, new FileOutputStream(dest), '\0');
	        PdfStamper stp = PdfStamper.createSignature(reader, new FileOutputStream(dest), '\0', null, true);
	        PdfSignatureAppearance sap = stp.getSignatureAppearance();
	        sap.setVisibleSignature(rect, page, null);
	        sap.setSignDate(new GregorianCalendar());
	        sap.setCrypto(null, chain, null, null);

	        sap.setReason(reason);
	        sap.setLocation(location);
	        sap.setCN(my_cn);
	        sap.setTimestampDate(my_timestamp_date);
	        sap.setTimestampTime(my_timestamp_time);
	        sap.setTimestampSite(my_timestamp_site);
	        sap.setAcro6Layers(true);
	        sap.setRenderingMode(RenderingMode.TIMESTAMP);
	        Image image = Image.getInstance (img);
	        sap.setSignatureGraphic(image);

	        //PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_SHA1);
	        PdfSignature dic = new PdfSignature(PdfName.SGA_NPKITS, PdfName.ADBE_PKCS7_DETACHED);
	        dic.setDate(new PdfDate(sap.getSignDate()));
	        //dic.setName(PdfPKCS7.getSubjectFields((X509Certificate)chain[0]).getField("CN"));
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
	        if(m_bDebug) {
				System.out.println(m_logClass + "input_to_sign.length = "+input_to_sign.length);
			}
	        byte[] input_to_sign_hash = sha1(input_to_sign);
	        
	        byte [] signed_data;
	        //=============================================================================================================================
	        //signed_data = TSAJava.TSAGetToken(ts_server, ts_port, ts_duration, ts_policy_id, ts_customer_id, input_to_sign_hash);
	        String strTest = "sga test timestamp token!!!";
	        signed_data = strTest.getBytes();
	        //=============================================================================================================================
	        if(signed_data.length == 0)
			{
				if(m_bDebug) {
					System.out.println(m_logClass + "ExecTCTimestamp(82007) : ����Ȯ�� ������ �����Ͽ����ϴ�.");
		        }
				nRet = 82007;
				strLastErr = "����Ȯ�� ������ �����Ͽ����ϴ�.";
			}
			else
			{
		    	byte[] outc = new byte[csize];
		        //System.out.println(outc.length);
	
		        PdfDictionary dic2 = new PdfDictionary();
	
		        System.arraycopy(signed_data, 0, outc, 0, signed_data.length);
		        
		        dic2.put(PdfName.CONTENTS, new PdfString(outc).setHexWriting(true));
		        sap.close(dic2);
		        nRet = 11111;
			}
	    }
	    catch (Exception e) {
	    	if(m_bDebug) {
				System.out.println(m_logClass + "ExecTCTimestamp(82003) : Exception = " + e.getMessage());
	        }
	    	nRet = 82003;
	    	strLastErr = e.getMessage();
	        e.printStackTrace();
	    }
	    return nRet;
	}

	
	public String GetLastErr()
	{
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
	    for (byte b: bytes) {
	      builder.append(String.format("%02x", b));
	    }
	    return builder.toString();
	}
	
}
