package com.hpsworldwide.powercard.otp.lib.img;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.aztec.AztecReader;
import com.google.zxing.aztec.AztecWriter;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.DataMatrixReader;
import com.google.zxing.datamatrix.DataMatrixWriter;
import com.google.zxing.oned.Code128Reader;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Reader;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.pdf417.PDF417Reader;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.krysalis.barcode4j.BarcodeDimension;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.impl.pdf417.PDF417Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

/**
 * uses com.google.zxing & org.krysalis.barcode4j<br />
 * alternative for adding format : uk.org.okapibarcode<br />
 *
 * @see http://barcode4j.sourceforge.net/2.1/embedding.html &
 * https://github.com/zxing/zxing
 * @author (c) HPS Solutions
 */
public class BarCodeUtils {

    private static final int STANDARD_DOTS_PER_INCH_RESOLUTION = 300;

    // note : you can test non confidential data with http://www.askapache.com/online-tools/base64-image-converter/
    // uses both implementations
    public static void writePngImage(String message, int width, int height, BarcodeFormat barcodeFormat, String charset, OutputStream outputStream) throws IOException {
        switch (barcodeFormat) {
            case CODE_128: {
                // charset ignored in this version
                writePngImage(message, width, height, new Code128Bean(), outputStream);
                break;
            }
            case PDF_417: {
                // charset ignored in this version
                // TODO : upgrade to 2.1 to be able to add encoding support
                // see source @ https://sourceforge.net/p/barcode4j/svn/371/tree/trunk/barcode4j/src/java/org/krysalis/barcode4j/impl/pdf417/PDF417Bean.java#l84
                PDF417Bean pdf417Bean = new PDF417Bean();
                // pdf417Bean.setEncoding(StandardCharsets.UTF_8.toString());
                writePngImage(message, width, height, pdf417Bean, outputStream);
                break;
            }
            case DATA_MATRIX: {
                // charset ignored in this version
                writePngImage(message, width, height, new DataMatrixBean(), outputStream);
                break;
            }
            case QR_CODE: {
                // done using com.google.zxing
                // could also be done using org.krysalis.barcode4j.impl.qr
                try {
                    writePngImage(message, width, height, new QRCodeWriter(), charset, outputStream);
                } catch (WriterException ex) {
                    throw new IOException(ex);
                }
                break;
            }
            case AZTEC: {
                try {
                    writePngImage(message, width, height, new AztecWriter(), charset, outputStream);
                } catch (WriterException ex) {
                    throw new IOException(ex);
                }
                break;
            }
            case EAN13: {
                try {
                    writePngImage(message, width, height, new EAN13Writer(), charset, outputStream);
                } catch (WriterException ex) {
                    throw new IOException(ex);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("barcode format " + barcodeFormat + " not supported");
            }
        }
    }

    // note : you can test non confidential data with http://www.askapache.com/online-tools/base64-image-converter/
    // uses both implementations
    public static byte[] toPngImage(String message, int width, int height, BarcodeFormat barcodeFormat, String charset) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            writePngImage(message, width, height, barcodeFormat, charset, bos);
            return bos.toByteArray();
        }
    }

    public enum BarcodeFormat {

        CODE_128,
        PDF_417,
        QR_CODE,
        /**
         * flashcode ;p
         */
        DATA_MATRIX,
        AZTEC,
        EAN13
    }

    /**
     * uses com.google.zxing<br />
     * http://stackoverflow.com/questions/2489048/qr-code-encoding-and-decoding-using-zxing
     */
    private static void writePngImage(String message, int width, int height, Writer writer, String charset, OutputStream outputStream) throws WriterException, IOException {
        // TODO : add EncodeHintType.ERROR_CORRECTION in Map<EncodeHintType,?> hints in parameters
        Map<EncodeHintType, String> hints = new EnumMap<>(EncodeHintType.class);
        if (charset != null && !charset.isEmpty()) {
            hints.put(EncodeHintType.CHARACTER_SET, charset);
        }
        com.google.zxing.BarcodeFormat barcodeFormat;
        if (writer instanceof Code128Writer) {
            barcodeFormat = com.google.zxing.BarcodeFormat.CODE_128;
        } else if (writer instanceof PDF417Writer) {
            barcodeFormat = com.google.zxing.BarcodeFormat.PDF_417;
        } else if (writer instanceof QRCodeWriter) {
            barcodeFormat = com.google.zxing.BarcodeFormat.QR_CODE;
        } else if (writer instanceof AztecWriter) {
            barcodeFormat = com.google.zxing.BarcodeFormat.AZTEC;
        } else if (writer instanceof EAN13Writer) {
            barcodeFormat = com.google.zxing.BarcodeFormat.EAN_13;
        } else if (writer instanceof DataMatrixWriter) {
            barcodeFormat = com.google.zxing.BarcodeFormat.DATA_MATRIX;
        } else {
            throw new IllegalArgumentException("barcode format not found for writer " + writer);
        }
        BitMatrix matrix = writer.encode(message, barcodeFormat, width, height, hints);
        // try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
        //    return outputStream.toByteArray();
        // }
    }

    /**
     * uses org.krysalis.barcode4j<br>
     */
    private static void writePngImage(String message, int width, int height, AbstractBarcodeBean barcodeBean, OutputStream outputStream) throws IOException {
        barcodeBean.setFontSize(0); // hide the text in the barcode
        BitmapCanvasProvider bitmapCanvasProvider = new BitmapCanvasProvider(STANDARD_DOTS_PER_INCH_RESOLUTION, BufferedImage.TYPE_BYTE_GRAY, true, 0);
        bitmapCanvasProvider.establishDimensions(new BarcodeDimension(width, height));
        barcodeBean.generateBarcode(bitmapCanvasProvider, message);
        bitmapCanvasProvider.finish();
        BufferedImage barcodeImage = bitmapCanvasProvider.getBufferedImage();
        ImageUtils.toPNG(barcodeImage, outputStream);
    }

    public static String toPngImageHtmlTag(String message, int width, int height, BarcodeFormat barcodeFormat, String charset) throws IOException {
        return new StringBuilder()
                .append("<img src=\"data:image/png;base64,")
                .append(Base64.encodeBase64String(toPngImage(message, width, height, barcodeFormat, charset)))
                .append("\" />").toString();
    }

    public static String toPngImageBase64(String message, int width, int height, BarcodeFormat barcodeFormat, String charset) throws IOException {
        return Base64.encodeBase64String(toPngImage(message, width, height, barcodeFormat, charset));
    }

    public static DecodedBarcodeResult decodeBarcode(BufferedImage image, BarcodeFormat barcodeFormat, String charset) throws IOException, NotFoundException, FormatException, ChecksumException {
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.TRY_HARDER, true);
        if (charset != null && !charset.isEmpty()) {
            hints.put(DecodeHintType.CHARACTER_SET, charset);
        }
        // hints.put(DecodeHintType.PURE_BARCODE, true);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        Reader reader;
        switch (barcodeFormat) {
            case PDF_417: {
                reader = new PDF417Reader();
                break;
            }
            case QR_CODE: {
                reader = new QRCodeReader();
                break;
            }
            case AZTEC: {
                reader = new AztecReader();
                break;
            }
            case CODE_128: {
                reader = new Code128Reader();
                break;
            }
            case DATA_MATRIX: {
                reader = new DataMatrixReader();
                break;
            }
            case EAN13: {
                reader = new EAN13Reader();
                break;
            }
            default: {
                throw new IllegalArgumentException("barcode format [" + barcodeFormat + "] not supported yet");
            }
        }
        Result result = reader.decode(binaryBitmap, hints);
        String textContent = result.getText();
        byte[] rawBinaryContent = (result.getRawBytes() != null) ? result.getRawBytes() : textContent.getBytes(charset);
        return new DecodedBarcodeResult(textContent, rawBinaryContent);
    }

    public static class DecodedBarcodeResult {

        private final String textContent;
        private final byte[] rawBinaryContent;

        public DecodedBarcodeResult(String textContent, byte[] rawBinaryContent) {
            this.textContent = textContent;
            this.rawBinaryContent = rawBinaryContent;
        }

        public String getTextContent() {
            return textContent;
        }

        public byte[] getRawBinaryContent() {
            return rawBinaryContent;
        }

        @Override
        public String toString() {
            return "DecodedBarcodeResult{" + "textContent=" + textContent + ", rawBinaryContent }";
        }

    }

}
