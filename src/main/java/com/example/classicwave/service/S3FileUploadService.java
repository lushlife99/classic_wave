package com.example.classicwave.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
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
    public void uploadImages(List<ImageGeneration> imageResults, String folderName) {
        for (ImageGeneration image : imageResults) {
            try {
                String url = image.getOutput().getUrl();
                String fileName = Paths.get(new URL(url).getPath()).getFileName().toString();
                String s3Key = folderName + "/" + fileName;
                InputStream inputStream = new URL(url).openStream();
                amazonS3Client.putObject(bucket, s3Key, inputStream, null);
                inputStream.close();
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void uploadAudios(List<Speech> audioResults, String folderName) {
        for (int i = 0; i < audioResults.size(); i++) {
            try {

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
            } catch (Exception e) {
                log.error("Failed to upload audio file to S3: " + e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }
}
