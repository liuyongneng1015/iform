package tech.ascs.icity.iform.controller;

import io.swagger.annotations.Api;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.iform.ICityIFormApplication;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.FileUploadModel;
import tech.ascs.icity.iform.api.model.DataSourceType;
import tech.ascs.icity.iform.api.service.FileUploadService;
import tech.ascs.icity.iform.service.UploadService;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tech.ascs.icity.iform.utils.OkHttpUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.*;

@Api(tags = "文件上传服务", description = "文件上传服务")
@RestController
public class FileUploadController implements FileUploadService {

	private Logger log = LoggerFactory.getLogger(FileUploadController.class);

	@Autowired
	private UploadService uploadService;

	public FileUploadModel fileUpload(HttpServletRequest request) {
		log.error("fileUpload in ");
		MultipartFile file = ((MultipartHttpServletRequest)request).getFile("file");
		String fileSizeLimit = request.getParameter("fileSizeLimit");
		String sourceTypeStr = request.getParameter("sourceType");
		verifyFileFormat(file, request);
		DataSourceType sourceType = DataSourceType.getDataSourceType(sourceTypeStr);
		Integer size = null;
		if(StringUtils.isNoneBlank(fileSizeLimit)){
			size = Integer.parseInt(fileSizeLimit);
		}
		try {
			return uploadService.uploadOneFileReturnUrl(null, sourceType, size, file);
		} catch (Exception e) {
			throw new IFormException("上传文件失败:" + e.getMessage());
		}
	}

	public List<FileUploadModel> batchFileUpload(HttpServletRequest request) {
		List<MultipartFile> files =((MultipartHttpServletRequest)request).getFiles("file");
		String fileSizeLimit = request.getParameter("fileSizeLimit");
		String sourceTypeStr = request.getParameter("sourceType");
		DataSourceType sourceType = DataSourceType.getDataSourceType(sourceTypeStr);
		Integer size = null;
		if(StringUtils.isNoneBlank(fileSizeLimit)){
			size = Integer.parseInt(fileSizeLimit);
		}
		List<FileUploadModel> list = new ArrayList<>();
		if(files != null && files.size() > 0) {
			for (MultipartFile file : files){
				verifyFileFormat(file, request);
			}
			for (MultipartFile file : files){
				try {
					list.add(uploadService.uploadOneFileReturnUrl(null, sourceType, size, file));
				} catch (Exception e) {
					throw new IFormException("上传文件失败:" + e.getMessage());
				}
			}
		}
		return list;
	}

	private void verifyFileFormat(MultipartFile file, HttpServletRequest request){
		String filename = file.getOriginalFilename();
		String format = filename.substring(filename.lastIndexOf(".")+1);
		String fileFormat = request.getParameter("fileFormat");
		if(StringUtils.isNotBlank(fileFormat) && !fileFormat.contains(format)){
			throw new IFormException("上传文件失败:文件类型错误");
		}
	}



	public String downloadTemplate(HttpServletResponse response, HttpServletRequest request) {
		String fileUrl = request.getParameter("url");
		String name = request.getParameter("name");

		try {
			if (fileUrl != null) {
				//设置文件路径
				URL url = new URL(fileUrl);
				InputStream inputStream = null;
				if (url != null) {
					inputStream = url.openStream();
					response.setContentType("application/force-download");// 设置强制下载不打开
					String end = fileUrl.substring(fileUrl.lastIndexOf(".")+1);
					String filePath = StringUtils.isNotBlank(name) ? name : fileUrl.substring(fileUrl.lastIndexOf("/")+1);
					if(!filePath.endsWith(end)){
						filePath = filePath+"."+end;
					}
					response.addHeader("Content-Disposition", "attachment;fileName=" + filePath);// 设置文件名
					byte[] buffer = new byte[2048];
					BufferedInputStream bis = null;
					try {
						bis = new BufferedInputStream(inputStream);
						OutputStream os = response.getOutputStream();
						int i = bis.read(buffer);
						while (i != -1) {
							os.write(buffer, 0, i);
							i = bis.read(buffer);
						}
						return "下载成功";
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (bis != null) {
							IOUtils.closeQuietly(bis);
						}
						if (inputStream != null) {
							IOUtils.closeQuietly(inputStream);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "下载失败";
	}

	@Override
	public List<Map<String, Object>> parseExcel(HttpServletRequest request) {
		MultipartFile file = ((MultipartHttpServletRequest)request).getFile("file");
		return uploadService.parseExcel(file);
	}

	private static final String OS = System.getProperty("os.name").toLowerCase();

	@Override
	public void downloadManyFiles(HttpServletResponse response, @RequestBody List<FileUploadModel> files) {
		if (files==null || files.size()==0) {
			throw new IFormException("要下载的文件不能为空");
		}
        String uuid = UUID.randomUUID().toString();
        String rootPath = null;
        if (OS.startsWith("win")) {          // Windows系统
            rootPath = "C:"+File.separator+uuid;
        } else if (OS.startsWith("linux")) { // linux系统
            rootPath = "/tmp/"+File.separator+uuid;
        } else if (OS.startsWith("mac")) {   // mac系统
            rootPath = "/tmp/"+File.separator+uuid;
        } else {
            return;
        }
        File tmpDir = new File(rootPath+File.separator+uuid);
        boolean result = tmpDir.mkdirs();
        if (result==false) {
            throw new IFormException("服务器创建文件夹失败");
        }


        /**
        List<FileUploadModel> list = new ArrayList();
        list.add(new FileUploadModel("aa", "http://192.168.4.151:9000/icity/2019/02/22/011ddbc7-2a49-46e1-9457-80bac0af78d0.jpg"));
        list.add(new FileUploadModel("aa", "http://192.168.4.151:9000/icity/2019/02/22/083af844-886a-4957-9b78-f8796707cfa9.jpg"));
        list.add(new FileUploadModel("aa", "http://192.168.4.151:9000/icity/2019/02/22/09364129-7ea4-43ac-b93b-231aa6d72627.jpg"));
        list.add(new FileUploadModel("bb", "http://192.168.4.151:9000/icity/2019/02/22/5644736c-b0f1-4713-9482-318ee768d56e.jpg"));
        list.add(new FileUploadModel("bb", "http://192.168.4.151:9000/icity/2019/02/22/d4c14b49-9d95-4b20-ba25-af011dda6ea1.jpg"));
         */
        Map<String, Integer> map = new HashMap();
        for (FileUploadModel fileInfo:files) {
            String filename = fileInfo.getName();
            Integer count = map.get(filename);
            if (count!=null) {
                filename = filename + "-" + count;
                map.put(filename, ++count);
            } else {
                map.put(filename,1);
            }
            String url = fileInfo.getUrl();
            int index = url.lastIndexOf(".");
            if (index>0) {
                filename = filename+url.substring(index);
            }
            OkHttpUtils.downloadFile(url, rootPath+File.separator+uuid, filename);
        }
        deleteDir(rootPath);
    }

    public static void deleteDir(String path){
        File file = new File(path);
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(int i=0; i<files.length; i++) {
                deleteDir(files[i].getPath());
            }
        }
        file.delete();
    }
}
