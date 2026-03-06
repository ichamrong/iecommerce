package com.chamrong.iecommerce.promotion;

import com.chamrong.iecommerce.promotion.domain.rule.policy.BestSavingsPolicy;
import com.chamrong.iecommerce.promotion.domain.rule.policy.SelectionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Application-level configuration for choosing the default promotion selection policy.
 *
 * <p>We prefer {@link BestSavingsPolicy} as the default {@link SelectionPolicy} so that when
 * multiple policies are present in the context, the engine has a single unambiguous candidate.
 */
@Configuration
public class PromotionPolicyConfiguration {

  @Bean
  @Primary
  public SelectionPolicy selectionPolicy(BestSavingsPolicy bestSavingsPolicy) {
    return bestSavingsPolicy;
  }
}
