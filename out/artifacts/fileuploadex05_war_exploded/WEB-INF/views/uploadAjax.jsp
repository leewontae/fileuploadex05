<%--
  Created by IntelliJ IDEA.
  User: iwontae
  Date: 2021/11/03
  Time: 3:22 오후
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>insert title here</title>

    <style>

        .uploadResult{
            width:100%;
            background-color: gray;
        }
        .uploadResult ul{
            display: flex;
            flex-flow:row;
            justify-content:center;
            align-items: center;
        }
        .uploadResult ul li{
            list-style: none;
            padding:10px;
            align-content: center;
            text-align: center;
        }
        .uploadResult ul li img{
            width: 100px;
        }
        .uploadResult ul li span{
            color: white;
        }
        .bigPictureWrapper{
            position: absolute;
            display: none;
            justify-content: center;
            align-items: center;
            top: 0%;
            width: 100%;
            height: 100%;
            background-color: gray;
            z-index: 100;
            background: rgba(255,255,255,0.5);

        }
        bigPicture{
            position: relative;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .bigPicture img{
            width: 600px;
        }
    </style>

</head>
<body>
<h1> Upload with Ajax</h1>

<div class ='uplaodDiv'>
    <input type="file" name="uploadFile" multiple>
</div>

<div class="uploadResult">
    <ul>

    </ul>

</div>
<div class="bigPictureWrapper">
    <div class="bigPicture">

    </div>

</div>
<button id="uploadBtn">Upload</button>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>

<script>
    $(document).ready(function(){

        var regex = new RegExp("(.*?)\.(exe|sh|zip|alz)$");
        var maxSize = 5242880;

        function checkExtension(fileName,fileSize){

            if(fileSize>=maxSize){
                alert("파일 사이즈 초과");
                return false;
            }

            if(regex.test(fileName)){
                alert("해당 종류의 파일은 업로드 할 수 없습니다.");
                return false;
            }
            return true;
        }

        var cloneObj = $(".uplaodDiv").clone();

       $("#uploadBtn").on('click',function(e){

           var formData = new FormData();
           var inputFile = $("input[name='uploadFile']");
           var files = inputFile[0].files;

           console.log(files);
            for(var i=0; i< files.length; i++){

                if(!checkExtension(files[i].name, files[i].size)){
                    return false;
                }
                    formData.append("uploadFile", files[i]);
            }

            $.ajax({
                url:'/uploadFormAction',
                processData: false,
                contentType: false,
                data: formData,
                type: 'POST',
                dataType:'json',
                success:function(result){
                    console.log("json 받는 곳 ")
                   console.log(result);

                   showUploadFile(result);

                   $(".uplaodDiv").html(cloneObj.html());
                   // uploadDiv에 대한 크론을 만든 후 클릭 했을때 다시 쿠가 할수 있도록 리셋된 상황이다.
                }
            });
            /*첨부파일 데이터는 formData에 추가한 뒤에 Ajax를 통해서 formData자체를 전송합니다.*/
       }) ;

       var uploadResult = $(".uploadResult ul");
       function showUploadFile(uploadResultArr){

           var str="";

           $(uploadResultArr).each(function(i,obj){

               if(!obj.image){

                   var fileCallPath = encodeURIComponent(obj.uploadPath+"/"+obj.uuid+"_"+obj.fileName);

                   var fileLink = fileCallPath.replace(new RegExp(/\\/g),"/");


                   str+="<li><div><a href='/download?fileName="+fileCallPath+"'>"+"<img src='/resources/img/20-11.jpg'>"+obj.fileName+"</a>"+
                       "<span data-file=\'"+fileCallPath+"\' data-type='file'>x</span>"
                   +"<div></li>";

               }else{
                  // str+="<li>"+ obj.fileName+"</li>";

                   var fileCallPath = encodeURIComponent(obj.uploadPath+"/s_"+obj.uuid+"_"+obj.fileName);
                   // 브라우저에서 GET방식으로 첨부파일의 이름을 사용할 때에는 항상 파일 이름에 포함된 공백 문자나 한글 이름 등이 문제가 될수 있다.
                   // 이를 수정하기 위해 encodeURIComponent()를 이용해서 URI 호출에 적합한 문자열로 인코딩 처리해야 한다.
                    var originPath = obj.uploadPath +"\\"+obj.uuid+"_"+obj.fileName;
                    originPath =originPath.replace(new RegExp(/\\/g),"/");

                   str += "<li><a href=\"javascript:showImage(\'"+originPath+"\')\"><img src='/display?fileName="+fileCallPath+"'></a>"+
                       "<span data-file=\'"+fileCallPath+"\' data-type='image'>x</span>"+"<li>";
                   // controller에서 /display 경로로 가서 시작 됨과 동시에 javascript:showImage에 대한 fucntion이 실행되어 원본이미지를 div에 보여줄 것이다.
                   // 두번째 실습: 화면에서 삭제 기능 추가 하기
               }
           });
           uploadResult.append(str);
       }

    });

    // $(document).ready(function(){...}); 밖에 showimage 선언한 이유는 자웅에 <a> 태그 에서 직접 showimage()를 호출 할수 있게 하기 위함이다.
    function showImage(fileCallPath){
        //alert(fileCallPath); UUID포함된 파일 명
        $(".bigPictureWrapper").css("display","flex").show();

        $(".bigPicture")
        .html("<img src='/display?fileName=" + encodeURI(fileCallPath)+"'>")
        .animate({width:'100%',height:'100%'},1000);


    }
    $(".bigPictureWrapper").on("click",function(e){
        $(".bigPicture").animate({width:'0%',height:'0%'},1000);
        setTimeout(()=>{
            $(this).hide();
        },1000);
        // 크롬에서는 적용이 가능하지만 IE에서는 동작 하지 않는다.
        /*IE에서 적용 하기 위해서는

        $(".bigPicture").animate({width:'0%',height:'0%'},1000);
        setTimeout(function(){
           $('.bigPictureWrapper').hide();
        },1000);

        */
    })

    $(".uploadResult").on("click","span",function(e){
        var targetFile = $(this).data("file");
        var type=$(this).data("type");
        var data =$(this).parents("li");
        console.log("@@@@@@@@@@2"+data);
        console.log(targetFile);

        $.ajax({
            url:'/deleteFile',
            data:{fileName:targetFile,type:type},
            dataType: 'text',
            type:'POST',
            success:function(result) {
                alert(result);
                data.empty();
            }
        });
    });
</script>
</body>
</html>
