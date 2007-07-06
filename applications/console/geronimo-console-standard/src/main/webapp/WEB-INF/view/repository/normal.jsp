<%--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>

<%-- $Rev$ $Date$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<c:set var="reslist" value="${requestScope['org.apache.geronimo.console.repo.list']}"/>

<style type="text/css">  
    div.Hidden {
    display: none;
    }
    
    div.Shown {
    display: block;
    font-size: 10px;
    }
</style>  

<script language="JavaScript">
function <portlet:namespace/>validate() {
   if (! (document.<portlet:namespace/>fileSelect.local.value 
      && document.<portlet:namespace/>fileSelect.group.value 
      && document.<portlet:namespace/>fileSelect.artifact.value 
      && document.<portlet:namespace/>fileSelect.version.value 
      && document.<portlet:namespace/>fileSelect.fileType.value))
   {
      alert("File, Group, Artifact, Version, and Type are all required fields");
      return false;
   }
}

function <portlet:namespace/>parse(localFile) {
    // Split the path
    var pathParts = localFile.split("\\"); // Assuming windows file delim
    if(localFile.indexOf("/") >= 0) // check if *nix 
        pathParts = localFile.split("/");
    basename = pathParts[pathParts.length - 1]; // Last part is the base file name

    // Attempt to get the artifact and type from the basename
    // This regular expression for repository filename is taken from Maven1Repository.MAVEN_1_PATTERN
    regExpStr = "(.+)-([0-9].+)\\.([^0-9]+)";
    var fileRegExp = new RegExp(regExpStr, "g");
    if(basename.match(fileRegExp) != null) {
        // base file name matches the regular expression
        fileRegExp.compile(regExpStr, "g");
        var fileParts = fileRegExp.exec(basename);
        fileType = fileParts[fileParts.length - 1];
        version = fileParts[fileParts.length - 2];
        artifact = fileParts[fileParts.length - 3];
        document.<portlet:namespace/>fileSelect.fileType.value = fileType;
        document.<portlet:namespace/>fileSelect.version.value = version;
        document.<portlet:namespace/>fileSelect.artifact.value = artifact;

        // Attempt to suggest the group
        if(pathParts.length >= 3 && pathParts[pathParts.length-2] == fileType +'s') {
            // Maven1Repository
            document.<portlet:namespace/>fileSelect.group.value = pathParts[pathParts.length-3];
        } else if(pathParts.length >= 4 && pathParts[pathParts.length-2] == version && pathParts[pathParts.length-3] == artifact) {
            // Maven2Repository
            document.<portlet:namespace/>fileSelect.group.value = pathParts[pathParts.length-4];
        } else {
            document.<portlet:namespace/>fileSelect.group.value = '';
        }
    } else {
        document.<portlet:namespace/>fileSelect.fileType.value = '';
        document.<portlet:namespace/>fileSelect.version.value = '';
        document.<portlet:namespace/>fileSelect.artifact.value = '';
        document.<portlet:namespace/>fileSelect.group.value = '';
    }
}

</script>

<table width="100%">
<tr>
  <td align="center">
  <form onsubmit="return <portlet:namespace/>validate();" enctype="multipart/form-data" name="<portlet:namespace/>fileSelect" method="POST" action="<portlet:actionURL><portlet:param name="action" value="deploy"/></portlet:actionURL>">
  <table>
    <tr>
      <th colspan="2">Add Archive to Repository</th>
    </tr>
    <tr>
      <td>File</td>
      <td><input name="local" onchange="<portlet:namespace/>parse(value);" type="file">&nbsp;&nbsp;&nbsp;</td>
    </tr>
    <tr>
      <td>Group:</td>
      <td><input type="text" name="group" value="${group}"/></td>
    </tr>
    <tr>
      <td>Artifact:</td>
      <td><input type="text" name="artifact" value="${artifact}"/></td>
    </tr>
    <tr>
      <td>Version:</td>
      <td><input type="text" name="version" value="${version}"/></td>
    </tr>
    <tr>
      <td>Type:</td>
      <td><input type="text" name="fileType" value="${fileType}"/></td>
    </tr>
    <tr><td colspan="2"><font size="-2">&nbsp;</font></td></tr>
    <tr>
      <td colspan="2" align="center"><input type="submit" value="Install" /></td>
    </tr>
  </table>
  </form>
  </td>
</tr>
</table>

<b>Current Repository Entries</b>
<p>Click on an entry to view usage.</p>
<ul>
<c:forEach items="${reslist}" var="res">
<li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="usage"/><portlet:param name="res" value="${res}"/></portlet:actionURL>"><c:out value="${res}"/></a></li>
</c:forEach>
</ul>
