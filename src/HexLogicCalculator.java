import java.util.Collections;
import java.util.Stack;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.io.File;
import java.io.FileInputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.*;
import java.security.*;
import java.security.cert.*;
import java.util.Collections;

public class HexLogicCalculator {

    public static void main(String[] args) {
        String expression = "(((NOT 'D6A4B5C7') AND (NOT ('DBD6A4B6' OR '98D6A8B5'))) XOR 'A5D7A4B5')";
        
        try {
            BigInteger result = evaluateExpression(expression);
            System.out.println("Final Result (in Hex): " + result.toString(16).toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
        }

        expression = "'82B2D6EB33E36DCF21B7DBB45C110A5482B2D6EB33E36DCF21B7DBB45C110A54' XOR 'F2AC013660BB67B3F0AA6F903DC736BEF2AC013660BB67B3F0AA6F903DC736BE'";

        try {
            BigInteger result = evaluateExpression(expression);
            System.out.println("Final Result2 (in Hex): " + result.toString(16).toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
        }

        BigInteger ret = add_number(new BigInteger("1", 16), new BigInteger("A", 16));
        System.out.println("Final Result3 (in Hex): " + ret.toString(16).toUpperCase());
    }

    //讀取PDF檔案內容，並顯示在console上
    public static void readPDF(String filePath) {
        try {
            // Load the PDF file
            PDDocument document = PDDocument.load(new File(filePath));
            // Create a PDFTextStripper object
            PDFTextStripper pdfStripper = new PDFTextStripper();
            // Extract text from PDF document
            String text = pdfStripper.getText(document);
            // Print the text on the console
            System.out.println(text);
            // Close the document
            document.close();
        } catch (Exception e) {
            e.printStackTrace();

    // Function to evaluate the entire expression
    public static BigInteger evaluateExpression(String expression) throws Exception {
        // Replace hex notation '0x...' and remove single quotes
        expression = expression.replaceAll("0x", "").replaceAll("'", "");
        System.out.println("Evaluating Expression: " + expression);

        // Resolve innermost parentheses first
        while (expression.contains("(")) {
            int start = expression.lastIndexOf('(');
            int end = expression.indexOf(')', start);
            String subExpr = expression.substring(start + 1, end);
            BigInteger subResult = evaluateSubExpression(subExpr);
            System.out.println("Sub-Expression: " + subExpr + " = " + subResult.toString(16).toUpperCase());
            expression = expression.substring(0, start) + subResult.toString(16).toUpperCase() + expression.substring(end + 1);
        }

        return evaluateSubExpression(expression);
    }

    // Function to evaluate a single expression without parentheses
    private static BigInteger evaluateSubExpression(String expr) throws Exception {
        String[] tokens = expr.split("\\s+");
        Stack<BigInteger> values = new Stack<>();
        Stack<String> ops = new Stack<>();

        for (String token : tokens) {
            if (isHex(token)) {
                values.push(new BigInteger(token, 16));
            } else if (token.equals("NOT")) {
                // Apply NOT directly to the next hex value
                ops.push(token);
            } else if (token.equals("AND") || token.equals("OR") || token.equals("XOR")) {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(token)) {
                    if (ops.peek().equals("NOT")) {
                        values.push(applyOp(ops.pop(), values.pop(), null));
                    } else {
                        values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                    }
                }
                ops.push(token);
            } else {
                throw new Exception("Invalid token: " + token);
            }
        }

        while (!ops.isEmpty()) {
            if (ops.peek().equals("NOT")) {
                values.push(applyOp(ops.pop(), values.pop(), null));
            } else {
                values.push(applyOp(ops.pop(), values.pop(), values.pop()));
            }
        }

        return values.pop();
    }

    // Helper function to check if a string is a hex number
    private static boolean isHex(String token) {
        return token.matches("^[A-Fa-f0-9]+$");
    }

    // Function to define the precedence of each operator
    private static int precedence(String op) {
        switch (op) {
            case "NOT":
                return 3;
            case "AND":
                return 2;
            case "OR":
            case "XOR":
                return 1;
            default:
                return -1;
        }
    }

    // Function to apply a logic operation
    // Function to apply a logic operation
  private static BigInteger applyOp(String op, BigInteger b, BigInteger a) throws Exception {
    if (b == null) throw new NullPointerException("Operand is missing for operation: " + op);

    BigInteger result; // Variable to hold the result
    switch (op) {
        case "NOT":
            // Apply NOT operation and mask to 32 bits (or adjust based on expected width)
            result = b.not().and(new BigInteger("FFFFFFFF", 16)); // Assuming 32-bit width
            System.out.println("NOT " + b.toString(16).toUpperCase() + " = " + result.toString(16).toUpperCase());
            return result;
        case "AND":
            result = a.and(b); // Apply AND operation
            System.out.println(a.toString(16).toUpperCase() + " AND " + b.toString(16).toUpperCase() + " = " + result.toString(16).toUpperCase());
            return result;
        case "OR":
            result = a.or(b); // Apply OR operation
            System.out.println(a.toString(16).toUpperCase() + " OR " + b.toString(16).toUpperCase() + " = " + result.toString(16).toUpperCase());
            return result;
        case "XOR":
            result = a.xor(b); // Apply XOR operation
            System.out.println(a.toString(16).toUpperCase() + " XOR " + b.toString(16).toUpperCase() + " = " + result.toString(16).toUpperCase());
            return result;
        default:
            throw new Exception("Unknown operator: " + op);
    }
  }
  private static BigInteger add_number(BigInteger a, BigInteger b) {
      return a.add(b);
  }

  //使用Bouncy Castle Java 套件，從PFX檔案中，取得私鑰進行PKCS#7簽章的方法 


public static byte[] signPKCS7(byte[] data, String pfxPath, String password) throws Exception {
    try {
        // 1. 加載 Bouncy Castle 提供者
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        // 2. 從PFX檔案載入私鑰和證書
        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        try (FileInputStream pfxStream = new FileInputStream(pfxPath)) {
            keyStore.load(pfxStream, password.toCharArray());
        }

        // 3. 取得私鑰和證書
        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

        // 4. 創建 PKCS#7 簽章產生器
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        
        // 5. 設定簽章演算法
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA")
            .setProvider("BC")
            .build(privateKey);

        // 6. 加入簽章者資訊
        generator.addSignerInfoGenerator(
            new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder()
                    .setProvider("BC")
                    .build()
            )
            .build(contentSigner, certificate)
        );

        // 7. 加入證書
        generator.addCertificates(new JcaCertStore(Collections.singletonList(certificate)));

        // 8. 產生簽章
        CMSTypedData cmsData = new CMSProcessableByteArray(data);
        CMSSignedData signedData = generator.generate(cmsData, true);

        // 9. 返回編碼後的簽章資料
        return signedData.getEncoded();
        
    } catch (Exception e) {
        throw new RuntimeException("PKCS#7 簽章失敗: " + e.getMessage(), e);
    }
}

}
