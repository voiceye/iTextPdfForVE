package com.sga.text.pdf;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.sga.text.Image;
import com.sga.text.Rectangle;
import com.sga.text.pdf.PdfSignatureAppearance.RenderingMode;

public class TrustCertificateBL {
	private String strLastErr;
	private	  boolean m_bDebug;
	private String m_logClass = "[Java TC BlockChain] ";
	public TrustCertificateBL() {
		strLastErr = "";
		m_bDebug = false;
    }
	
	public int SetDebug(boolean bDebug)
	{
		m_bDebug = bDebug;
		return 11111;
	}
	

	public int ExecTCBL(String src, String dest, String tcbl_server, int tcbl_port, 
			String reason, String location,  
			Rectangle rect, int page, String img) {
		int nRet = 11111;
	    strLastErr = "";
	    
	    try {
		
			SimpleDateFormat mydate = new SimpleDateFormat("yyyy/MM/dd"); 
			SimpleDateFormat mytime = new SimpleDateFormat("HH:mm");
			Date currentTime = new Date ();
			String strDate = mydate.format(currentTime);
			String strTime = mytime.format(currentTime);
			
			
			String contactInfo = tcbl_server + ":" + Integer.toString(tcbl_port);
			String my_blockchain_name = "sga_blockchain";
			String my_blockchain_date = strDate; //"2011/05/20"; 
			String my_blockchain_time = strTime; //"12:00"; 
			String my_blockchain_site = "TCBL"; 
			Certificate[] chain = null;
	    	
	        PdfReader reader = new PdfReader(src);
	        //PdfStamper stp = PdfStamper.createSignature(reader, new FileOutputStream(dest), '\0');
	        PdfStamper stp = PdfStamper.createSignature(reader, new FileOutputStream(dest), '\0', null, true);
	        PdfSignatureAppearance sap = stp.getSignatureAppearance();
	        sap.setVisibleSignature(rect, page, null);
	        sap.setSignDate(new GregorianCalendar());
	        sap.setCrypto(null, chain, null, null);

	        
	        sap.setContact(contactInfo);
	        sap.setReason(reason);
	        sap.setLocation(location);
	        sap.setBlockchainName(my_blockchain_name);
	        sap.setBlockchainDate(my_blockchain_date);
	        sap.setBlockchainTime(my_blockchain_time);
	        sap.setBlockchainSite(my_blockchain_site);
	        sap.setAcro6Layers(true);
	        
	        Image image = Image.getInstance (img);
	        sap.setSignatureGraphic(image);
	        
	        sap.setRenderingMode(RenderingMode.BLOCKCHAIN);
	        PdfSignature dic = new PdfSignature(PdfName.SGA_TCBL, PdfName.SGA_TCBL_SHA256);
	        dic.setDate(new PdfDate(sap.getSignDate()));

	        dic.setName(my_blockchain_name);
	        if (sap.getReason() != null)
	            dic.setReason(sap.getReason());
	        if (sap.getLocation() != null)
	            dic.setLocation(sap.getLocation());
	        if (sap.getContact() != null)
	            dic.setContact(sap.getContact());

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
				System.out.println(m_logClass + "input_to_sign.length : " + input_to_sign.length);
	        }
	        //System.out.println(input_to_sign.length);
	        
	        byte[] input_hash = sha256(input_to_sign);

	        // input data hash
	        String hash = bytesToHex1(input_hash); //input_to sign => hash
	        
	        //create unique document number
	        String doc_num = "";  //pdf_document + unique key
	        
	        //get PDFID
	        PdfDictionary trailer = reader.getTrailer();
	        if(trailer.contains(PdfName.ID))
	        {
	        	PdfArray ids = (PdfArray) trailer.get(PdfName.ID);
	            PdfString original = ids.getAsString(0);
	            PdfString modified = ids.getAsString(1);
	            System.out.println(bytesToHex1(original.getBytes()));
	            System.out.println(bytesToHex1(modified.getBytes()));
	            doc_num = bytesToHex1(original.getBytes());
	            
	            //==================================================================
	            //�����Ҷ� original ���� ����ȴ�. ���� �ʿ� =========================== 
	        }
	        
	        //request value
	        String request_value = doc_num + "|" + hash;
	        
	        //get blockchain token(���)   ==> using blackchain restful
	        String response_value = request_value;
	        
	        String token = response_value;
			
			byte[] signed_data = token.getBytes();
			//byte[] signed_data = timeStampToken2.getMessageImprint();
			if(m_bDebug) {
				System.out.println(m_logClass + "signed_data.length : " + signed_data.length);
	        }
			if(signed_data.length == 0)
			{
				if(m_bDebug) {
					System.out.println(m_logClass + "getMessageImprint(89999) : error");
		        }
				nRet = 89999;
				strLastErr = "error";
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
				System.out.println(m_logClass + "ExecTCBL(82043) : Exception = " + e.getMessage());
	        }
	    	nRet = 82043;
	    	strLastErr = e.getMessage();
	        e.printStackTrace();
	    }
	    return nRet;
	}

	
	public String GetLastErr()
	{
		return strLastErr;
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
