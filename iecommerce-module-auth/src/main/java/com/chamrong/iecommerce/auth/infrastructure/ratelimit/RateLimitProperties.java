package com.chamrong.iecommerce.auth.infrastructure.ratelimit;

/**
 * In-memory configuration for IP-based rate limiting.
 *
 * <p>Defaults are conservative values suitable for production. For now this is a simple POJO
 * instantiated directly by {@link IpRateLimitFilter}; if you later want to externalize it to
 * configuration, you can reintroduce {@code @ConfigurationProperties} and a dedicated bean.
 */
public class RateLimitProperties {

  private int loginMaxPerMinute = 10;
  private int forgotPasswordMaxPerHour = 5;
  private int signupMaxPerDay = 3;

  public int loginMaxPerMinute() {
    return loginMaxPerMinute;
  }

  public void setLoginMaxPerMinute(int loginMaxPerMinute) {
    this.loginMaxPerMinute = loginMaxPerMinute;
  }

  public int forgotPasswordMaxPerHour() {
    return forgotPasswordMaxPerHour;
  }

  public void setForgotPasswordMaxPerHour(int forgotPasswordMaxPerHour) {
    this.forgotPasswordMaxPerHour = forgotPasswordMaxPerHour;
  }

  public int signupMaxPerDay() {
    return signupMaxPerDay;
  }

  public void setSignupMaxPerDay(int signupMaxPerDay) {
    this.signupMaxPerDay = signupMaxPerDay;
  }
}
