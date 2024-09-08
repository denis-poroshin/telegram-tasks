package pro.sky.telegrambot.services;

import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.repostory.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
@Service
public class TaskService {

    private final NotificationTaskRepository notificationTaskRepository;
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    public TaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    public void createNotificationTask(Matcher matcher, Update update){
        String data = matcher.group(1);
        String text = matcher.group(3);
        logger.info("Creating task, data {} task {}",data,text );

        NotificationTask notificationTask = new NotificationTask();
        LocalDateTime parse = LocalDateTime.parse(data, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        notificationTask.setDateTime(parse);
        notificationTask.setText(text);
        notificationTask.setIdChat(update.message().chat().id());
        notificationTaskRepository.save(notificationTask);

        logger.info("Notification task{} created", notificationTask);
    }
}
