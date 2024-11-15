package com;
import java.io.*;
import java.security.*;
import sun.misc.*;
//import javax.swing.JOptionPane;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD2Digest;
import org.bouncycastle.crypto.digests.MD4Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA224Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;

//javac -classpath .;* sth\sec\Hash2.java
//java -classpath .;* sth.sec.Hash2

public class Hash2
{
  private boolean debug = true;
  byte[]  raw           = null;
  String AlgorithmName  = "SHA256"; //MD2(16Byte) ,MD4(16Byte) ,MD5(16Byte) ,SHA1(20Byte) SHA224(28Byte) , SHA256(32Byte) , SHA384(48Byte) , SHA512(64Byte)
  private Digest digest = null;
  public static void main(String[] args) throws Exception
  {
  	  //for(int i = 1 ; i <= 20 ; i++)
  	  {
	  	  long startTime=0 ,endTime=0;
			  startTime = System.currentTimeMillis();
	  	  Hash2 ha = new Hash2();
	  	  ha.SetAlgorithm("SHA256");
	  	  ha.Run("newimage");
	  	  endTime = System.currentTimeMillis();
	  	  System.out.println("HASH2["+(endTime-startTime) + " ms"+"] : "+ha.GetBase64());
	  	  ha = null;
	  	}
  }
  public Hash2()
  {
  
  }
  public void SetAlgorithm(String AlgorithmName)
  {
  	  this.AlgorithmName = AlgorithmName;
  	  if(this.AlgorithmName.equals("MD2"))
  	  {
  	  	digest = new MD2Digest();
  	  }
  	  else if(this.AlgorithmName.equals("MD4"))
  	  {
  	  	digest = new MD4Digest();
  	  }
  	  else if(this.AlgorithmName.equals("MD5"))
  	  {
  	  	digest = new MD5Digest();
  	  }
  	  else if(this.AlgorithmName.equals("SHA1"))
  	  {
  	  	digest = new SHA1Digest();
  	  }
  	  else if(this.AlgorithmName.equals("SHA224"))
  	  {
  	  	digest = new SHA224Digest();
  	  }
  	  else if(this.AlgorithmName.equals("SHA256"))
  	  {
  	  	digest = new SHA256Digest();
  	  }
  	  else if(this.AlgorithmName.equals("SHA384"))
  	  {
  	  	digest = new SHA384Digest();
  	  }
  	  else if(this.AlgorithmName.equals("SHA512"))
  	  {
  	  	digest = new SHA512Digest();
  	  }
  }
  public void SetDebug(boolean debug)
  {
  	  this.debug = debug;
  }
  public boolean GetDebug()
  {
  	  return this.debug ;
  }
  public int Run(String data)
  {
		      raw = new byte[digest.getDigestSize()];
		      digest.update(data.getBytes(), 0,data.length());
		      digest.doFinal(raw, 0);
			   	if(debug)System.out.println(this.AlgorithmName + " Hash ("+raw.length+"Byte) : "+byte2hex(raw));
			   	return 0;
  }
  public int Run(byte[] data)
  {
          raw = new byte[digest.getDigestSize()];
		      digest.update(data, 0,data.length);
		      digest.doFinal(raw, 0);
			   	if(debug)System.out.println(this.AlgorithmName + " Hash ("+raw.length+"Byte) : "+byte2hex(raw));
			   	return 0;
  }
  public String GetBase64()
  {
  	  if(raw.length >0)
  	  {
  	  		return base64Encoder(raw);
  	  }
  	  else
  	  {
  	      return "";	
  	  }
  } 
  public byte[] base64Decoder(String Base64TextData)
  {
  	  byte[] DataByteA={};
  	  if(!Base64TextData.equals(""))
  	  {
		      DataByteA = org.bouncycastle.util.encoders.Base64.decode(Base64TextData.getBytes());	
		  }
		  return DataByteA;
  }
  public String base64Encoder(byte[] data)
  {
  	  String Base64String = "";
  	  if(data != null)
  	  {
  	  	  byte[] Base64Array = org.bouncycastle.util.encoders.Base64.encode(data);
	  	  	if(Base64Array != null)
	  	  	{
	  	  		Base64String = new String(Base64Array);
	  	  	}
  	  }
      return Base64String;
  }
  
  String NISaveFile(String FilePath , byte[] Data)
  {
       	try
       	{
           File outcs= new File(FilePath);
           FileOutputStream ofcs = new FileOutputStream(outcs);
           ofcs.write(Data);
           ofcs.close();
        }
        catch( java.io.FileNotFoundException e ) {return e.getMessage();}
        catch( java.io.IOException e1 ) {return e1.getMessage() ;}
        return "0";  
  }   
    //--------------------NIReadFile Start---------------------------------
 public byte[] NIReadFile(String FilePath )
       {
       	byte[] buffer = null ;
       	try
       	  {
           File conFile =new File(FilePath);
	   FileInputStream in =new FileInputStream(conFile); 
           buffer=new byte[in.available()];  /// how many byte should be allocated !
           in.read(buffer);
           in.close();
           return buffer ;
          }
          catch( java.io.FileNotFoundException e ) {return buffer ;}
          catch( java.io.IOException e1 ) {return buffer ;}
        
       }             
//--------------------NIReadFile End---------------------------------     


 public String byte2hex(byte[] block) 
 {
        StringBuffer buf = new StringBuffer();
	      int len = block.length;

        for (int i = 0; i < len; i++) {
             byte2hex(block[i], buf);
	     if (i < len-1) {
		 buf.append(":");
	     }
        } 
        return buf.toString();
    }

//--------------------byte2hex Start----------------------------
static public void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                            '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }
//--------------------byte2hex End----------------------------

    

}