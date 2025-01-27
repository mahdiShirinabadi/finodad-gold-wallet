package com.melli.hub.service.impl;

import com.melli.hub.service.MessageResolverService;
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
public class MessageResolverServiceImplementation implements MessageResolverService {

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

        String result = text.replaceAll("\u06cc", "\u064a");
        result = result.replaceAll("\u06a9", "\u0643");
        result = result.replaceAll("0", "\u06F0");
        result = result.replaceAll("1", "\u06F1");
        result = result.replaceAll("2", "\u06F2");
        result = result.replaceAll("3", "\u06F3");
        result = result.replaceAll("4", "\u06F4");
        result = result.replaceAll("5", "\u06F5");
        result = result.replaceAll("6", "\u06F6");
        result = result.replaceAll("7", "\u06F7");
        result = result.replaceAll("8", "\u06F8");
        result = result.replaceAll("9", "\u06F9");

        return result;
    }
}
