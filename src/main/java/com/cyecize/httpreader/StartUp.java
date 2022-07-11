package com.cyecize.httpreader;

import com.cyecize.httpreader.util.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StartUp {
    public static void main(String[] args) throws Exception {
        final int port = 8080;
        final ServerSocket serverSocket = new ServerSocket(port);

        System.out.println(String.format("Start listening on port %d", port));

        while (true) {
            final Socket client = serverSocket.accept();
            client.setSoTimeout(0);
            new Thread(() -> {
                try {
                    final InputStream inputStream = client.getInputStream();
                    final OutputStream outputStream = client.getOutputStream();

                    final HttpRequest httpRequest = parseMetadata(inputStream);

                    switch (httpRequest.getMethod()) {
                        case "GET":
                            handleGetRequest(httpRequest, outputStream);
                            break;
                        case "POST":
                            handlePostRequest(httpRequest, outputStream, inputStream);
                            break;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static HttpRequest parseMetadata(InputStream data) throws IOException {
        final List<String> metadataLines = new ArrayList<>();

        final StringBuilder lineBuilder = new StringBuilder();
        int b;
        boolean wasNewLine = false;

        while ((b = data.read()) >= 0) {
            if (b == '\r') {
                int next = data.read();
                if (next == '\n') {
                    if (wasNewLine) {
                        break;
                    }
                    wasNewLine = true;
                    metadataLines.add(lineBuilder.toString());
                    lineBuilder.setLength(0);
                }
            } else {
                lineBuilder.append((char) b);
                wasNewLine = false;
            }
        }

        final String firstLine = metadataLines.get(0);
        final String method = firstLine.split("\\s+")[0];
        final String url = firstLine.split("\\s+")[1];

        final Map<String, String> headers = new HashMap<>();

        for (int i = 1; i < metadataLines.size(); i++) {
            String headerLine = metadataLines.get(i);
            if (headerLine.trim().isEmpty()) {
                break;
            }

            String key = headerLine.split(":\\s")[0];
            String value = headerLine.split(":\\s")[1];

            headers.put(key, value);
        }

        return new HttpRequest(method, url, headers);
    }

    public static void handleGetRequest(HttpRequest request, OutputStream outputStream) throws IOException {
        String fileName = request.getUrl();
        if (FileUtils.exist("webapp" + fileName)) {
            fileName = "webapp" + fileName;
        } else if (FileUtils.exist("uploaded" + fileName)) {
            fileName = "uploaded" + fileName;
        } else {
            outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n<h1>File not found!</h1>".getBytes(StandardCharsets.UTF_8));
            return;
        }

        final StringBuilder responseMetadata = new StringBuilder();
        responseMetadata.append("HTTP/1.1 200 OK\r\n");

        responseMetadata.append(String.format("Content-Type: %s\r\n", FileUtils.probeContentType(fileName)));

        final InputStream fileStream = FileUtils.getInputStream(fileName);
        responseMetadata.append(String.format("Content-Length: %d\r\n", fileStream.available()));
        responseMetadata.append("\r\n");

        outputStream.write(responseMetadata.toString().getBytes(StandardCharsets.UTF_8));
        try (fileStream) {
            fileStream.transferTo(outputStream);
        }
    }

    public static void handlePostRequest(HttpRequest request,
                                         OutputStream clientOs,
                                         InputStream inputStream) throws IOException {
        int remaining = Integer.parseInt(request.getHeaders().get("Content-Length"));
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final byte[] buff = new byte[2048];

        while (remaining > 0) {
            int read = inputStream.read(buff, 0, Math.min(remaining, buff.length));
            os.write(buff, 0, read);
            remaining -= read;
            System.out.println(remaining);
        }

        final String body = os.toString();
        System.out.println(body.length());
        ObjectMapper objectMapper = new ObjectMapper();

        FileDto fileDto = objectMapper.readValue(body, FileDto.class);
        FileUtils.saveFile(fileDto.getFileName(), fileDto.getFile());

        clientOs.write("HTTP/1.1 201 Created\r\n\r\n<h1>File was created</h1>".getBytes(StandardCharsets.UTF_8));
    }

    static class HttpRequest {
        private final String method;
        private final String url;
        private final Map<String, String> headers;

        HttpRequest(String method, String url, Map<String, String> headers) {
            this.method = method;
            this.url = url;
            this.headers = headers;
        }

        public String getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }
    }

    static class FileDto {
        private String fileName;
        private String file;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }
    }
}
