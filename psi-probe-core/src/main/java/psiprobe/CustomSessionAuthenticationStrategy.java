/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package psiprobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

public class CustomSessionAuthenticationStrategy implements SessionAuthenticationStrategy {
  private final SessionCreationPolicy policy;

  public CustomSessionAuthenticationStrategy(SessionCreationPolicy policy) {
    this.policy = policy;
  }

  @Override
  public void onAuthentication(Authentication authentication, HttpServletRequest request,
      HttpServletResponse response) throws SessionAuthenticationException {
    if (policy == SessionCreationPolicy.ALWAYS) {
      request.getSession(true); // force session
    } else if (policy == SessionCreationPolicy.IF_REQUIRED) {
      // Only create session if needed elsewhere (Spring Security default behavior)
    } else if (policy == SessionCreationPolicy.NEVER || policy == SessionCreationPolicy.STATELESS) {
      if (request.getSession(false) != null) {
        throw new SessionAuthenticationException(
            "Session is not allowed under current policy: " + policy);
      }
    }
  }

}
