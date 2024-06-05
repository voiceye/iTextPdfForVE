package com.sga.text.pdf;

public class PdfDRMEncrypt {
	public int m_nC1Key = 74102;
	  
	public int m_nC2Key = 12337;
	  
	public int m_nC3Key = 100;
	  
	public void setKey1(int nKey)
	{
		m_nC1Key = nKey;
	}
	
	public void setKey2(int nKey)
	{
		m_nC2Key = nKey;
	}
	  
	public PdfDRMEncrypt()
	{
	}
	  
	public PdfDRMEncrypt(int nKey1,int nKey2)
	{
		m_nC1Key = nKey1;
		m_nC2Key = nKey2;
	}
	  
	public void setKey(int nKey1,int nKey2,int nKey3)
	{
		m_nC1Key = nKey1;
		m_nC2Key = nKey2;
		m_nC3Key = nKey3;
	}
	  
	  
	public byte HexaByte(int nVal)
	{
		byte [] szHexaByte = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		if (nVal > 15)
		{
			nVal = 0;
		}
		return szHexaByte[nVal];
	}
	  
	public String ValueToHex(int szSrc[])
	{
		if (szSrc == null)
		return null;
		int nSrcLen = szSrc.length;
		byte szBuf[] = new byte[nSrcLen*2];
		for (int i=0;i<nSrcLen*2;i++)
		{
			szBuf[i] = 0;
		}
		for(int i=0;i<nSrcLen;i++)
		{
			szBuf[(i*2)+0] = HexaByte(((int)(szSrc[i]))/16);
			szBuf[(i*2)+1] = HexaByte(((int)(szSrc[i]))%16);
		}
		String sRet = new String(szBuf);
		return sRet;
	}
	  
	public byte[] HexToValue(byte[] szSrc)
	{
		int nLen = szSrc.length;
		byte[] szDest = new byte[nLen/2];
		char szChar[] = new char[2];
		for(int I = 0 ; I < nLen/2; I++)
		{
			szChar[0] = (char)szSrc[I*2];
			szChar[1] = (char)szSrc[I*2+1];
			byte btDest = (byte)HexToDecimal(szChar);
			int nDest = btDest < 0 ? ( Byte.MAX_VALUE + 1 ) * 2 + btDest : btDest;
			szDest[I] = (byte)nDest;
		}
		String sRet = new String(szDest);
		return szDest;
	}
	 
	public int MakeMyKey(byte btSrc[])
	{
		int nMakeMyKey = 0;
		int nLen = btSrc.length;
		System.out.println("[KKKK]" + "nLen:"+nLen);
		for(int i = 0 ; i < nLen ; i++)
		{
			int un =  btSrc[i]&0xff;
			int nRem = un % 10;
			int nPow = (int) Math.pow(10, ((i+1) % 7));
			nMakeMyKey = nMakeMyKey + (nRem * nPow);
			System.out.println("[KKKK]" + "btSrc[i]:" + btSrc[i] + " btSrc[i]&0xff:" + un + " nRem:"+nRem +" nPow:"+ nPow + "MakeMyKey:"+nMakeMyKey);
		}
		return nMakeMyKey;
	}
	
	public String Encrypt(byte btSrc[], int Key)
	{
		int nSrcLen = btSrc.length;
		long nKey2 = Key;
		int FirstResult[] = new int[nSrcLen];
		for (int i=0;i<nSrcLen;i++)
		{
			FirstResult[i] = 0;
		}
		int nLen = btSrc.length;
		for(int i = 0 ; i < nLen ; i++)
		{
			byte btByte = (byte)btSrc[i];
			int cSrc = btByte < 0 ? ( Byte.MAX_VALUE + 1 ) * 2 + btByte : btByte;
			long nXor = ((long)nKey2) / ((long)256);
			byte btTmp = (byte)(cSrc^nXor);
			FirstResult[i] = btTmp < 0 ? ( Byte.MAX_VALUE + 1 ) * 2 + btTmp : btTmp;
			//byte cFirstResult = (byte)FirstResult[i];
			//int nFirstResult = cFirstResult < 0 ? ( Byte.MAX_VALUE + 1 ) * 2 + cFirstResult : cFirstResult;
			//long nFirstResultKey = (long)(nFirstResult + nKey2);
			//nKey2 = (nFirstResultKey) * m_nC1Key + m_nC2Key;
		}
		String sRet = "";
		sRet = ValueToHex(FirstResult);
		return sRet;
	}
	  
	public int HexToDecimal(char[] szSrc)
	{
		int nRet = 0;
		int nLen = szSrc.length;
		for (int i=0;i<nLen;i++)
		{
			byte cChar = (byte)szSrc[i];
			nRet = nRet * 16;
			nRet += HexToDecimal(cChar);
		}
		return nRet;
	}
	  
	public int HexToDecimal(byte cChar)
	{
		if (cChar == 'A' || cChar == 'a')
			return 10;
		if (cChar == 'B' || cChar == 'b')
			return 11;
		if (cChar == 'C' || cChar == 'c')
			return 12;
		if (cChar == 'D' || cChar == 'd')
			return 13;
		if (cChar == 'E' || cChar == 'e')
			return 14;
		if (cChar == 'F' || cChar == 'f')
			return 15;
		return (cChar-48);
	}
	  
	public String Decrypt(byte szSrc[],int Key)
	{
		if (szSrc == null)
			return null;
		int nSrcLen = szSrc.length;
		byte FirstResult[] = new byte[nSrcLen/2];
		for (int i=0;i<nSrcLen/2;i++)
		{
			FirstResult[i] = 0;
		}
		int nLen = 0;
		FirstResult = HexToValue(szSrc);
		byte szFirstResult[] = FirstResult;
		byte szBuf[] = new byte[nSrcLen/2];
		for (int i=0;i<nSrcLen/2;i++)
		{
			szBuf[i] = 0;
		}
		byte szResult[] = new byte[nSrcLen/2];
		for (int i=0;i<nSrcLen/2;i++)
		{
			szResult[i] = 0;
		}
		int nKey = Key < 0 ? ( Integer.MAX_VALUE + 1 ) * 2 + Key : Key;
		long nKey2 = (long)nKey;
		for(int I = 0 ; I < nSrcLen/2 ; I++)
		{
			int nVal = szFirstResult[I] < 0 ? ( Byte.MAX_VALUE + 1 ) * 2 + szFirstResult[I] : szFirstResult[I];
			long nFirstResult = ((long)nVal);
			long nXor = (nKey2/(long)256);
			long nXorResult = nFirstResult ^ nXor;
			szResult[I] = (byte)(nXorResult);
			//byte cFirstResult = ((byte)szFirstResult[I]);
			//long cFirstResultKey = (nFirstResult + nKey2);
			//nKey2 = cFirstResultKey * m_nC1Key + m_nC2Key;
			//int a = 123;
		}
		String sRet = new String(szResult);
		return sRet;
	}
}
