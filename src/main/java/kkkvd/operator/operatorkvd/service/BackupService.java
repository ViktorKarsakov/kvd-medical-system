package kkkvd.operator.operatorkvd.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
public class BackupService {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${app.backup.pg-dump-path:pg_dump}")
    private String pgDumpPath;

    public byte[] createBackup() {
        try {
            String cleanUrl = url.replace("jdbc:postgresql://", "");
            String[] parts = cleanUrl.split("[:/]");
            String host = parts[0];
            String port = parts[1];
            String dbName = parts[2];

            ProcessBuilder pb = new ProcessBuilder(
                    pgDumpPath,
                    "-h", host,
                    "-p", port,
                    "-U", username,
                    "--no-owner",
                    "--no-acl",
                    dbName
            );
            pb.environment().put("PGPASSWORD", password);

            Process process = pb.start();

            // Запускаем чтение stderr в отдельном потоке — предотвращает deadlock
            // при переполнении буфера stderr
            StringBuilder stderrBuffer = new StringBuilder();
            Thread stderrThread = startStderrReader(process, stderrBuffer);

            // Читаем stdout (сам SQL-дамп)
            byte[] result = readProcessOutput(process);

            // Ждём завершения процесса (максимум 5 минут)
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "pg_dump превысил лимит времени (5 минут)");
            }

            // Даём потоку stderr время завершить чтение
            stderrThread.join(2000);

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ошибка pg_dump (код " + exitCode + "): " + stderrBuffer);
            }

            return result;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка создания бэкапа: " + e.getMessage());
        }
    }

    private Thread startStderrReader(Process process, StringBuilder buffer) {
        Thread thread = new Thread(() -> {
            try {
                buffer.append(new String(process.getErrorStream().readAllBytes()));
            } catch (Exception ignored) {}
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private byte[] readProcessOutput(Process process) throws Exception {
        InputStream inputStream = process.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toByteArray();
    }

    public String generateFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        return "kvd_backup_" + timestamp + ".sql";
    }
}