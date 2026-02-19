package com.chamrong.iecommerce.common;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SoftDeleteAspect {

  @AfterReturning(pointcut = "bean(entityManager)", returning = "entityManager")
  public void enableSoftDeleteFilter(EntityManager entityManager) {
    Session session = entityManager.unwrap(Session.class);
    session.enableFilter("softDeleteFilter").setParameter("isDeleted", false);
  }
}
