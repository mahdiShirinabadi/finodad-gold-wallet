package com.melli.wallet.service.operation.impl;

import com.melli.wallet.service.operation.MessageResolverOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Class Name: MessageResolverServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/26/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class MessageResolverOperationServiceImplementation implements MessageResolverOperationService {

    @Override
    public String resolve(String messageTemplate, Map<String, Object> parameters) {
        parameters.put("line", "\n");
        parameters.put("rtl", "\u200f");
        parameters.put("ltr", "\u200e");
        parameters.put("divider", "************************");
        StringSubstitutor sub = new StringSubstitutor(parameters);
        return sub.replace(messageTemplate);
    }

    @Override
    public String resolveEnglish(String messageTemplate, Map<String, Object> parameters) {
        parameters.put("line", "\n");
        parameters.put("rtl", "\u200f");
        parameters.put("ltr", "\u200e");
        parameters.put("divider", "************************");
        StringSubstitutor sub = new StringSubstitutor(parameters);
        return normalize(sub.replace(messageTemplate));
    }

    private static String normalize(String text) {

        String result = text.replace("\u06cc", "\u064a");
        result = result.replace("\u06a9", "\u0643");
        result = result.replace("0", "\u06F0");
        result = result.replace("1", "\u06F1");
        result = result.replace("2", "\u06F2");
        result = result.replace("3", "\u06F3");
        result = result.replace("4", "\u06F4");
        result = result.replace("5", "\u06F5");
        result = result.replace("6", "\u06F6");
        result = result.replace("7", "\u06F7");
        result = result.replace("8", "\u06F8");
        result = result.replace("9", "\u06F9");

        return result;
    }
}
