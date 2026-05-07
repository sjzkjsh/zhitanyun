package com.example.Service.PdfServiceImpl;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PdfStreamService {

    public ResponseEntity<InputStreamResource> getPdfStream(Path filePath, String rangeHeader) throws IOException {
        long fileLength = Files.size(filePath);
        String contentType = "application/pdf";

        if (rangeHeader == null) {
            // 返回整个文件
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(resource);
        }

        // 解析 Range: bytes=start-end
        String[] ranges = rangeHeader.replace("bytes=", "").split("-");
        long start = Long.parseLong(ranges[0]);
        long end = ranges.length > 1 ? Long.parseLong(ranges[1]) : fileLength - 1;
        if (end > fileLength - 1) end = fileLength - 1;
        long contentLength = end - start + 1;

        // 构建只读取指定范围的 InputStream
        InputStream fullStream = Files.newInputStream(filePath);
        fullStream.skip(start);
        InputStream rangeStream = new LimitedInputStream(fullStream, contentLength);

        InputStreamResource resource = new InputStreamResource(rangeStream);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                .contentLength(contentLength)
                .body(resource);
    }

    // 限制读取字节数的输入流包装器
    private static class LimitedInputStream extends InputStream {
        private final InputStream wrapped;
        private long remaining;
        public LimitedInputStream(InputStream wrapped, long limit) {
            this.wrapped = wrapped;
            this.remaining = limit;
        }
        @Override
        public int read() throws IOException {
            if (remaining <= 0) return -1;
            int b = wrapped.read();
            if (b != -1) remaining--;
            return b;
        }
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) return -1;
            int toRead = (int) Math.min(len, remaining);
            int read = wrapped.read(b, off, toRead);
            if (read > 0) remaining -= read;
            return read;
        }
        @Override
        public void close() throws IOException {
            wrapped.close();
        }
    }
}