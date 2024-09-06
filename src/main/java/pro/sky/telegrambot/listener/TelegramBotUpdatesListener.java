package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repostory.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final NotificationTaskRepository notificationTaskRepository;

    private final Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");



    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private OrderedFormContentFilter formContentFilter;

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;

    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {

            logger.info("Processing update: {}", update);
            if (update.message().text().equals("/help")){
                SendMessage sendMessage = new SendMessage(update.message().chat().id(),
                        getHelp());
                telegramBot.execute(sendMessage);

            }
            if (update.message().text().equals("/start")){
                String nameUser = update.message().from().firstName();
                SendMessage sendMessage = new SendMessage(update.message().chat().id(),
                        getHello(nameUser));
                telegramBot.execute(sendMessage);
            }

            Matcher matcher = pattern.matcher(update.message().text());
            if (matcher.matches()){
                createNotificationTask(matcher, update);
            }

            Collection<NotificationTask> getListDateTime = getListDateTime();
            for (NotificationTask notificationTask : getListDateTime){
                if (notificationTask.getIdChat().equals(update.message().chat().id())){
                    SendMessage sendMessage = new SendMessage(update.message().chat().id(),
                            notificationTask.getText());
                    telegramBot.execute(sendMessage);

                }
            }


        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private String getHelp(){
        return "Чтобы заплонировать задачу тебе необходимо вписать дату\n" +
                "и задчу как указанно в примере:\n"
                + "03.09.2024 18:26 Сделать домашнюю работу";
    }

    private String getHello(String firstName){
        logger.info("Hello bot {}", firstName);
        return "Привет " + firstName + "!\n"
                + "Этот телеграм бот предназначен для планирования задач.\n"
                + "Чтобы узнать как им пользоваться введи команду /help.";
    }

    private void createNotificationTask(Matcher matcher, Update update){
        String data = matcher.group(1);
        String text = matcher.group(3);
        NotificationTask notificationTask = new NotificationTask();
        LocalDateTime parse = LocalDateTime.parse(data, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        notificationTask.setDateTime(parse);
        notificationTask.setText(text);
        notificationTask.setIdChat(update.message().chat().id());
        notificationTaskRepository.save(notificationTask);
    }

    @Scheduled(cron = "0 1 * * * *")
    private Collection<NotificationTask> getListDateTime(){

        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        Collection<NotificationTask> byAllDateTime = notificationTaskRepository.findByAllDateTime(dateTime);
        logger.info("getListDateTime: {}", byAllDateTime);
        return byAllDateTime;

    }

}
