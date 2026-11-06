package com.Timo.Timo.global.analytics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnalyticsEvent {

  SIGN_UP_COMPLETED("sign_up_completed"),
  LOGGED_IN("logged_in"),
  ONBOARDING_COMPLETED("onboarding_completed"),
  PROFILE_UPDATED("profile_updated"),
  TIMEZONE_UPDATED("timezone_updated"),
  LOGGED_OUT("logged_out"),
  ACCOUNT_WITHDRAWN("account_withdrawn"),

  TODO_CREATED("todo_created"),
  TODO_COMPLETED("todo_completed"),
  TODO_UNCOMPLETED("todo_uncompleted"),
  SUBTASK_COMPLETED("subtask_completed"),
  TODO_UPDATED("todo_updated"),
  TODO_MEMO_UPDATED("todo_memo_updated"),
  TODO_REORDERED("todo_reordered"),
  TODO_DELETED("todo_deleted"),
  AI_DURATION_PREDICTED("ai_duration_predicted"),

  TIMER_STARTED("timer_started"),
  TIMER_PAUSED("timer_paused"),
  TIMER_RESUMED("timer_resumed"),
  TIMER_SWITCHED("timer_switched"),
  TIMER_EXTENDED("timer_extended"),
  TIMER_COMPLETED("timer_completed"),
  TIMER_STOPPED("timer_stopped"),

  TAG_CREATED("tag_created"),
  TAG_DELETED("tag_deleted"),
  CALENDAR_CONNECTED("calendar_connected"),
  CALENDAR_DISCONNECTED("calendar_disconnected"),

  HOME_VIEWED("home_viewed"),
  TODAY_VIEWED("today_viewed"),
  TODO_DETAIL_VIEWED("todo_detail_viewed"),
  STATISTICS_VIEWED("statistics_viewed"),
  FOCUS_VIEWED("focus_viewed"),
  TIMEBOX_VIEWED("timebox_viewed"),
  CALENDAR_EVENTS_VIEWED("calendar_events_viewed"),
  PROFILE_VIEWED("profile_viewed"),
  TERMS_VIEWED("terms_viewed");

  private final String eventName;
}
