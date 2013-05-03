<%--
  Created by IntelliJ IDEA.
  User: Gabriel
  Date: 17.12.12
  Time: 13:25
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<html>
<head>
    <title>Create Need</title>
</head>
<body>
    <h1>New need</h1>

    <form:form method="POST" action="create">
        <table>
            <tr>
                <td><form:label path="title">Need title:</form:label></td>
                <td><form:input path="title" /></td>
            </tr>
            <tr>
                <td><form:label path="basicNeedType">Basic type:</form:label></td>
                <td><form:select path="basicNeedType">
                    <form:options items="${enumValues}" />
                </form:select></td>
            </tr>
            <tr>
                <td><form:label path="state">Need state:</form:label></td>
                <td><form:select path="state">
                    <form:options items="${enumValues}" />
                </form:select></td>
            </tr>
            <tr>
                <td><form:label path="anonymize">Anonymize owner:</form:label></td>
                <td><form:checkbox path="anonymize" /></td>
            </tr>
            <tr>
                <td><form:label path="wonNode">WoN Node:</form:label></td>
                <td><form:input path="wonNode" /></td>
            </tr>
            <tr>
                <td><form:label path="textDescription">Text description:</form:label></td>
                <td><form:textarea path="textDescription" rows="5" cols="30" /></td>
            </tr>
            <tr>
                <td><form:label path="upperPriceLimit">Upper price limit:</form:label> </td>
                <td><form:input path="upperPriceLimit" /></td>
            </tr>
            <tr>
                <td><form:label path="lowerPriceLimit">Lower price limit:</form:label> </td>
                <td><form:input path="lowerPriceLimit" /></td>
            </tr>
            <tr>
                <td><form:label path="currency">Currency</form:label> </td>
                <td><form:input path="currency" /></td>
            </tr>
            <tr>
                <td><form:label path="deliveryMethod">Delivery method</form:label> </td>
                <td><form:select path="deliveryMethod">
                    <form:options items="${enumValues}" />
                </form:select></td>
            </tr>

            <tr>
                <td><form:label path="latitude">Latitude:</form:label></td>
                <td><form:input path="latitude" /></td>
            </tr>
            <tr>
                <td><form:label path="longitude">Longitude:</form:label></td>
                <td><form:input path="longitude" /></td>
            </tr>
            <tr>
                <td><form:label path="startTime">Start time:</form:label></td>
                <td><form:input path="startTime" /></td>
            </tr>
            <tr>
                <td><form:label path="endTime">End time:</form:label></td>
                <td><form:input path="endTime" /></td>
            </tr>
            <tr>
                <td><form:label path="recurIn">Recur in:</form:label></td>
                <td><form:input path="recurIn" /></td>
            </tr>
            <tr>
                <td><form:label path="recurTimes">Recur times:</form:label></td>
                <td><form:input path="recurTimes" /></td>
            </tr>
            <tr>
                <td><form:label path="recurInfiniteTimes">Recur inifinite times:</form:label></td>
                <td><form:checkbox path="recurInfiniteTimes" /></td>
            </tr>
            <tr>
                <td colspan="2"><input type="submit" value="Submit"/></td>
            </tr>
        </table>
    </form:form>
</body>
</html>