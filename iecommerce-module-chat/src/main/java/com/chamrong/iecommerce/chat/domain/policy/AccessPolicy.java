package com.chamrong.iecommerce.chat.domain.policy;

import com.chamrong.iecommerce.chat.domain.Conversation;

/** Who can see what: participant checks and staff overrides. */
public final class AccessPolicy {

  private AccessPolicy() {}

  /**
   * Returns true if the actor can read the conversation (is participant or has staff override).
   *
   * @param conversation the conversation
   * @param actorId the user/staff id
   * @param isStaff whether the actor has staff role
   */
  public static boolean canRead(Conversation conversation, Long actorId, boolean isStaff) {
    if (conversation == null || actorId == null) return false;
    if (conversation.hasParticipant(actorId)) return true;
    return isStaff;
  }

  /**
   * Returns true if the actor can send messages (must be participant; conversation must be open).
   */
  public static boolean canSend(Conversation conversation, Long actorId) {
    if (conversation == null || actorId == null) return false;
    return conversation.hasParticipant(actorId);
  }
}
