<%--

    Licensed under the GPL License. You may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      https://www.gnu.org/licenses/old-licenses/gpl-2.0.html

    THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
    WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
    PURPOSE.

--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="https://github.com/psi-probe/psi-probe/jsp/tags" prefix="probe" %>

<%-- Displays a list of web application listeners --%>

<html>

    <head>
        <title>
            <spring:message htmlEscape="true" code="probe.jsp.title.app.listeners" arguments="${param.webapp}"/>
        </title>
    </head>

    <%-- Make Tab #1 visually "active". --%>
    <c:set var="navTabApps" value="active" scope="request"/>
    <c:set var="use_decorator" value="application" scope="request"/>
    <c:set var="appTabListeners" value="active" scope="request"/>

    <body>

        <ul class="options">
            <li id="viewAppListenerMaps">
                <a href="<c:url value='/applistenermaps.htm'><c:param name='webapp' value='${param.webapp}'/></c:url>">
                    <spring:message code="probe.jsp.app.listeners.opt.maps"/>
                </a>
            </li>
        </ul>

        <div class="embeddedBlockContainer">
            <c:choose>
                <c:when test="${! empty appListeners}">

                    <h3><spring:message code="probe.jsp.app.listeners.h3.defs"/></h3>

                    <display:table htmlId="listenerTbl" name="appListeners" uid="fltr"
                            class="genericTbl" style="padding:0;border-spacing:0;border-collapse:separate;"
                            requestURI="" defaultsort="1">
                        <display:column property="listenerName" sortable="true"
                                titleKey="probe.jsp.app.listeners.col.listenerName" maxLength="40" class="leftmost"/>
                        <display:column property="listenerClass" sortable="true"
                                titleKey="probe.jsp.app.listeners.col.listenerClass" maxLength="50"/>
                        <display:column titleKey="probe.jsp.app.listeners.col.listenerDesc" sortable="false">
                            <probe:out value="${ftlr.listenerDesc}" maxLength="50"/>&#160;
                        </display:column>
                    </display:table>
                </c:when>
                <c:otherwise>
                    <div class="infoMessage">
                        <p>
                            <spring:message code="probe.jsp.app.listeners.empty"/>
                        </p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </body>
</html>
