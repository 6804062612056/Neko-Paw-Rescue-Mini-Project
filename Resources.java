import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

// เปิดไฟล์ทรัพยากร (รูป / ไฟล์ด่าน) ได้ทั้งตอนรันใน jar และตอนรันจากโฟลเดอร์โปรเจกต์
public class Resources {

    public static InputStream open(String path) {
        String p = path.startsWith("/") ? path.substring(1) : path;

        // 1) อยู่ใน classpath / ใน jar
        InputStream is = Resources.class.getResourceAsStream("/" + p);
        if (is != null) return is;

        // 2) โฟลเดอร์ปัจจุบัน และโฟลเดอร์ที่วางไฟล์ jar อยู่
        File[] candidates = { new File(p), new File(jarDir(), p) };
        for (File f : candidates) {
            if (f.isFile()) {
                try {
                    return new FileInputStream(f);
                } catch (IOException e) {
                    // ลองที่ถัดไป
                }
            }
        }
        return null;
    }

    private static File jarDir() {
        try {
            File loc = new File(Resources.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return loc.isFile() ? loc.getParentFile() : loc;
        } catch (Exception e) {
            return new File(".");
        }
    }
}
