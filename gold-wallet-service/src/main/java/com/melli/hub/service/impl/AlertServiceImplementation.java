package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.SettingGeneralEntity;
import com.melli.hub.domain.master.persistence.AlertHourlyMessageRepository;
import com.melli.hub.domain.redis.AlertHourlyMessageRedis;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.AlertService;
import com.melli.hub.service.MessageService;
import com.melli.hub.service.SettingGeneralService;
import com.melli.hub.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.melli.hub.service.SettingGeneralService.MOBILE_FOR_GOT_ALERT;
import static com.melli.hub.service.SettingGeneralService.SMS_SEND_ALERT;

@Service
@Log4j2
@RequiredArgsConstructor
public class AlertServiceImplementation implements AlertService {

    private MessageService messageService;
    private final AlertHourlyMessageRepository alertHourlyMessageRepository;
    private final SettingGeneralService settingGeneralService;
    private final Environment environment;

    @Value("${project.name}")
    private String projectName;

    @Override
    @Async("threadPoolExecutorForSlack")
    public void send(String message, String errorCode) {

        try {
            Optional<SettingGeneralEntity> settingMobileSForAlertOptional = Optional.ofNullable(settingGeneralService.getSetting(MOBILE_FOR_GOT_ALERT));
            Optional<SettingGeneralEntity> settingSendSmsOptional = Optional.ofNullable(settingGeneralService.getSetting(SMS_SEND_ALERT));

            if (settingSendSmsOptional.isEmpty()) {
                log.info("there is no setting with name ({})", SMS_SEND_ALERT);
                return;
            }

            if (!Boolean.TRUE.equals(Boolean.parseBoolean(settingSendSmsOptional.get().getValue()))) {
                log.error("The setting named SMS_SEND_ALERT is configured to ({}), and the system does not send any notifications with errorCode ({}).", settingSendSmsOptional.get().getValue(), errorCode);
                return;
            }

            if (settingMobileSForAlertOptional.isEmpty()) {
                log.info("There is no setting with name ({})", MOBILE_FOR_GOT_ALERT);
                return;
            }


            if (!StringUtils.hasText(errorCode)) {
                log.info("The error code is null, and the system does not generate any error notifications");
                return;
            }

            List<String> mobileList = List.of(settingMobileSForAlertOptional.get().getValue().split(";"));
            if (mobileList.isEmpty()) {
                log.info("There are no mobile phone set up for sending alerts!!!");
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formatDateTime = LocalDateTime.now().format(formatter);

            StringBuilder stringBuilder = new StringBuilder(message);
            for (String mobile : mobileList) {
                String id = mobile + errorCode;
                Optional<AlertHourlyMessageRedis> alertHourlyMessage = alertHourlyMessageRepository.findById(id);
                if (alertHourlyMessage.isPresent()) {
                    log.info("we sent message to mobile ({}) for code ({}) before", mobile, errorCode);
                    continue;
                }
                String header = "Project Name (" + projectName + ")\n" + "ActionTime: " + formatDateTime + "\n";
                stringBuilder.insert(0, header);
                Optional<String> profileOptional = Arrays.stream(environment.getActiveProfiles()).findAny();
                profileOptional.ifPresent(s -> stringBuilder.insert(header.length(), "***** Profile (" + s + ") ***** \n"));
                messageService.send(stringBuilder.toString(), mobile);
                alertHourlyMessageRepository.save(new AlertHourlyMessageRedis(id, message, mobile, errorCode));
            }
        } catch (InternalServiceException e) {
            log.error("error in send message to slack and error is {}", e.getMessage());
        }
    }

    @Autowired
    public void setMessageService(@Lazy MessageService messageService) {
        this.messageService = messageService;
    }
}
