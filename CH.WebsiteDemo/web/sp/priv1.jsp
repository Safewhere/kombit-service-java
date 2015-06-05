<!-- "$Id: priv1.jsp 3040 2008-06-23 15:34:36Z jre $"; -->
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="dk.itst.oiosaml.sp.UserAssertionHolder"%>
<%@page import="dk.itst.oiosaml.sp.service.util.Utils"%>
<%@page import="dk.itst.oiosaml.sp.util.AttributeUtil"%>
<%@page import="dk.itst.oiosaml.sp.UserAssertion"%>
<%@page import="dk.itst.oiosaml.sp.UserAttribute"%>
<%@page import="dk.itst.oiosaml.sp.service.util.Constants"%>
<%@page import="BasicPrivilegeProfileParser.PrivilegeGroupParser"%>
<%@page import="BasicPrivilegeProfileParser.PrivilegeGroup"%>
<%@page import="java.util.ArrayList"%>

	<% UserAssertion ua = UserAssertionHolder.get(); %>

    <jsp:include page="/head.jsp" />
    <div style="float: left">
    <h1>User NameID</h1>
    <%= ua.getSubject() %>
    
    <h1>Attributes on UserAssertion</h1>
    <ul>
   <!-- loop through each attribute if name is Bpp claim then parse and get Bpp string value -->
    <% 
    boolean hasAssuranceLevel = false;
    boolean hasSpecVer = false;
    boolean hasKombitSpecVer = false;
    boolean hasCvrNumberIdentifier = false;
    boolean hasPrivilegesIntermediate = false;
    
    for (UserAttribute a : ua.getAllAttributes()) {
        String name = a.getName();
        String value = a.getValue();
        if(name.equals("dk:gov:saml:attribute:Privileges_intermediate"))
        {
            hasPrivilegesIntermediate = true;

            try
            {
            ArrayList<PrivilegeGroup> bppGroupsList = 
                (ArrayList<PrivilegeGroup>)PrivilegeGroupParser.Parse(value); 
                value = PrivilegeGroupParser.ConvertToJsonString(bppGroupsList);
            }catch (Exception ex)
            {
                value = a.getValue();
            }
        }
        if(name.equals("dk:gov:saml:attribute:AssuranceLevel"))
        {
            hasAssuranceLevel = true;
        }
        if(name.equals("dk:gov:saml:attribute:SpecVer"))
        {
            hasSpecVer = true;
        }
        if(name.equals("dk:gov:saml:attribute:KombitSpecVer"))
        {
            hasKombitSpecVer = true;
        }
        if(name.equals("dk:gov:saml:attribute:CvrNumberIdentifier"))
        {
            hasCvrNumberIdentifier = true;
        }
    %> 
    <!-- display each name - value in screen -->
    <li><%= name %> - <%= value %> </li>
    <%
    }
    
    if(!hasAssuranceLevel)
    {
        throw new Exception("Missing Assurance Level Attribute");
    }
    if(!hasSpecVer)
    {
        throw new Exception("Missing SpecVer Attribute");
    }
    if(!hasKombitSpecVer)
    {
        throw new Exception("Missing KombitSpecVer Attribute");
    }
    if(!hasCvrNumberIdentifier)
    {
        throw new Exception("Missing CvrNumberIdentifier Attribute");
    }    
    
    %>
    </ul>   
    <p>
    Authenticated: <%= ua.isAuthenticated() %><br />
    Assertion signed: <%= ua.isSigned() %><br />
    SAML Profile: <%= ua.isOIOSAMLCompliant() %><br />
    OCES Attribute Profile: <%= ua.isOCESProfileCompliant() %><br />
    Persistent Pseudonym Profile: <%= ua.isPersistentPseudonymProfileCompliant() %><br />
     
    </p>
    <a href="query.jsp">Perform attribute query</a> &nbsp; <a href="logout.jsp">Local logout</a> &nbsp; <a href="<%= request.getRequestURL() %>?<%= Constants.QUERY_STRING_FORCE_AUTHN %>=True">Force login</a><br />
    </div>
	<div style="float: right"><img src="<%= request.getContextPath() %>/oiosaml.gif" alt="oiosaml.java" /></div>
	<div style="clear: both">&nbsp;</div>
    <h1>Assertion:</h1><span style="font-size:80%; font-family:Monospace;">
    <%= Utils.beautifyAndHtmlXML(UserAssertionHolder.get().getXML(), "&nbsp;&nbsp;&nbsp;&nbsp;") %>
  </span>
  
  <br />
  <% if (!UserAssertionHolder.get().isAuthenticated()) { %>
  <a href="<%= request.getContextPath() %>/saml/login">Force login</a><br />
  <% } %>
  
  </body>
</html>
