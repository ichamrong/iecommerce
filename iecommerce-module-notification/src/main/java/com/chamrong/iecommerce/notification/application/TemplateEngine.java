package com.chamrong.iecommerce.notification.application;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service("notificationTemplateEngine")
public class TemplateEngine {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");

  public String render(String template, Map<String, Object> data) {
    if (template == null) return null;
    if (data == null || data.isEmpty()) return template;

    Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      String key = matcher.group(1).trim();
      Object value = data.getOrDefault(key, matcher.group(0));
      matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(value)));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
}
