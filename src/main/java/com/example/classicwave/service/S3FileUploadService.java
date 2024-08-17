package com.example.classicwave.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileUploadService {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final static String AUDIO_FILE_PREFIX = "audio";

    public void uploadImages(List<ImageGeneration> imageResults, String folderName) throws IOException {
        for (int i = 0; i < imageResults.size(); i++) {
            ImageGeneration image = imageResults.get(i);

            String b64Json = image.getOutput().getB64Json();

            if (b64Json == null || b64Json.isEmpty()) {
                log.warn("Base64 JSON string is null or empty for image: " + image);
                continue;
            }

            byte[] imageBytes = Base64.decodeBase64(b64Json);
            String fileName = i + ".png";
            String s3Key = folderName + "/" + fileName;

            // S3에 업로드
            InputStream inputStream = new ByteArrayInputStream(imageBytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType("image/png");

            amazonS3Client.putObject(bucket, s3Key, inputStream, metadata);
            inputStream.close();

        }
    }

    public void uploadAudios(List<Speech> audioResults, String folderName) throws IOException {
        for (int i = 0; i < audioResults.size(); i++) {


            Speech audioResult = audioResults.get(i);
            byte[] output = audioResult.getOutput();
            String fileName = AUDIO_FILE_PREFIX + i;
            String s3Key = folderName + "/" + fileName;

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(output);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(output.length);
            metadata.setContentType("audio/mpeg");
            amazonS3Client.putObject(bucket, s3Key, byteArrayInputStream, metadata);
            byteArrayInputStream.close();

        }
    }
}
