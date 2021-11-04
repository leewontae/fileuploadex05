package org.zero.controller;


import lombok.extern.log4j.Log4j;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zero.domain.AttackFileDTO;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@Log4j
public class UploadController {

    @GetMapping(value = "/uploadForm")
    public void uploadForm(){
        log.info("upload form");
    }


    private String getFolder(){ //년/월/일 폴더 생성
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String str = sdf.format(date);

        log.info("str : " + str);

        return str.replace("-", File.separator);
    }

    private boolean checkImageType(File file){
        try{

            String contenType = Files.probeContentType(file.toPath());
            log.info(contenType);
            return contenType.startsWith("image");

        }catch (Exception e){
                e.getMessage();
        }
        return false;
    }

    @PostMapping(value="/uploadFormAction" , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    //뷰로 가지 않는다.
    public ResponseEntity<List<AttackFileDTO>> uplaodFormPost(MultipartFile[] uploadFile) {

        List<AttackFileDTO> list = new ArrayList<>();

        log.info("update ajax post.......");
        String uploadFolder = "/Users/iwontae/IdeaProjects/demo/fileuploadex05/upload/temp";

        String uploadFolderPath = getFolder();
        // 오늘 날짜로의 경로 출력
        log.info("uploadFolderPath :" + uploadFolderPath);
        File uploadPath = new File(uploadFolder, uploadFolderPath);
        //uploadPath : uploadFolder+uploadFolderPath 의 경로로 폴더 생성
        //파일경로
        log.info("upload path :" + uploadPath);

        if (uploadPath.exists() == false) {
            uploadPath.mkdirs();
        }
        //yyyy/mm/dd folder 만든다.
// 여기까지 

        for (MultipartFile multipartFile : uploadFile) {

            AttackFileDTO attackFileDTO = new AttackFileDTO();

            log.info("--------------------");
            log.info("Upload file name: " + multipartFile.getOriginalFilename());
            log.info("Upload File size" + multipartFile.getSize());

            String uploadFileName = multipartFile.getOriginalFilename();
            log.info("@@@@@@@@@@@@@@@" + uploadFileName + "@@@@@@@@@@@@@@@");

            uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\") + 1);
            // \\기준으로 마지막 파일명 추출
            log.info("only file name:" + uploadFileName);

            attackFileDTO.setFileName(uploadFileName);
            //파일 이름 저장

            UUID uuid = UUID.randomUUID();
            //이름이 중복된 파일은 삭제되므로 이를 방지하기 위해서 UUID를 사용한다.
            uploadFileName = uuid.toString() + "_" + uploadFileName;
            // 실제로 저장된 파일 이름
            log.info("uploadfilename" + uploadFileName);


            try {
                File saveFile = new File(uploadPath, uploadFileName);
                multipartFile.transferTo(saveFile);
                //바이트 형태의 데이터를 file객체에 설정한 파일 경로에 전송한다.

                attackFileDTO.setUuid(uuid.toString());
                attackFileDTO.setUploadPath(uploadFolderPath);

                if (checkImageType(saveFile)) {
                    //만일 이미지 타입이라면 섬네일을 생성하도록 한다.

                    attackFileDTO.setImage(true);

                    FileOutputStream thumbnail = new FileOutputStream(new File(uploadPath, "s_" + uploadFileName));

                    Thumbnailator.createThumbnail(multipartFile.getInputStream(), thumbnail, 100, 100);
                    // inputstream 과 outputstream , 넓이와 높이 이다.
                    thumbnail.close();

                }
                list.add(attackFileDTO);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return new ResponseEntity<>(list, HttpStatus.OK );
    }

    @GetMapping("/uploadAjax")
    public void uploadAjax(){
        log.info("upload ajax");
    }

    @GetMapping("/display")
    @ResponseBody
    public ResponseEntity<byte[]> getFile(String fileName){
        log.info("fileName:"+fileName);

        File file = new File("/Users/iwontae/IdeaProjects/demo/fileuploadex05/upload/temp/"+fileName);

        ResponseEntity<byte[]> result = null;
        try{

            HttpHeaders header = new HttpHeaders();
            header.add("Content-Type",Files.probeContentType(file.toPath()));
            result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file),header,HttpStatus.OK);
            log.info("@@@@@@@@@result@@@@@@@@@ "+result);
            //<200 OK OK,[B@5db60dba,[Content-Type:"image/jpeg"]>

        }catch(IOException e){
            e.printStackTrace();
        }

       return result;
    }


    @GetMapping(value = "/download",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    //MIME 타입은 다운로드 할수 있는 application/octet-stream으로 지정하고 다운로드 시 저장되는 이름은 Content-Disposition을 이용해서 지정한다.
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@RequestHeader("User-Agent")String userAgent, String fileName){
        // ResponseEntity<byte[]>로 대체 가능 하다.
        // resources 타입을 이용해서 좀더  간단히 처리 하도록 한다.
        // 첨부파일 다운로드는 서버에서 MIME 타입을 다운로드 타입으로 지정하고 , 적절한 헤더 메시지를 통해서 다운로드 이름을 지정하게 처리 한다.
        log.info("download file :" + fileName);

        Resource resource = new FileSystemResource("/Users/iwontae/IdeaProjects/demo/fileuploadex05/upload/temp/"+fileName);
        //경로에 있는 파일을 resource에 대입 시킨다.
        log.info("resource: " + resource);

        if(resource.exists() ==false){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String resourceName = resource.getFilename();
        log.info("resourceName: " + resourceName);
        String resourceOriginalName = resourceName.substring(resourceName.indexOf("_")+1);
        log.info("resourceOriginalName: " + resourceOriginalName);

        HttpHeaders headers = new HttpHeaders();

        try{
    // userAgent를 이용하여 어떤 브라우저인지 확인 하고 그에 맞는 인코딩 방식을 적용 한다.
            String downloadName = null;
            if(userAgent.contains("Trident")){
                log.info("IE browser");
                downloadName = URLEncoder.encode(resourceOriginalName,"UTF-8").replaceAll("/+","");
            }else if(userAgent.contains("Edge")){
                log.info("Edge browser");
                downloadName = URLEncoder.encode(resourceOriginalName,"UTF-8");
                log.info("Edge name:" + downloadName);
            }else {
                log.info("chrome browser");
                downloadName = new String (resourceOriginalName.getBytes("UTF-8"),"ISO-8859-1");
                // resourceOriginalName : UUID부분을 잘라낸 상태의 파일 이름으로 저장 하기위해 resourceOriginalname 으로 한것이다.
                // 이전 예제는 UUID가 포함된 resourceName 으로 했다.
                log.info("downloadName"+downloadName);
                // 파일 이름
            }
            headers.add("Content-Disposition","attachment; filename="+ downloadName);

            //headers.add("Content-Disposition","attachment; filename="+ new String(resourceName.getBytes("UTF-8"),"ISO-8859-1"));
         //MIME 타입은 다운로드 할수 있는 application/octet-stream으로 지정하고 다운로드 시 저장되는 이름은 Content-Disposition을 이용해서 지정한다.
         // 저장되는 이름을 Content-dispodition으로 지정하는 이유는  파일이름이 한글인 경우 저장할 떄 깨지는 문제를 막기 위해서 이다.
         // ~upload/temp 폴더에 있는 파일의 이름과 확장자로 /download?fileName=test.jsp' 와 같이 호풀 하면 브라우저는 자동으로 해당 파일을 다운로드 하는것을 볼수 있다.

            //문제 : IE브라우저로 첨부파일 다운로드 시 한글 이름이 깨지는 경우가 있다.
            /*IE브라우저로 서비스를 운영해야 한다면  HttpServletRequest에 포함된 헤더 정보를을 이용해서 요청이 발생한 브라우저가  IE계열인지 확인하여 맞다면 다른 방식으로 처리한다.
            HTTP 헤더 메시지 중에서 디바이스의 정보를 알수 있는 헤더는 User-Agent값을 이용한다.*/

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return  new ResponseEntity<Resource>(resource,headers,HttpStatus.OK) ;
    }
    @PostMapping("/deleteFile")
    @ResponseBody
    public ResponseEntity<String> deleteFile(String fileName, String type){
        log.info("deleteFile:"+ fileName);
        File file;
        try{

            String a = URLDecoder.decode(fileName,"UTF-8");
            log.info("@@@@@a@@@@@@@"+ a);
            // 2021/11/04/s_aab9ae88-cca9-4434-b66d-8c6bc81c349b_20-11.jpg

            file = new File("/Users/iwontae/IdeaProjects/demo/fileuploadex05/upload/temp/"+ URLDecoder.decode(fileName,"UTF-8"));
            file.delete();
            if(type.equals("image")){
                String largeFileName = file.getAbsolutePath().replace("s_","");
                log.info("largeFileName:"+ largeFileName);
                file = new File(largeFileName);
                file.delete();
            }

        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>("deleted",HttpStatus.OK);
    }
}


