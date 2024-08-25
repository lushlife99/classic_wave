package com.example.classicwave.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.example.classicwave.domain.Book;
import com.example.classicwave.error.CustomException;
import com.example.classicwave.error.ErrorCode;
import com.example.classicwave.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileUploadService {

    private final AmazonS3Client amazonS3Client;
    private final BookRepository bookRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final static String AUDIO_FILE_PREFIX = "audio";
    private final static String Image_FILE_PREFIX = "image";

    public String getImageUrl(String folderName, String fileName) {

        return amazonS3Client.getUrl(bucket, folderName + "/" + fileName).toString();
    }

    public Resource getAudio(String folderName, String fileName) {
        S3Object imageObject = amazonS3Client.getObject(new GetObjectRequest(bucket, folderName + "/" + fileName));
        InputStream imageInputStream = imageObject.getObjectContent();
        return new InputStreamResource(imageInputStream);
    }

    @Transactional(readOnly = true)
    public String getBookThumbnail(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        return getImageUrl(book.getFolderName() + "/image", Image_FILE_PREFIX + "0.png");
    }

    public void uploadImages(List<Resource> imageResults, String folderName) throws IOException {
        for (int i = 0; i < imageResults.size(); i++) {
            Resource image = imageResults.get(i);
            String fileName = Image_FILE_PREFIX + i + ".png";
            String s3Key = folderName + "/" + fileName;

            try (InputStream imageInputStream = image.getInputStream()) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(imageInputStream.available());
                metadata.setContentType("image/png");

                amazonS3Client.putObject(bucket, s3Key, imageInputStream, metadata);
            } catch (IOException e) {
                log.error("Failed to upload image: {}", fileName, e);
                throw e;
            }
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

    public String uploadProfileImage(Resource image, Long id) throws IOException {

        String fileName =  Image_FILE_PREFIX +id+".png";
        String s3Key = "user/" + fileName;

        try (InputStream imageInputStream = image.getInputStream()) {

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/png");
            amazonS3Client.putObject(bucket, s3Key, imageInputStream, metadata);

            return fileName;
        } catch (IOException e) {
            log.error("Failed to upload image: {}", fileName, e);
            throw e;
        }
    }



}
