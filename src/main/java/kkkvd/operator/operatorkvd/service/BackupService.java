package kkkvd.operator.operatorkvd.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Сервис для создания резервной копии базы данных
@Service
public class BackupService {
    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${app.backup.pg-dump-path}")
    private String pgDumpPath;

    //Создаёт бэкап и возвращает содержимое SQL-файла
    public byte[] createBackup() {
        try {
            // Извлекаем хост, порт и имя БД из JDBC URL
            String cleanUrl = url.replace("jdbc:postgresql://", "");
            String[] parts = cleanUrl.split("[:/]");
            String host = parts[0];
            String port = parts[1];
            String dbName = parts[2];

            // Собираем команду pg_dump

            ProcessBuilder pb = new ProcessBuilder(
                    pgDumpPath,
                    "-h", host,
                    "-p", port,
                    "-U", username,
                    "--no-owner",
                    "--no-acl",
                    dbName
            );

            //Передаём пароль через переменную окружения PGPASSWORD
            pb.environment().put("PGPASSWORD", password);

            // Запускаем процесс
            Process process = pb.start();

            // Читаем вывод pg_dump (это и есть SQL-дамп)
            InputStream inputStream = process.getInputStream();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            // Ждём завершения процесса
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Читаем stderr для диагностики
                String error = new String(process.getErrorStream().readAllBytes());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка pg_dump (код " + exitCode + "): " + error);
            }
            return result.toByteArray();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка создания бэкапа: " + e.getMessage());
        }
    }

    //Генерирует имя файла бэкапа с датой и временем
    public String generateFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        return "kvd_backup_" + timestamp + ".sql";
    }
}
