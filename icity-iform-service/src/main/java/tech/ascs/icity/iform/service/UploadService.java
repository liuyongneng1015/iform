package tech.ascs.icity.iform.service;

import org.springframework.web.multipart.MultipartFile;
import tech.ascs.icity.iform.api.model.FileUploadModel;
import tech.ascs.icity.iform.api.model.FileUploadType;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.FileUploadEntity;
import tech.ascs.icity.iform.model.ItemModelEntity;
import tech.ascs.icity.jpa.service.JPAService;

import java.io.InputStream;
import java.util.List;

public interface UploadService  extends JPAService<FileUploadEntity> {

    /**
     * 上传图片并返回url，用当前时间的day作为前置
     * @param day
     * @param filename
     * @param contentType
     * @param inputStream
     * @param rename
     * @return
     * @throws Exception
     */
    String uploadOneFileReturnUrl(String day,
                                  String filename,
                                  String contentType,
                                  InputStream inputStream,
                                  boolean rename) throws Exception;

    /**
     * 上传文件，并显示是否重命名
     * @param fileSize 文件大小M
     * @param file
     * @return
     * @throws Exception
     */
    FileUploadModel uploadOneFileReturnUrl(FileUploadType uploadType, Integer fileSize, MultipartFile file) throws Exception;


    /**
     * 上传文件，并显示是否重命名
     * @param inputStream 文件输入流
     * @return
     * @throws Exception
     */
    FileUploadModel uploadOneFileByInputstream(String fileName, InputStream inputStream, String contentType) throws Exception ;

    /**
     * 重新上传文件，并显示是否重命名
     * @param inputStream 文件输入流
     * @return
     * @throws Exception
     */
    void resetUploadOneFileByInputstream(String filePath, InputStream inputStream, String contentType) throws Exception;

    /**
     * 图片的base64字符串集合转成图片，并上传到minio，然后返回url
     * @param base64List
     * @return
     * @throws Exception
     */
    List<String> base64ToImgAndUpload(List<String> base64List);

    /**
     * 上传文件路径
     * @param key minio的key
     * @return
     * @throws Exception
     */
    String getFileUrl(String key) ;


    /**
     * 上传文件，并显示是否重命名
     * @param fileUploadModel 文件上传
     * @return
     * @throws Exception
     */
    FileUploadEntity saveFileUploadEntity(FileUploadModel fileUploadModel) ;

    /**
     * 上传文件实体
     * @param fileUploadtype 文件上传类型
     * @return
     * @throws Exception
     */
    List<FileUploadEntity> getFileUploadEntity(FileUploadType fileUploadtype, String fromSource, String fromSourceDataId) ;
}
