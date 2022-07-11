package com.cyecize.httpreader;

import com.cyecize.httpreader.util.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StartUp {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(80);
        System.out.println("Listening on port 80");

        while (true) {
            final Socket clientConnection = serverSocket.accept();
            new Thread(() -> {
                try {
                    InputStream inputStream = clientConnection.getInputStream();
                    OutputStream outputStream = clientConnection.getOutputStream();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String firstLine = bufferedReader.readLine();

                    List<String> headers = new ArrayList<>();
                    while (bufferedReader.ready()) {
                        String headerLine = bufferedReader.readLine();
                        if (headerLine.trim().isEmpty()) {
                            break;
                        }
                        headers.add(headerLine);
                    }

                    if (bufferedReader.ready()) {
                        //TODO: read body
                    }

                    final String[] firstLineTokens = firstLine.split("\\s+");
                    String method = firstLineTokens[0];
                    String url = firstLineTokens[1];

                    switch (method) {
                        case "GET":
                            processGetRequest(url, headers, outputStream);
                            break;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        clientConnection.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void processGetRequest(String url, List<String> headers, OutputStream outputStream) throws IOException {
        url = "webapp" + url;
        if (!FileUtils.exist(url)) {
            final String firstLine = "HTTP/1.1 404 Not Found\r\n\r\n<h1>File Was not found!</h1>";
            outputStream.write(firstLine.getBytes(StandardCharsets.UTF_8));
        }

        List<String> responseHeaders = new ArrayList<>();
        String fileType = FileUtils.probeContentType(url);
        InputStream inputStream = FileUtils.getInputStream(url);

        responseHeaders.add(String.format("Content-Type: %s", fileType));
        responseHeaders.add(String.format("Content-Length: %d", inputStream.available()));

        final String firstLine = "HTTP/1.1 200 OK\r\n";
        final StringBuilder response = new StringBuilder();
        response.append(firstLine);
        response.append(String.join("\r\n", responseHeaders));
        response.append("\r\n\r\n");

        outputStream.write(response.toString().getBytes(StandardCharsets.UTF_8));

        try (inputStream) {
            inputStream.transferTo(outputStream);
        }
    }
}
