package com.melli.hub.service;

import java.util.Map;

public interface MessageResolverService {

    String resolve(String messageTemplate, Map<String, Object> parameters);

    String resolveEnglish(String messageTemplate, Map<String, Object> parameters);
}
