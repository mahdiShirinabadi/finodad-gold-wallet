package com.melli.wallet.service.operation;

import java.util.Map;

public interface MessageResolverOperationService {

    String resolve(String messageTemplate, Map<String, Object> parameters);

    String resolveEnglish(String messageTemplate, Map<String, Object> parameters);
}
