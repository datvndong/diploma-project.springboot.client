package springboot.centralizedsystem.domains;

import org.springframework.web.multipart.MultipartFile;

public class ImportFile {

    // Upload files.
    private MultipartFile[] fileDatas;

    public MultipartFile[] getFileDatas() {
        return fileDatas;
    }

    public void setFileDatas(MultipartFile[] fileDatas) {
        this.fileDatas = fileDatas;
    }

    public ImportFile(MultipartFile[] fileDatas) {
        super();
        this.fileDatas = fileDatas;
    }

    public ImportFile() {
        super();
    }
}
