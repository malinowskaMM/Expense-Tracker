package pl.lodz.p.it.expenseTracker.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class MethodProcessingInterceptor implements HandlerInterceptor {

  private final LoggerService logger = new LoggerService();

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    logger.log("Processing of method started: " + request.getMethod(), request.getRequestURI(), LoggerService.LoggerServiceLevel.DEBUG);
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    logger.log("Processing of method ended: " + request.getMethod(), request.getRequestURI(), LoggerService.LoggerServiceLevel.DEBUG);
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
  }
}