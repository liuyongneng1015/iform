package tech.ascs.icity.iform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.MinioClient;
import org.apache.commons.lang.StringUtils;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.iform.IFormException;
import tech.ascs.icity.iform.api.model.FileUploadModel;
import tech.ascs.icity.iform.api.model.FileUploadType;
import tech.ascs.icity.iform.model.FileUploadEntity;
import tech.ascs.icity.iform.service.UploadService;
import tech.ascs.icity.iform.utils.*;
import tech.ascs.icity.jpa.service.JPAManager;
import tech.ascs.icity.jpa.service.support.DefaultJPAService;

import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;


@Service
public class UploadServiceImpl extends DefaultJPAService<FileUploadEntity> implements UploadService {

	@Autowired
	private MinioClient minioClient;

	@Autowired
	private MinioConfig minioConfig;

	@Autowired
	public ObjectMapper mapper;

	private JPAManager<FileUploadEntity> fileUploadEntityManager;

	@Override
	protected void initManager() {
		super.initManager();
		fileUploadEntityManager = getJPAManagerFactory().getJPAManager(FileUploadEntity.class);
	}

	public UploadServiceImpl() {
		super(FileUploadEntity.class);
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

	@Override
	public FileUploadEntity saveFileUploadEntity(FileUploadModel fileUploadModel) {
		FileUploadEntity fileUploadEntity = fileUploadModel.isNew() ? new FileUploadEntity() : fileUploadEntityManager.get(fileUploadModel.getId());
		BeanUtils.copyProperties(fileUploadModel, fileUploadEntity);
		return fileUploadEntityManager.save(fileUploadEntity);
	}

	@Override
	public List<FileUploadEntity> getFileUploadEntity(FileUploadType fileUploadtype, String fromSource, String fromSourceDataId) {
		List<FileUploadEntity> fileUploadEntityList = new ArrayList<>();
		if(fileUploadtype == FileUploadType.ItemModel) {
			fileUploadEntityList = fileUploadEntityManager.query().filterEqual("uploadType", fileUploadtype).filterEqual("fromSource", fromSource).list();
		}else{
			fileUploadEntityList = fileUploadEntityManager.query().filterEqual("uploadType", fileUploadtype).filterEqual("fromSource", fromSource).filterEqual("fromSourceDataId", fromSourceDataId).list();
		}
		return fileUploadEntityList;
	}

	@Override
	public List<Map<String, Object>> parseExcel(MultipartFile file) {
		try {
			return ImportUtils.readExcel(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 上传文件，并显示是否重命名
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@Override
	public FileUploadModel uploadOneFileReturnUrl(String fileKey, FileUploadType uploadType, Integer fileSize, MultipartFile file) throws Exception {
		FileUploadModel fileUploadModel = new FileUploadModel();
		InputStream inputStream = file.getInputStream();
		File thumbnailFile = null;
		InputStream  thumbnailFileInputStream = null;
		try {

			String filename = file.getOriginalFilename();
			String day = CommonUtils.date2Str(new Date(), "yyyy-MM-dd");
			String filePath = org.springframework.util.StringUtils.hasText(fileKey) ? fileKey : renameFile(day, true, filename);
			if (fileSize != null) {
				if (file.getBytes().length > fileSize * 1024) {
					throw new IFormException("文件超过【"+fileSize+"kb】了");
				}
			}
			ByteArrayOutputStream baos = MergedQrCodeImages.cloneInputStream(inputStream);

			// 打开两个新的输入流
			InputStream stream1 = new ByteArrayInputStream(baos.toByteArray());
			// 打开两个新的输入流
			minioClient.putObject(minioConfig.getBucket(), filePath, stream1, file.getContentType());
			FileUploadEntity fileUploadModelEntity = new FileUploadEntity();
			fileUploadModelEntity.setFileKey(filePath);
			fileUploadModelEntity.setUrl(getFileUrl(filePath));
			fileUploadModelEntity.setName(filename);
			fileUploadModelEntity.setUploadType(uploadType);
			if(file.getContentType().contains("video")) {//视频
				thumbnailFile = new File(UUID.randomUUID()+"_thumbnail.png");
				InputStream stream2 = new ByteArrayInputStream(baos.toByteArray());
				fetchFrame(stream2, thumbnailFile.getAbsolutePath());
				thumbnailFileInputStream = new FileInputStream(thumbnailFile);
				minioClient.putObject(minioConfig.getBucket(), filePath+"_thumbnail.png", thumbnailFileInputStream, "image/png");
				fileUploadModelEntity.setThumbnail(filePath+"_thumbnail.png");
				fileUploadModelEntity.setThumbnailUrl(getFileUrl(filePath+"_thumbnail.png"));
			}
			fileUploadEntityManager.save(fileUploadModelEntity);
			BeanUtils.copyProperties(fileUploadModelEntity, fileUploadModel);
		} catch (Exception e) {
			throw  e;
		} finally {
			if(inputStream != null){
				inputStream.close();
			}
			if(thumbnailFileInputStream != null){
				thumbnailFileInputStream.close();
			}
			if(thumbnailFile != null && thumbnailFile.exists()){
				thumbnailFile.delete();
			}
		}
		return fileUploadModel;
	}

	@Override
	public FileUploadModel uploadOneFileByInputstream(String fileName, InputStream inputStream, String contentType) throws Exception {
		FileUploadModel fileUploadModel = null;
		try {
			String day = CommonUtils.date2Str(new Date(), "yyyy-MM-dd");
			String filePath = renameFile(day, true, fileName);
			minioClient.putObject(minioConfig.getBucket(), filePath, inputStream, contentType);
			fileUploadModel = new FileUploadModel();
			fileUploadModel.setFileKey(filePath);
			fileUploadModel.setUrl(getFileUrl(filePath));
			fileUploadModel.setName(fileName);
		} catch (Exception e) {
			throw  e;
		} finally {
			if(inputStream != null){
				inputStream.close();
			}
		}
		return fileUploadModel;
	}


	@Override
	public void resetUploadOneFileByInputstream(String filePath, InputStream inputStream, String contentType) throws Exception {
		try {
			minioClient.putObject(minioConfig.getBucket(), filePath, inputStream, contentType);
		} catch (Exception e) {
			throw  e;
		} finally {
			if(inputStream != null){
				inputStream.close();
			}
		}
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
	public static String renameFile(String day, boolean rename, String srcFileName) {
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

	/**
	 * 获取指定视频的帧并保存为图片至指定目录
	 * @param inputStream  源视频文件
	 * @param framefile  截取帧的图片存放路径
	 * @throws Exception
	 */
	public static void fetchFrame(InputStream inputStream, String framefile)
			throws Exception {
		FFmpegFrameGrabber ff = new FFmpegFrameGrabber(inputStream);//videofile视频路径，我用的是网络路径
		ff.start();
		int lenght = ff.getLengthInFrames();
		int i = 0;
		Frame f = null;
		while (i < lenght) {
			// 过滤前5帧，避免出现全黑的图片
			f = ff.grabFrame();
			if ((i > 5) && (f.image != null)) {
				break;
			}
			i++;
		}
		int owidth = f.imageWidth ;
		int oheight = f.imageHeight ;
		// 对截取的帧进行等比例缩放
		int width = 800;
		int height = (int) (((double) width / owidth) * oheight);
		Java2DFrameConverter converter = new Java2DFrameConverter();
		BufferedImage fecthedImage =converter.getBufferedImage(f);
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		bi.getGraphics().drawImage(fecthedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH),
				0, 0, null);
		File targetFile = new File(framefile);
		ImageIO.write(bi, "png", targetFile);
		ff.flush();
		ff.stop();
	}

}