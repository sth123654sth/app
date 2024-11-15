import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfSignature;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfDate;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

public class PDFSigner {

    public static void signPDFWithPKCS7(String inputPdfPath, String outputPdfPath, byte[] pkcs7Signature) throws IOException, DocumentException, GeneralSecurityException {
        System.out.println("PKCS#7 Signature (Hex): " + bytesToHex(pkcs7Signature));
        CMSSignedData signedData;
        try {
            signedData = new CMSSignedData(pkcs7Signature);
        } catch (CMSException e) {
            throw new GeneralSecurityException("Error processing PKCS#7 signature", e);
            
        }

        Store<X509CertificateHolder> certStore = signedData.getCertificates();
        Collection<X509CertificateHolder> certCollection = certStore.getMatches(null);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        System.out.println("certCollection.size(): " + certCollection.size());
        Certificate[] certificateChain = new Certificate[certCollection.size()];
        int i = 0;
        X509Certificate cert = null;
        for (X509CertificateHolder certHolder : certCollection) {
            cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
            System.out.println(" ****** Certificate CN: " + cert.getSubjectX500Principal().getName());
            certificateChain[i++] = cert;
        }
        // 1. 讀取輸入 PDF 文件
        PdfReader pdfReader = new PdfReader(inputPdfPath);
        
        // 2. 建立輸出文件流
        FileOutputStream outputStream = new FileOutputStream(outputPdfPath);
        
        // 3. 初始化 PdfStamper 來編輯 PDF 文件
        PdfStamper stamper = PdfStamper.createSignature(pdfReader, outputStream, '\0');
        Calendar signDate = Calendar.getInstance();
        stamper.setEnforcedModificationDate(signDate);
        // 4. 設定簽章外觀
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        
        PdfDictionary dic = new PdfDictionary();
          dic.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);
          dic.put(PdfName.M, new PdfDate(signDate));
        appearance.setCryptoDictionary(dic);
        appearance.setSignDate(signDate);
        appearance.setReason("Document signed with PKCS#7 signature.");
        appearance.setLocation("Location Example");
        appearance.setVisibleSignature(new Rectangle(100, 100, 300, 150), 1, "Signature1");  // 定義簽名欄位名稱
        appearance.setCryptoDictionary(new PdfDictionary());
        appearance.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
        appearance.setContact("STH");
        Map<PdfName, Integer> exc = new HashMap<>();
        // 6. 計算簽名長度
        int contentSize = pkcs7Signature.length * 2 + 2;
        exc.put(PdfName.CONTENTS, contentSize);

        // 5. 設定簽章對象
        PdfSignature pdfSignature = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
        pdfSignature.setReason(appearance.getReason());
        pdfSignature.setLocation(appearance.getLocation());
        pdfSignature.setCert(cert.getEncoded());
        pdfSignature.setDate(new PdfDate());
        appearance.setCryptoDictionary(pdfSignature);

        // 7. 填入外部的 PKCS#7 簽章 certificateChain
        PdfPKCS7 sgn = new PdfPKCS7(null, certificateChain, null,"SHA256", "BC", false);
        sgn.setExternalDigest(pkcs7Signature, null, "RSA");
        //byte[] encodedSig = sgn.getEncodedPKCS7(null, appearance.getSignDate(), null, null);

        byte[] encodedSig = sgn.getEncodedPKCS7();
        //System.out.println("Encoded Signature (Hex): " + bytesToHex(encodedSig));
        byte[] paddedSig = new byte[contentSize];
        System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
        dic.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));

        encodedSig = sgn.getEncodedPKCS7(null, appearance.getSignDate(), null, null);
        appearance.setExternalDigest(encodedSig, null, "RSA");
        // 6. 計算簽名長度
        //int contentSize = pkcs7Signature.length * 2 + 2;
        //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //HashMap<PdfName, Integer> exc = new HashMap<PdfName, Integer>();
        //exc.put(PdfName.CONTENTS, contentSize);
        //appearance.preClose(exc);
        appearance.preClose(exc);
        
      
        
        
        appearance.close(dic);
        

        // 關閉 stamper 以完成 PDF 簽署
        stamper.close();
        pdfReader.close();
    }

    public static void main(String[] args) {
        try {
            // 外部提供的 PKCS#7 簽章 (byte array)
            String pfxFilePath = "C:\\Java\\pdf\\test\\hank_123123.pfx";
            String pfxPassword = "123123";
            byte[] pdfData = Files.readAllBytes(Paths.get("C:\\Java\\pdf\\test\\1.pdf"));

            // Generate PKCS#7 signature
            byte[] pkcs7Signature = generatePKCS7Signature(pfxFilePath, pfxPassword, pdfData);
            signPDFWithPKCS7("C:\\Java\\pdf\\test\\1.pdf", "C:\\Java\\pdf\\test\\2.pdf", pkcs7Signature);
            System.out.println("PDF 簽署完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    public static byte[] generatePKCS7Signature(String pfxFilePath, String pfxPassword, byte[] data) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyStore keyStore = loadKeyStore(pfxFilePath, pfxPassword);
        PrivateKey privateKey = getPrivateKey(keyStore, pfxPassword);
        Certificate[] certificateChain = keyStore.getCertificateChain(keyStore.aliases().nextElement());
        for (Certificate cert : certificateChain) {
            if (cert instanceof X509Certificate) {
            X509Certificate x509Cert = (X509Certificate) cert;
            System.out.println("Certificate CN: " + x509Cert.getSubjectX500Principal().getName());
            }
        }

        // Generate PKCS#7 signature
        CMSTypedData msg = new CMSProcessableByteArray(data);
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        ContentSigner sha256Signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(privateKey);
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()).build(sha256Signer, (X509Certificate) certificateChain[0]));
        gen.addCertificates(new JcaCertStore(Collections.singletonList(certificateChain[0])));
        CMSSignedData sigData = gen.generate(msg, false);

        return sigData.getEncoded();
    }
    private static KeyStore loadKeyStore(String pfxFilePath, String pfxPassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(pfxFilePath)) {
            keyStore.load(fis, pfxPassword.toCharArray());
        }
        return keyStore;
    }

    private static PrivateKey getPrivateKey(KeyStore keyStore, String pfxPassword) throws Exception {
        String alias = keyStore.aliases().nextElement();
        return (PrivateKey) keyStore.getKey(alias, pfxPassword.toCharArray());
    }
}
