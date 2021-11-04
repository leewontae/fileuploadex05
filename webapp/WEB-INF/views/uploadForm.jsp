<%--
  Created by IntelliJ IDEA.
  User: iwontae
  Date: 2021/11/03
  Time: 2:52 오후
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>insert title here</title>
</head>
<body>

<form action="uploadFormAction" method="post" enctype="multipart/form-data">
    <input type="file" name="uploadFile" multiple>
<%--<input type='file'>의 경우  multiple이라는 속성을 이용하면 하나의<input>태그로 한꺼번에 여러개의 파일을 업로드 할수 있다.
button 클릭시 uploadFormAction의 경로로 간다. --%>
    <button>submit</button>
</form>
</body>
</html>
