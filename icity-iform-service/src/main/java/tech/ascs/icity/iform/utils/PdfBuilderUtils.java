package tech.ascs.icity.iform.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Pdf表格导出工具类
 * @author renjie
 * @since 0.7.3
 **/
public class PdfBuilderUtils {

    private Rectangle rectangle;
    private String title;
    private BaseFont baseFont;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");


    /**
     * 不设置纸张大小, 会自动计算
     */
    public PdfBuilderUtils() throws IOException, DocumentException {
        this(null);
    }

    public PdfBuilderUtils(Rectangle rectangle) throws IOException, DocumentException {
        this.rectangle = rectangle;
        initFont();
    }

    /**
     * 通过 页面的宽和高来创建生成器
     *
     * @param urx 宽度
     * @param ury 高度
     */
    public PdfBuilderUtils(float urx, float ury) throws IOException, DocumentException {
        this(new Rectangle(urx, ury));
    }

    private void initFont() throws IOException, DocumentException {
        baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

    }

    /**
     * 设置pdf文档的标题头
     *
     * @param title 标题头
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 开始构建生成文档
     * @param header 表格标题头
     * @param datas 表格数据
     * @param outputStream pdf数据会通过这个流输出
     * @throws DocumentException
     * @throws IOException 找不到相关字体的时候会出现的异常
     */
    public void buildTablePdf(List<String> header, List<List<String>> datas, OutputStream outputStream) throws DocumentException, IOException {

        List<List<String>> computeList = new ArrayList<>();
        computeList.add(header);
        computeList.addAll(datas);

        Rectangle rectangle = this.rectangle;
        if (rectangle == null) {
            float width = computeWidthWithDatas(computeList).stream().reduce(0.0F, (f1, f2) -> f1 + f2);
            width += 20.0F;
            float height = datas.size() * 35.0F + 200.0F;
            rectangle = new Rectangle(width, height);
        }

        Document document = new Document(rectangle);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);

        document.open();

        // 设置标题
        if (title != null) {
            document.add(buildTitle(title));
        }

        document.add(creteTime());

        PdfPTable table = new PdfPTable(header.size());
        table.setSpacingAfter(10.0F);
        table.setSpacingBefore(10.0F);

        List<PdfPRow> rows = table.getRows();
        table.setWidths(computeCellWidths(computeList));


        rows.add(createHeader(header));

        rows.addAll(datas.stream().map(this::createDataRow).collect(Collectors.toList()));

        document.add(table);

        document.close();
        writer.flush();
        writer.close();
    }

    private Paragraph buildTitle(String title) throws IOException, DocumentException {
        return buildCenterParagraph(title, 36.0F);
    }

    private Paragraph creteTime() throws IOException, DocumentException {
        return buildCenterParagraph("生成时间:" + formatter.format(LocalDateTime.now()), 24.0F);
    }

    private PdfPRow createDataRow(List<String> datas) {
        PdfPCell[] cells = new PdfPCell[datas.size()];

        int index = 0;
        for (Object data : datas) {
            PdfPCell cell = new PdfPCell(buildBaseParagraph(Objects.toString(data), 16.0F));
            cells[index++] = cell;
        }

        return new PdfPRow(cells);
    }

    private PdfPRow createHeader(List<String> headers) {
        PdfPCell[] cells = new PdfPCell[headers.size()];

        int index = 0;
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(buildCenterParagraph(header, 16.0F));
            cell.setBorderColor(BaseColor.BLUE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cells[index++] = cell;
        }

        return new PdfPRow(cells);

    }

    private Paragraph buildBaseParagraph(String context, float frontSize) {
        Font font = new Font(baseFont);
        font.setSize(frontSize);
        return new Paragraph(context, font);
    }

    private Paragraph buildCenterParagraph(String context, float frontSize) {
        Paragraph paragraph = buildBaseParagraph(context, frontSize);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        return paragraph;
    }

    private List<Float> computeWidthWithDatas(List<List<String>> datas) {

        Font font = new Font(baseFont);
        font.setSize(16.0F);
        int cellCount = datas.get(0).size();
        List<Float> widths = new ArrayList<>();

        for (int i = 0; i < cellCount; i++) {
            List<String> cellList = new ArrayList<>();
            for (List<String> rowData : datas) {
                cellList.add(rowData.get(i));
            }
            String maxDataCell = cellList.stream().max(Comparator.comparing(String::length)).orElse("");

            Chunk chunk = new Chunk(maxDataCell, font);
            widths.add(chunk.getWidthPoint() * 1.3F + 30.0F);
        }

        return widths;
    }

    private float[] computeCellWidths(List<List<String>> datas) {
        List<Float> floats = computeWidthWithDatas(datas);
        float[] result = new float[floats.size()];

        int index = 0;
        for (Float f : floats) {
            result[index] = f;
            index++;
        }
        return result;
    }


}
