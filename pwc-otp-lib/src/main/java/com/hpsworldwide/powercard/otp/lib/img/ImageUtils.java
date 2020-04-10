package com.hpsworldwide.powercard.otp.lib.img;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author (c) HPS Solutions
 */
public class ImageUtils {

    private static final String PNG_IMAGE_FORMAT_NAME = "PNG";
    private static final String JPEG_IMAGE_FORMAT_NAME = "JPEG";
    /**
     * constants from
     * http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html#C.Anc-text
     */
    /**
     * Short (one line) title or caption for image
     */
    public static final String TITLE = "Title";
    /**
     * Name of image's creator
     */
    public static final String AUTHOR = "Author";
    /**
     * Description of image (possibly long)
     */
    public static final String DESCRIPTION = "Description";
    /**
     * Copyright notice
     */
    public static final String COPYRIGHT = "Copyright";
    /**
     * Time of original image creation
     */
    public static final String CREATION_TIME = "Creation Time";
    /**
     * Software used to create the image
     */
    public static final String SOFTWARE = "Software";
    /**
     * Legal disclaimer
     */
    public static final String DISCLAIMER = "Disclaimer";
    /**
     * Warning of nature of content
     */
    public static final String WARNING = "Warning";
    /**
     * Device used to create the image
     */
    public static final String SOURCE = "Source";
    /**
     * Miscellaneous comment; conversion from GIF comment
     */
    public static final String COMMENT = "Comment";

    public static byte[] toPNG(RenderedImage image) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            toPNG(image, os);
            return os.toByteArray();
        }
    }

    public static void toPNG(RenderedImage image, OutputStream outputStream) throws IOException {
        ImageIO.write(image, PNG_IMAGE_FORMAT_NAME, outputStream);
    }

    public static byte[] toPNG(RenderedImage image, Map<String, String> metadataMap) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            toPNG(image, metadataMap, os);
            return os.toByteArray();
        }
    }

    // http://stackoverflow.com/questions/6495518/writing-image-metadata-in-java-preferably-png#answer-8735707
    public static void toPNG(RenderedImage image, Map<String, String> metadataMap, OutputStream outputStream) throws IOException {
        // note : compression parameter not supported in PNG default encoding
        if (metadataMap == null || metadataMap.isEmpty()) {
            toPNG(image, outputStream);
            return;
        }
        ImageWriter writer = ImageIO.getImageWritersByFormatName(PNG_IMAGE_FORMAT_NAME).next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromRenderedImage(image);
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
        IIOMetadataNode root = new IIOMetadataNode(metadata.getNativeMetadataFormatName());
        IIOMetadataNode text = new IIOMetadataNode("tEXt");
        for (String keyword : metadataMap.keySet()) {
            IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry");
            textEntry.setAttribute("keyword", keyword);
            textEntry.setAttribute("value", metadataMap.get(keyword));
            text.appendChild(textEntry);
        }
        root.appendChild(text);
        metadata.mergeTree(metadata.getNativeMetadataFormatName(), root);
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(stream);
            writer.write(metadata, new IIOImage(image, /*tumbnails*/ null, metadata), writeParam);
        }
    }

    public static byte[] toJPEG(RenderedImage image) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
            toJPEG(image, os);
            return os.toByteArray();
        }
    }

    public static void toJPEG(RenderedImage image, OutputStream outputStream) throws IOException {
        ImageIO.write(image, JPEG_IMAGE_FORMAT_NAME, outputStream);
    }

    /**
     * @param compressionQuality may be null
     * @param comment may be null
     */
    public static byte[] toJPEG(RenderedImage image, Float compressionQuality, String comment) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
            toJPEG(image, compressionQuality, comment, os);
            return os.toByteArray();
        }
    }

    // inspired by http://www.java-gaming.org/index.php/topic,4997.
    /**
     * @param compressionQuality may be null
     * @param comment may be null
     */
    public static void toJPEG(RenderedImage image, Float compressionQuality, String comment, OutputStream outputStream) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName(JPEG_IMAGE_FORMAT_NAME).next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        // System.out.println("getCompressionQualityDescriptions: " + Arrays.toString(writeParam.getCompressionQualityDescriptions()));
        // System.out.println("getCompressionTypes: " + Arrays.toString(writeParam.getCompressionTypes()));
        // System.out.println("getCompressionQualityValues: " + Arrays.toString(writeParam.getCompressionQualityValues()));
        IIOMetadata metadata;
        if (comment != null) {
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromRenderedImage(image);
            metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            IIOMetadataNode root = new IIOMetadataNode(metadata.getNativeMetadataFormatName());
            // names from http://docs.oracle.com/javase/7/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html#image
            IIOMetadataNode jpegVariety = new IIOMetadataNode("JPEGvariety");
            root.appendChild(jpegVariety);
            IIOMetadataNode markerSequence = new IIOMetadataNode("markerSequence");
            IIOMetadataNode com = new IIOMetadataNode("com");
            com.setAttribute("comment", comment);
            markerSequence.appendChild(com);
            root.appendChild(markerSequence);
            metadata.mergeTree(metadata.getNativeMetadataFormatName(), root);
        } else {
            metadata = null;
        }
        if (compressionQuality != null) {
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(compressionQuality);
        }
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(stream);
            writer.write(metadata, new IIOImage(image, /*thumbails*/ null, metadata), writeParam);
        }
    }

}
