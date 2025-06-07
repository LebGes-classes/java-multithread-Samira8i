package bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PhotoFilterBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "SamirafilterBW_bot";
    }

    @Override
    public String getBotToken() {
        return "7763317641:AAHv3UDF6PCOYiSog8Pc_UUKCrWkVtUmZy0";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            handlePhotos(update.getMessage());
        }
    }

    private void handlePhotos(Message message) {
        List<PhotoSize> photos = message.getPhoto();

        // Map: ключ - fileUniqueId, значение - самая большая PhotoSize для этого уникального файла
        Map<String, PhotoSize> largestPhotos = new LinkedHashMap<>();

        for (PhotoSize photo : photos) {
            String uniqueId = photo.getFileUniqueId();
            // Если фото с таким uniqueId ещё нет или текущее больше по размеру, то обновляем
            if (!largestPhotos.containsKey(uniqueId) || photo.getFileSize() > largestPhotos.get(uniqueId).getFileSize()) {
                largestPhotos.put(uniqueId, photo);
            }
        }

        // Теперь largestPhotos содержит по одному большому фото на каждый уникальный файл
        if (largestPhotos.size() > 3) {
            sendMessage(message.getChatId(), "Можно отправить максимум 3 фото.");
            return;
        }

        // Обрабатываем каждое фото
        for (PhotoSize photo : largestPhotos.values()) {
            new Thread(() -> {
                try {
                    File file = downloadPhoto(photo.getFileId());
                    File processed = applyGrayscaleFilter(file);
                    sendPhoto(message.getChatId(), processed);
                    file.delete();
                    processed.delete();
                } catch (Exception e) {
                    sendMessage(message.getChatId(), "Ошибка при обработке изображения: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        }
    }


    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendPhoto(Long chatId, File file) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId.toString());
        sendPhoto.setPhoto(new InputFile(file));
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private File downloadPhoto(String fileId) throws Exception {
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);
        org.telegram.telegrambots.meta.api.objects.File telegramFile = execute(getFile);
        String fileUrl = telegramFile.getFilePath();
        String fullUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + fileUrl;

        BufferedImage image = ImageIO.read(new URL(fullUrl));
        File localFile = File.createTempFile("input_", ".jpg");
        ImageIO.write(image, "jpg", localFile);
        return localFile;
    }

    private File applyGrayscaleFilter(File inputFile) throws IOException {
        BufferedImage img = ImageIO.read(inputFile);

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3;
                int newRgb = (gray << 16) | (gray << 8) | gray;
                img.setRGB(x, y, newRgb);
            }
        }

        File output = File.createTempFile("grayscale_", ".jpg");
        ImageIO.write(img, "jpg", output);
        return output;
    }
}
