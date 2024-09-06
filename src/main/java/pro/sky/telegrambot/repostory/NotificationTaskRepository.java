package pro.sky.telegrambot.repostory;

import jdk.jfr.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.entity.NotificationTask;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

//    Optional<NotificationTask> findByAll();
//    @Query(value = "SELECT * FROM data_time = ${localDateTime}", nativeQuery = true)
//    Collection<NotificationTask> findBy(LocalDateTime localDateTime);
    @Query(value = "SELECT * FROM notification_task WHERE date_time = :localDateTime", nativeQuery = true)
    Collection<NotificationTask> findByAllDateTime(@Param("localDateTime") LocalDateTime localDateTime);



}
