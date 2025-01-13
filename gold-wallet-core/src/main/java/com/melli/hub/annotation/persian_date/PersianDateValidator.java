package com.melli.hub.annotation.persian_date;

import com.melli.hub.util.date.DateUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Log4j2
public class PersianDateValidator implements ConstraintValidator<PersianDateValidation, String> {

    private String expectedFormat;

    private boolean biggerToday;

    @Override
    public void initialize(PersianDateValidation persianDateValidation) {
        expectedFormat  = persianDateValidation.format();
        biggerToday = persianDateValidation.biggerToday();
    }

    public boolean isValid(String input, ConstraintValidatorContext cxt) {
        String format = expectedFormat;
        log.info("start check date format ({}) with format ({})!!!", input, format);
        Date date = null;
        try {
            SimpleDateFormat  dateFormat = new SimpleDateFormat(format);
            dateFormat.setLenient(false);
            date = dateFormat.parse(input);
            if(date == null){
                return false;
            }
        } catch (ParseException e) {
            log.error("date format ({}) is invalid!!!", input);
            return false;
        }
        if(biggerToday){
            Date inventoryGeorgianDate = DateUtils.parse(input, format, true, DateUtils.FARSI_LOCALE);
            if(inventoryGeorgianDate.before(new Date())){
                log.error("input date ({}) is less than current date", input);
                cxt.disableDefaultConstraintViolation();
                cxt.buildConstraintViolationWithTemplate("تاریخ نمیتواند از روز جاری کمتر باشد")
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
