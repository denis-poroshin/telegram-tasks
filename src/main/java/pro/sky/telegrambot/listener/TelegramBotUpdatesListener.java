package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repostory.NotificationTaskRepository;
import pro.sky.telegrambot.services.SchedulerService;
import pro.sky.telegrambot.services.TaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final NotificationTaskRepository notificationTaskRepository;
    private final SchedulerService schedulerService;
    private final TaskService taskService;

    private final Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");



    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private OrderedFormContentFilter formContentFilter;
    @Autowired
    private DataSourceTransactionManagerAutoConfiguration dataSourceTransactionManagerAutoConfiguration;

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository, SchedulerService scheduler, TaskService taskService) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.schedulerService = scheduler;
        this.taskService = taskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            SendMessage sendMessage;
            Matcher matcher = pattern.matcher(update.message().text());
            if (matcher.matches()){
                taskService.createNotificationTask(matcher, update);
            }

            Collection<NotificationTask> getListDateTime = schedulerService.getListDateTime();
            for (NotificationTask notificationTask : getListDateTime){
                if (notificationTask.getIdChat().equals(update.message().chat().id())){
                    sendMessage = new SendMessage(update.message().chat().id(),
                            notificationTask.getText());

                    telegramBot.execute(sendMessage);

                }
            }

            switch (update.message().text()){
                case "/help":
                    sendMessage = new SendMessage(update.message().chat().id(),
                            getHelp());
                    telegramBot.execute(sendMessage);
                    break;
                case "/start":
                    String nameUser = update.message().from().firstName();
                    sendMessage = new SendMessage(update.message().chat().id(),
                            getHello(nameUser));
                    telegramBot.execute(sendMessage);
                    break;
//                default:
//                    sendMessage = new SendMessage(update.message().chat().id(),
//                            "Команда: " + update.message().text() + "\nне поддерживается");
//                    telegramBot.execute(sendMessage);
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
}
