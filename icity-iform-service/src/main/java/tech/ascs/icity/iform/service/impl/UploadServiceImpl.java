package tech.ascs.icity.iform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.MinioClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.FileUploadModel;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.service.ItemModelService;
import tech.ascs.icity.iform.service.UploadService;
import tech.ascs.icity.iform.utils.CommonUtils;
import tech.ascs.icity.iform.utils.ImagesUtils;
import tech.ascs.icity.iform.utils.MinioConfig;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UploadServiceImpl extends DefaultJPAService<ColumnModelEntity> implements UploadService {

	@Autowired
	private MinioClient minioClient;

	@Autowired
	private MinioConfig minioConfig;

	@Autowired
	public ObjectMapper mapper;

	public UploadServiceImpl() {
		super(ColumnModelEntity.class);
	}

	/**
	 * 图片的base64字符串集合转成图片，并上传到minio，然后返回url
	 *
	 * @param base64List
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<String> base64ToImgAndUpload(List<String> base64List) {
		List<String> images = new ArrayList<>();
		try {
			if (base64List == null || base64List.size() == 0)
				return images;
			for (String img : base64List) {
				if (!StringUtils.isEmpty(img)) {
					images.add(base64ToImgAndUploadOne(img));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return images;
	}

	@Override
	public String getFileUrl(String key) {
		return minioConfig.getUrl()+"/"+minioConfig.getBucket()+"/"+key ;
	}

	/**
	 * 上传文件，并显示是否重命名
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@Override
	public FileUploadModel uploadOneFileReturnUrl(Integer fileSize, MultipartFile file) throws Exception {
		FileUploadModel fileUploadModel = null;
		InputStream inputStream = file.getInputStream();
		try {

			String filename = file.getOriginalFilename();
			String day = CommonUtils.date2Str(new Date(), "yyyy-MM-dd");
			String filePath = renameFile(day, true, filename);
			if (fileSize != null) {
				if (file.getBytes().length > fileSize * 1024) {
					throw new IFormException("文件超过【"+fileSize+"kb】了");
				}
			}
			minioClient.putObject(minioConfig.getBucket(), filePath, inputStream, file.getContentType());
			fileUploadModel = new FileUploadModel();
			fileUploadModel.setFileKey(filePath);
			fileUploadModel.setUrl(getFileUrl(filePath));
			fileUploadModel.setName(filename);
		} catch (Exception e) {
			throw  e;
		} finally {
			if(inputStream != null){
				inputStream.close();
			}
		}
		return fileUploadModel;
	}



	/**
	 * 图片的base64字符串集合转成图片，并上传到minio，然后返回url
	 *
	 * @param base64Str
	 * @return
	 * @throws Exception
	 */
	public String base64ToImgAndUploadOne(String base64Str) {
		String day = CommonUtils.date2Str(new Date(), "yyyy-MM-dd");
		String url = null;
		try {
			if (!StringUtils.isEmpty(base64Str)) {
				String[] base64 = base64Str.split(",");
				String type = ImagesUtils.getImgType(base64[0]);
				String filename = "1." + type;
				InputStream inputStream = ImagesUtils.base64ToInputStream(base64[1]);
				url = uploadOneFileReturnUrl(day, filename, "image/" + type, inputStream, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}

	@Override
	public String uploadOneFileReturnUrl(String day,
										 String filename,
										 String contentType,
										 InputStream inputStream,
										 boolean rename) throws Exception {
		String filePath = renameFile(day, rename, filename);
		minioClient.putObject(minioConfig.getBucket(), filePath, inputStream, contentType);
		return minioConfig.getHost()+"/"+minioConfig.getBucket() + "/" + filePath;
	}

	/**
	 * 是否需要重命名文件，返回新的文件名
	 *
	 * @param rename
	 * @param srcFileName
	 * @return
	 */
	private String renameFile(String day, boolean rename, String srcFileName) {
		String filePath = day + "/" + (UUID.randomUUID().toString().replace("-",""));
		if (rename) {//判断文件是否要重命名
			int index = srcFileName.lastIndexOf("."); //先判断文件是否有后缀名
			if (index != -1) {
				filePath = day + "/" + srcFileName.substring(index + 1) + "/" + (UUID.randomUUID().toString().replace("-",""));
				filePath += srcFileName.substring(index);
			}
		} else {
			filePath += "/" + srcFileName;//保留文件原名,即 prefix+"/"+UUID+"/"+文件原名
		}
		return filePath;
	}
}