package com.kwasheniak.data;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

@Log4j2
@Getter
public class ChatMessage implements Serializable {
    private final MessageType messageType;
    private String filename;
    private final byte[] data;

    public ChatMessage(String text) {
        messageType = MessageType.TEXT;
        data = convertToData(text);
    }

    public ChatMessage(File file) throws IOException {
        messageType = MessageType.FILE;
        filename = file.getName();
        data = convertToData(file);
    }

    private byte[] convertToData(String text) {
        return text.getBytes();
    }

    private byte[] convertToData(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}
