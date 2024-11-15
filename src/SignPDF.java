import java.util.Collections;
import java.util.Stack;
import java.math.BigInteger;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.*;
import java.security.*;
import java.security.cert.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfString;

public class SignPDF {

    public static void main(String[] args) {
        try {
            // 生成PKCS#1簽章
            String pfxFilePath = "C:\\Java\\client3.p12";
            String pfxPassword = "12345678";
            byte[] signature = generatePKCS1Signature(pfxFilePath, pfxPassword);

            // 打印簽章
            System.out.println("PKCS#1 Signature: " + new BigInteger(1, signature).toString(16));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] generatePKCS1Signature(String pfxFilePath, String pfxPassword) throws Exception {
        // 加載Bouncy Castle提供者
        Security.addProvider(new BouncyCastleProvider());

        // 從PFX檔案中讀取私鑰和證書
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(pfxFilePath), pfxPassword.toCharArray());

        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, pfxPassword.toCharArray());

        // 生成PKCS#1簽章
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update("data to sign".getBytes());
        return signature.sign();
    }

    public static byte[] generatePKCS7Signature(String pfxFilePath, String pfxPassword, byte[] data) throws Exception {
        // 加載Bouncy Castle提供者
        Security.addProvider(new BouncyCastleProvider());

        // 從PFX檔案中讀取私鑰和證書
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(pfxFilePath), pfxPassword.toCharArray());

        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, pfxPassword.toCharArray());
        java.security.cert.Certificate[] certificateChain = keyStore.getCertificateChain(alias);

        // 生成PKCS#7簽章
        CMSTypedData msg = new CMSProcessableByteArray(data);
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        ContentSigner sha256Signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(privateKey);
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()).build(sha256Signer, (X509Certificate) certificateChain[0]));
        gen.addCertificates(new JcaCertStore(Collections.singletonList(certificateChain[0])));
        CMSSignedData sigData = gen.generate(msg, false);

        return sigData.getEncoded();
    }

    // 使用OpenPDF Java 套件，將簽章值嵌入至PDF檔案中，不要使用iText 套件
    // 使用Bouncy Castle Java 套件，從PFX檔案中，取得私鑰進行PKCS#7簽章的方法 
    public static byte[] signPDF(byte[] pdfData, byte[] signature, boolean isPKCS7) throws Exception {

        // 加載Bouncy Castle提供者
        Security.addProvider(new BouncyCastleProvider());

        // 讀取PDF檔案
        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfData));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

        // 設置簽章外觀
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("Reason for signing");
        appearance.setLocation("Location of signing");
        appearance.setVisibleSignature("Signature1");

        if (isPKCS7) {
            // 生成PKCS#7簽章
            PdfPKCS7 sgn = new PdfPKCS7(null, null, null, "SHA256", "BC", false);
            sgn.setExternalDigest(signature, null, "RSA");
            appearance.preClose();
            sgn.setExternalDigest(signature, null, "RSA");
            PdfDictionary dic = new PdfDictionary();
            dic.put(PdfName.CONTENTS, new PdfString(sgn.getEncodedPKCS7()).setHexWriting(true));
            appearance.close(dic);
        } else {
            // 生成PKCS#1簽章
            PdfPKCS7 sgn = new PdfPKCS7(null, null, null, "SHA256", "BC", true);
            sgn.setExternalDigest(signature, null, "RSA");
            PdfDictionary dic = new PdfDictionary();
            dic.put(PdfName.CONTENTS, new PdfString(sgn.getEncodedPKCS7()).setHexWriting(true));
            appearance.close(dic);
        }

        stamper.close();
        reader.close();

        return os.toByteArray();
    }
}
