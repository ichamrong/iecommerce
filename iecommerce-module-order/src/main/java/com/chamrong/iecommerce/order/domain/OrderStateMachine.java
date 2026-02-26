package com.chamrong.iecommerce.order.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * State machine definition for the Order lifecycle.
 *
 * <p><b>Banking/Insurance pattern — Finite State Machine (FSM) with explicit transition table.</b>
 *
 * <p>Rather than scattering {@code if (state != X) throw ...} checks across every domain method, we
 * define the entire valid-transition graph in a single read-only {@link Map}. This is the same
 * approach used in banking transaction processing engines and insurance policy lifecycle
 * management:
 *
 * <ul>
 *   <li>The table is the single source of truth. A new engineer cannot accidentally add a
 *       transition without updating this class.
 *   <li>The {@link EnumMap} data structure gives O(1) lookup backed by an array — faster than a
 *       {@code HashMap} because keys are enum ordinals.
 *   <li>All sets are wrapped with {@link java.util.Collections#unmodifiableSet} — the transition
 *       table is truly immutable at runtime.
 * </ul>
 *
 * <p>Terminal states ({@link OrderState#Completed}, {@link OrderState#Cancelled}) intentionally
 * have empty allowed-target sets, making transitions out of them impossible by construction.
 */
public final class OrderStateMachine {

  /** Singleton lookup table. Built once at class load time, never mutated. */
  private static final Map<OrderState, Set<OrderState>> TRANSITIONS;

  static {
    final Map<OrderState, Set<OrderState>> t = new EnumMap<>(OrderState.class);
    t.put(
        OrderState.AddingItems,
        java.util.Collections.unmodifiableSet(
            EnumSet.of(OrderState.Confirmed, OrderState.Completed, OrderState.Cancelled)));
    t.put(
        OrderState.Confirmed,
        java.util.Collections.unmodifiableSet(
            EnumSet.of(OrderState.Picking, OrderState.PaymentSettled, OrderState.Cancelled)));
    t.put(
        OrderState.PaymentSettled,
        java.util.Collections.unmodifiableSet(
            EnumSet.of(
                OrderState.Picking,
                OrderState.Shipped,
                OrderState.Completed,
                OrderState.Cancelled)));
    t.put(
        OrderState.Picking,
        java.util.Collections.unmodifiableSet(
            EnumSet.of(OrderState.Packing, OrderState.Cancelled)));
    t.put(
        OrderState.Packing,
        java.util.Collections.unmodifiableSet(
            EnumSet.of(OrderState.Shipped, OrderState.Cancelled)));
    t.put(
        OrderState.Shipped,
        java.util.Collections.unmodifiableSet(
            EnumSet.of(OrderState.Delivered, OrderState.Cancelled)));
    t.put(
        OrderState.Delivered,
        java.util.Collections.unmodifiableSet(
            EnumSet.of(OrderState.Completed, OrderState.Cancelled)));
    t.put(
        OrderState.Completed,
        java.util.Collections.unmodifiableSet(EnumSet.noneOf(OrderState.class)));
    t.put(
        OrderState.Cancelled,
        java.util.Collections.unmodifiableSet(EnumSet.noneOf(OrderState.class)));
    TRANSITIONS = java.util.Collections.unmodifiableMap(t);
  }

  private OrderStateMachine() {} // utility class — prevent instantiation

  /**
   * Validates that a transition from {@code current} to {@code next} is legal.
   *
   * @throws OrderTransitionException if the transition is forbidden
   */
  public static void assertCanTransition(final OrderState current, final OrderState next) {
    final Set<OrderState> allowed = TRANSITIONS.getOrDefault(current, Set.of());
    if (!allowed.contains(next)) {
      throw new OrderTransitionException(current, next, allowed);
    }
  }

  /** Returns the valid next states from the given state (for documentation/UI purposes). */
  public static Set<OrderState> allowedTransitions(final OrderState from) {
    return TRANSITIONS.getOrDefault(from, Set.of());
  }
}
