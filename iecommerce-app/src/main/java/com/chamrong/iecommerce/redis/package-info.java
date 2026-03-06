/**
 * Placeholder package for Spring Data Redis repository scanning. The application uses Redis for
 * caching/locking only; JPA repositories are in other packages. Limiting
 * {@code @EnableRedisRepositories(basePackages = "com.chamrong.iecommerce.redis")} avoids "Could
 * not safely identify store assignment" INFO logs for every JPA repository.
 */
package com.chamrong.iecommerce.redis;
