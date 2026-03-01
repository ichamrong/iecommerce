package com.chamrong.iecommerce.sale.domain.specification;

public interface Specification<T> {
  boolean isSatisfiedBy(T t);

  default Specification<T> and(Specification<T> other) {
    return t -> isSatisfiedBy(t) && other.isSatisfiedBy(t);
  }
}
