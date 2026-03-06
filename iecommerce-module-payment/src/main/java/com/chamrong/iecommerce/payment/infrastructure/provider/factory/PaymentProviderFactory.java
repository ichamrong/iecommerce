package com.chamrong.iecommerce.payment.infrastructure.provider.factory;

import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.exception.PaymentDomainException;
import com.chamrong.iecommerce.payment.domain.intent.PaymentProviderPort;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Strategy + Factory: resolves the correct {@link PaymentProviderPort} for a given {@link
 * ProviderType} at runtime.
 *
 * <p>All {@link PaymentProviderPort} implementations are injected by Spring and registered by their
 * {@code supportedType()} at construction time. O(1) lookup via {@link Map}.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * PaymentProviderPort provider = factory.get(ProviderType.STRIPE);
 * ProviderResponse response = provider.createIntent(request);
 * }</pre>
 */
@Component
public class PaymentProviderFactory {

  private static final Logger log = LoggerFactory.getLogger(PaymentProviderFactory.class);

  private final Map<ProviderType, PaymentProviderPort> registry;

  /**
   * Constructs the factory from all available {@link PaymentProviderPort} adapters. Each adapter
   * must return a unique {@link ProviderType} from {@code supportedType()}.
   *
   * @param providers all provider adapters discovered by Spring
   */
  public PaymentProviderFactory(List<PaymentProviderPort> providers) {
    this.registry =
        providers.stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    PaymentProviderPort::supportedType,
                    Function.identity(),
                    (a, b) -> {
                      throw new IllegalStateException("Duplicate provider: " + a.supportedType());
                    }));
    log.info("PaymentProviderFactory initialized with providers: {}", registry.keySet());
  }

  /**
   * Returns the provider adapter for the given type.
   *
   * @param type the provider type; must not be null
   * @return the corresponding adapter
   * @throws PaymentDomainException if no adapter is registered for the given type
   */
  public PaymentProviderPort get(ProviderType type) {
    var provider = registry.get(type);
    if (provider == null) {
      throw new PaymentDomainException("No payment provider registered for: " + type);
    }
    return provider;
  }

  /** Returns true if an adapter is registered for the given type. */
  public boolean isSupported(ProviderType type) {
    return registry.containsKey(type);
  }
}
