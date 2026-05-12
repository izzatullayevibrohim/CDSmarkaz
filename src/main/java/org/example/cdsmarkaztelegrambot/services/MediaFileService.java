package org.example.cdsmarkaztelegrambot.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.net.ftp.FTPClient;
import org.example.cdsmarkaztelegrambot.enums.MediaFileType;
import org.example.cdsmarkaztelegrambot.models.MediaFile;
import org.example.cdsmarkaztelegrambot.repositories.MediaFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaFileService {

    private final MediaFileRepository mediaFileRepository;

    @Value("${ftp.server}")
    private String ftpServer;

    @Value("${ftp.port}")
    private int ftpPort;

    @Value("${ftp.username}")
    private String ftpUsername;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.remote-dir}")
    private String ftpRemoteDir;

    public MediaFile getMediaFileById(Long id) {
        return mediaFileRepository.findById(id).orElse(null);
    }

    public Long uploadFile(MultipartFile file, String type) throws IOException {
        FTPClient ftp = new FTPClient();

        String fileType = switch (type) {
            case "PHOTO" -> MediaFileType.PHOTO.getLabel();
            case "VIDEO" -> MediaFileType.VIDEO.getLabel();
            case "VOICE" -> MediaFileType.AUDIO.getLabel();
            case "TEXT" -> MediaFileType.TEXT.getLabel();
            default -> null;
        };

        try {
            ftp.connect(ftpServer, ftpPort);

            boolean loggedIn = ftp.login(ftpUsername, ftpPassword);
            if (!loggedIn) {
                throw new IOException("FTP login failed for user: " + ftpUsername);
            }

            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String remotePath = ftpRemoteDir + fileName;

            try(InputStream inputStream = file.getInputStream()) {

                boolean success = ftp.storeFile(remotePath, inputStream);

                if (!success) {
                    throw new IOException("FTP store failed for file: " + fileName);
                }else {

                    MediaFile mediaFile = new MediaFile();
                    mediaFile.setOriginalName(file.getOriginalFilename());
                    mediaFile.setContentType(file.getContentType());
                    mediaFile.setFileSize(file.getSize());
                    mediaFile.setFilePath(remotePath);
                    mediaFile.setFileType(fileType);
                    MediaFile save = mediaFileRepository.save(mediaFile);
                    return save.getId();
                }
            }
        }finally {
            if (ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
            }
        }
    }

    public byte[] downloadFile(String remotePath) throws IOException {
        FTPClient ftp = new FTPClient();

        try {
            ftp.connect(ftpServer, ftpPort);

            boolean loggedIn = ftp.login(ftpUsername, ftpPassword);
            if (!loggedIn) {
                throw new IOException("FTP login failed for user: " + ftpUsername);
            }

            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 InputStream inputStream = ftp.retrieveFileStream(remotePath)) {

                if (inputStream == null) {
                    throw new IOException("Fayl topilmadi: " + remotePath + " | Reply: " + ftp.getReplyString());
                }

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                ftp.completePendingCommand();
                return outputStream.toByteArray();
            }

        } finally {
            if (ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
            }
        }
    }
}
