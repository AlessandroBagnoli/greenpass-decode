package com.tano.green;

import COSE.Encrypt0Message;
import COSE.Message;
import com.google.iot.cbor.CborMap;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.Inflater;
import javax.imageio.ImageIO;
import nl.minvws.encoding.Base45;

public class Main {
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws Exception {

        // 1 - read text from file
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("IMG_2017.PNG");
        BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(is));
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);
        String text = result.getText();

        // 2 - remove prefix "HC1:" and decode base45 string
        byte[] bytecompressed = Base45.getDecoder().decode(text.substring(4));

        // 3 - inflate string using zlib
        Inflater inflater = new Inflater();
        inflater.setInput(bytecompressed);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytecompressed.length);
        byte[] buffer = new byte[BUFFER_SIZE];
        while (!inflater.finished()) {
            final int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        // 4 - decode COSE message (no signature verification done)
        Message a = Encrypt0Message.DecodeFromBytes(outputStream.toByteArray());

        // 5 create CborObject MAP
        CborMap cborMap = CborMap.createFromCborByteArray(a.GetContent());
        System.out.println(cborMap.toString(2));
    }


}
